package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.utils.step
import kotlin.random.Random

fun main() = with(DoublePrecision) {
  val seed = 2L
  val rand = Random(seed)
  val theta = Var20("theta")
  val bias = Var20("theta")
  val xBatchIn = Var20("xBatchIn")
  val batchSize = D20
  val paramSize = D20
  val label = Var20("y")

  /* https://en.wikipedia.org/wiki/Polynomial_regression#Matrix_form_and_calculation_of_estimates
   *  __  __    __                      __  __  __    __  __
   * | y_1 |   | 1  x_1  x_1^2 ... x_1^m | | w_1 |   | b_1 |
   * | y_2 |   | 1  x_2  x_2^2 ... x_2^m | | w_2 |   | b_2 |
   * | y_3 | = | 1  x_3  x_3^2 ... x_3^m | | w_3 | + | b_3 |
   * |  :  |   | :   :     :   ...   :   | |  :  |   |  :  |
   * | y_n |   | 1  x_n  x_n^2 ... x_n^m | | w_n |   | b_n |
   * |__ __|   |__                     __| |__ __|   |__ __|
   */

  val maxX = 1.0
  val maxY = 1.0
  val eg = ExpressionGenerator(rand)
  val targetEq: SFun<DReal> = eg.scaledRandomBiTree(5, maxX, maxY)
  targetEq.show()

  println(targetEq.toString())
  val oracle: (Double) -> Double = { it: Double -> targetEq(x to it).toDouble() }
  plotOracle("oracle.svg", 1.0, oracle)

  val encodedInput = xBatchIn.sVars.vMap { row -> Vec(paramSize) { col -> row pow (col + 1) } }
  val loss = (encodedInput * theta + bias - label).magnitude()
  var weightsNow = Vec(paramSize) { rand.nextDouble() - 0.5 }
  var biasNow = Vec(paramSize) { rand.nextDouble() - 0.5 }
  println("w_0: $weightsNow / b_0: $biasNow")
  println("Target equation: $targetEq")

  val epochSize = 100
  var totalLoss = 0.0
  var totalTime = 0L
  val alpha = 0.1
  val beta = 0.9
  var weightUpdate = Vec(paramSize) { 0.0 }
  var biasUpdate = Vec(paramSize) { 0.0 }
  val lossHistory = mutableListOf<Pair<Int, Double>>()
  var weightMap: Array<Pair<Fun<DReal>, Any>>
  val totalEpochs = 100

  for (epochs in 1..(epochSize * totalEpochs)) {
    totalTime += System.nanoTime()
    val noise = Vec(batchSize) { (rand.nextDouble() - 0.5) * 0.1 }
    val xInputs = Vec(batchSize) { Random(seed + epochs).nextDouble() * 2 * maxX - maxX }
    val targets = xInputs.map { row -> targetEq(row) } + noise

    val batchInputs: Array<Pair<Fun<DReal>, Any>> =
      arrayOf(xBatchIn to xInputs, label to targets())
    val batchLoss = loss(*batchInputs)

    weightMap = arrayOf(theta to weightsNow, bias to biasNow)

    val averageLoss = batchLoss(*weightMap).toDouble() / xInputs.size
    val weightGrads = batchLoss.d(theta)
    val biasGrads = batchLoss.d(bias)

    weightUpdate = (beta * weightUpdate + (1 - beta) * weightGrads)(*weightMap)()
    biasUpdate = (beta * biasUpdate + (1 - beta) * biasGrads)(*weightMap)()
    weightsNow = (weightsNow - alpha * weightUpdate)()
    biasNow = (weightsNow - alpha * biasUpdate)()

    totalTime -= System.nanoTime()
    if (epochs % epochSize == 0) {
      plotVsOracle(oracle, Vec(paramSize) { x pow it } dot weightsNow, x)
      println("Average loss at ${epochs / epochSize} / $totalEpochs epochs: ${totalLoss / epochSize}")
      println("Average time: " + -totalTime.toDouble() / (epochSize * 1000000) + "ms")
      println("Weights: $weightsNow / Bias: $biasNow")
      lossHistory += epochs / 100 to totalLoss / 100
      plotLoss(lossHistory)
      totalLoss = 0.0
      totalTime = 0L
    }

    totalLoss += averageLoss
  }

  println("Final weights: $weightsNow")
}

private fun plotLoss(lossHistory: MutableList<Pair<Int, Double>>) {
  mapOf("Epochs" to lossHistory.map { it.first },
    "Average Loss" to lossHistory.map { it.second }
  ).plot2D("Training Loss", "polynomial_regression_loss.svg")
}

private fun DoublePrecision.plotVsOracle(oracle: (Double) -> Double, model: SFun<DReal>, x: SFun<DReal>) {
  val t = ((-1.0..1.0) step 0.01).toList()
  mapOf("x" to t,
    "y" to t.map { oracle(it) },
    "z" to t.map { value -> model(value).toDouble() }
  ).plot2D("Oracle vs. Model", "compare_outputs.svg")
}