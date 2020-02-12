package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.utils.step
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import kotlin.random.Random

fun main() = with(DoublePrecision) {
  val seed = 4L
  val lossHistoryCumulative = mutableListOf<List<Pair<Int, Double>>>()
  for (expNum in 0..100) {
    val rand = Random(seed + expNum)
    val eg = ExpressionGenerator(rand)
    val targetExp: SFun<DReal> = eg.scaledRandomBiTree(5, maxX, maxY)
    learnExpression(rand, lossHistoryCumulative, targetExp, expNum)
  }
}

const val maxX = 1.0
const val maxY = 1.0
const val alpha = 0.01 // Step size
const val beta = 0.9   // Momentum
const val totalEpochs = 80
const val epochSize = 10

val batchSize = D20
val paramSize = D20
val theta = DoublePrecision.Var20("theta")
val bias = DoublePrecision.Var20("bias")
val xBatchIn = DoublePrecision.Var20("xBatchIn")
val label = DoublePrecision.Var20("y")

/* https://en.wikipedia.org/wiki/Polynomial_regression#Matrix_form_and_calculation_of_estimates
 *  __  __    __                      __  __  __    __  __
 * | y_1 |   | 1  x_1  x_1^2 ... x_1^m | | w_1 |   | b_1 |
 * | y_2 |   | 1  x_2  x_2^2 ... x_2^m | | w_2 |   | b_2 |
 * | y_3 | = | 1  x_3  x_3^2 ... x_3^m | | w_3 | + | b_3 |
 * |  :  |   | :   :     :   ...   :   | |  :  |   |  :  |
 * | y_n |   | 1  x_n  x_n^2 ... x_n^m | | w_n |   | b_n |
 * |__ __|   |__                     __| |__ __|   |__ __|
 */

private fun DoublePrecision.learnExpression(
  rand: Random,
  lossHistoryCumulative: MutableList<List<Pair<Int, Double>>>,
  targetEq: SFun<DReal>,
  expNum: Int) {
//  println(targetEq.toString())
//  val oracle: (Double) -> Double = { it: Double -> targetEq(x to it).toDouble() }
//  plotOracle("oracle.svg", 1.0, oracle)

  val encodedInput = xBatchIn.sVars.vMap { row -> Vec(paramSize) { col -> row pow (col + 1) } }
  val loss = (encodedInput * theta + bias - label).magnitude()
  var weightsNow = Vec(paramSize) { rand.nextDouble(-1.0, 1.0) }
  var biasNow = Vec(paramSize) { rand.nextDouble(-1.0, 1.0) }
//  println("w_0: $weightsNow / b_0: $biasNow")
//  println("Target equation: $targetEq")

  var totalLoss = 0.0
  var totalTime = 0L
  var weightUpdate = Vec(paramSize) { 0.0 }
  var biasUpdate = Vec(paramSize) { 0.0 }
  val lossHistory = mutableListOf<Pair<Int, Double>>()
  var weightMap: Array<Pair<Fun<DReal>, Any>>

  for (epochs in 1..(epochSize * totalEpochs)) {
    totalTime += System.nanoTime()
    val noise = Vec(batchSize) { (rand.nextDouble() - 0.5) * 0.1 }
    val xInputs = Vec(batchSize, genInputs(rand))
    val targets = xInputs.map { row -> targetEq(row) } + noise

    val batchInputs: Array<Pair<Fun<DReal>, Any>> = arrayOf(xBatchIn to xInputs, label to targets())
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
//      plotVsOracle(oracle, Vec(paramSize) { x pow (it + 1) } dot weightsNow, x)
//      println("Average loss at ${epochs / epochSize} / $totalEpochs epochs: ${totalLoss / epochSize}")
//      println("Average time: " + -totalTime.toDouble() / (epochSize * 1000000) + "ms")
//      println("Weights: $weightsNow / Bias: $biasNow")
      lossHistory += epochs / epochSize to totalLoss / epochSize
//      plotLoss(lossHistory)
      totalLoss = 0.0
      totalTime = 0L
    }

    totalLoss += averageLoss
  }

  plotLoss(lossHistory)
//    println("Final weights: $weightsNow")
  lossHistoryCumulative += lossHistory

  if (expNum % 10 == 0)
    ObjectOutputStream(FileOutputStream("cumulativeHistory.loss")).use { it.writeObject(lossHistoryCumulative) }
}

private fun genInputs(rand: Random) = { i: Int ->
  (-1.0 + (i * rand.nextDouble() * 2 - 1) * (maxX * 2 / batchSize.i)).coerceIn(-1.0, 1.0)
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