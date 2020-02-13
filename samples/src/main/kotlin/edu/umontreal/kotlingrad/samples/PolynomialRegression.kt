package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.experimental.DoublePrecision.magnitude
import edu.umontreal.kotlingrad.experimental.DoublePrecision.pow
import edu.umontreal.kotlingrad.utils.step
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import kotlin.random.Random

fun main() = with(DoublePrecision) {
  val seed = 2L
  val lossHistoryCumulative = mutableListOf<List<Pair<Int, Double>>>()
  for (expNum in 0..100) {
    val rand = Random(seed + expNum)
    val eg = ExpressionGenerator(rand)
    val targetExp: SFun<DReal> = eg.scaledRandomBiTree(5, maxX, maxY)
    val polynomial = learnExpression(rand, lossHistoryCumulative, targetExp)

    testPolynomial(rand, polynomial, targetExp)

    if (expNum % 10 == 0)
      ObjectOutputStream(FileOutputStream("checkpoint.hist")).use { it.writeObject(lossHistoryCumulative) }
  }
}

fun DoublePrecision.testPolynomial(rand: Random, polynomial: SFun<DReal>, targetEq: SFun<DReal>) {
  val sampleInputs = { i: Int ->
    // Samples inputs randomly, but spaced regularly
    (-1.0 + (i * rand.nextDouble() * 2 - 1) * (maxX * 2 / batchSize.i)).coerceIn(-1.0, 1.0)
  }

  val xInputs = Vec(batchSize, sampleInputs)
  val targets = xInputs.map { targetEq(it) } //+ (rand.nextDouble() - 0.5) * 0.1 }
  val batchInputs: Array<Pair<Fun<DReal>, Any>> = arrayOf(xBatchIn to xInputs, label to targets())
  val batchLoss = (xInputs.map { polynomial(it) } - label).magnitude()

  var randSearchEff = 0

  val error = ((polynomial - targetEq) pow 2) pow 0.5

  println("Error Threshold, Errors / 1k")
  for (i in 50..200) {
    val seff = (1..2000).count { i * 0.001 < error(rand.nextDouble(-1.0, 1.0)).toDouble() } / 2.0
    println("${i/1000.0}, " + seff)
  }

  for (i in 0..1000) {
    var xEval = rand.nextDouble(-1.0, 1.0)
    val seff = 1
    val dx = batchLoss.d(x)
    xEval
  }

  polynomial.d(x)
}

const val maxX = 1.0
const val maxY = 1.0
const val alpha = 0.01 // Step size
const val beta = 0.9   // Momentum
const val totalEpochs = 20
const val epochSize = 10
val batchSize = D30
val paramSize = D30
val theta = DoublePrecision.Var30("theta")
val xBatchIn = DoublePrecision.Var30("xBatchIn")
val label = DoublePrecision.Var30("y")
val encodedInput = xBatchIn.sVars.vMap { row -> DoublePrecision.Vec(paramSize) { col -> row pow (col + 1) } }
val pred = encodedInput * theta
val squaredLoss = (pred - label).magnitude()

/* https://en.wikipedia.org/wiki/Polynomial_regression#Matrix_form_and_calculation_of_estimates
 *  __  __    __                      __  __  __
 * | y_1 |   | 1  x_1  x_1^2 ... x_1^m | | w_1 |
 * | y_2 |   | 1  x_2  x_2^2 ... x_2^m | | w_2 |
 * | y_3 | = | 1  x_3  x_3^2 ... x_3^m | | w_3 |
 * |  :  |   | :   :     :   ...   :   | |  :  |
 * | y_n |   | 1  x_n  x_n^2 ... x_n^m | | w_n |
 * |__ __|   |__                     __| |__ __|
 */

private fun DoublePrecision.learnExpression(
  rand: Random,
  lossHistoryCumulative: MutableList<List<Pair<Int, Double>>>,
  targetEq: SFun<DReal>): SFun<DReal> {
  var weightsNow = Vec(paramSize) { rand.nextDouble(-1.0, 1.0) }

  val sampleInputs = { i: Int ->
    // Samples inputs randomly, but spaced regularly
    (-1.0 + (i * rand.nextDouble() * 2 - 1) * (maxX * 2 / batchSize.i)).coerceIn(-1.0, 1.0)
  }

  fun decodePolynomial(weights: Vec<DReal, D30>) = Vec(paramSize) { x pow (it + 1) } dot weights

  var totalLoss = 0.0
  var totalTime = 0L
  var weightUpdate = Vec(paramSize) { 0.0 }
  val lossHistory = mutableListOf<Pair<Int, Double>>()
  var weightMap: Array<Pair<Fun<DReal>, Any>>

  for (epochs in 1..(epochSize * totalEpochs)) {
    totalTime += System.nanoTime()
    val xInputs = Vec(batchSize, sampleInputs)
    val targets = xInputs.map { targetEq(it) } //+ (rand.nextDouble() - 0.5) * 0.1 }

    val batchInputs: Array<Pair<Fun<DReal>, Any>> = arrayOf(xBatchIn to xInputs, label to targets())
    val batchLoss = squaredLoss(*batchInputs)

    weightMap = arrayOf(theta to weightsNow)

    totalLoss += batchLoss(*weightMap).toDouble() / xInputs.size
    val weightGrads = batchLoss.d(theta)

    weightUpdate = (beta * weightUpdate + (1 - beta) * weightGrads)(*weightMap)()
    weightsNow = (weightsNow - alpha * weightUpdate)()

    totalTime -= System.nanoTime()
    if (epochs % epochSize == 0) {
//      plotVsOracle(targetEq, decodePolynomial(weightsNow))
      println("Average loss at ${epochs / epochSize} / $totalEpochs epochs: ${totalLoss / epochSize}")
      println("Average time: " + -totalTime.toDouble() / (epochSize * 1000000) + "ms")
//      println("Weights: $weightsNow")
      lossHistory += epochs / epochSize to totalLoss / epochSize
//      plotLoss(lossHistory)
      totalLoss = 0.0
      totalTime = 0L
    }
  }

  plotLoss(lossHistory)
//    println("Final weights: $weightsNow")
  lossHistoryCumulative += lossHistory

  return decodePolynomial(weightsNow)
}

private fun plotLoss(lossHistory: MutableList<Pair<Int, Double>>) {
  mapOf("Epochs" to lossHistory.map { it.first },
    "Average Loss" to lossHistory.map { it.second }
  ).plot2D("Training Loss", "polynomial_regression_loss.svg")
}

private fun DoublePrecision.plotVsOracle(oracle: SFun<DReal>, model: SFun<DReal>) {
  val t = ((-1.0..1.0) step 0.01).toList()
  mapOf("x" to t,
    "y" to t.map { oracle(it).toDouble() },
    "z" to t.map { model(it).toDouble() }
  ).plot2D("Oracle vs. Model", "compare_outputs.svg")
}