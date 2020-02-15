package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.experimental.DoublePrecision.magnitude
import edu.umontreal.kotlingrad.experimental.DoublePrecision.pow
import edu.umontreal.kotlingrad.utils.step
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import kotlin.random.Random
import kotlin.streams.toList

fun main() = with(DoublePrecision) {
  val lossHistoryCumulative = mutableListOf<List<Pair<Int, Double>>>()
  for (expNum in 0..100) {
    val targetExp = ExpressionGenerator.scaledRandomBiTree(5, maxX, maxY)
    val polynomial = learnExpression(lossHistoryCumulative, targetExp)

    testPolynomial(polynomial, targetExp)

    if (expNum % 10 == 0)
      ObjectOutputStream(FileOutputStream("checkpoint.hist")).use { it.writeObject(lossHistoryCumulative) }
  }
}

fun DoublePrecision.testPolynomial(model: SFun<DReal>, targetEq: SFun<DReal>) {
  val trueError = (model - targetEq) pow 2

  println("Threshold, Random Efficiency, Adversarial Efficiency")
  for (i in 1..200) {
    val threshold = i / 1000.0
    val budget = 10000
    val seffPG = List(budget) { rand.nextDouble(-testSplit, testSplit) }
      .filter { threshold < trueError(it).toDouble() }.count().toDouble() / budget

    val sqrtBudget = kotlin.math.sqrt(budget.toDouble()).toInt()
    val seffAD = (0..sqrtBudget).toList().parallelStream()
      .map { attack(trueError, model, sqrtBudget) }
      .toList().flatten().take(budget - batchSize.i).run {
        filter { threshold < trueError(it).toDouble() }.count().toDouble() / size
      }

    println("$threshold, $seffPG, $seffAD")
  }
}

private fun DoublePrecision.attack(targetEq: SFun<DReal>, model: SFun<DReal>, budget: Int): MutableList<Double> {
  var batchNow = Vec(batchSize, ::sampleInputs)
  val targets = batchNow.map { targetEq(it) }
  val batchInputs = arrayOf(xBatchIn to batchNow, label to targets())
  val batchLoss = (batchNow.map { model(it) } - label).magnitude()(*batchInputs)
  val dx = batchLoss.d(x)

  var update = Vec(paramSize) { 0.0 }
  for (step in 0..budget) {
    val dxs = batchNow.map { wrap(dx(it).toDouble()) }
    update = (beta * update + (1 - beta) * dxs)()
    batchNow = (batchNow + alpha * update)()
  }

  return batchNow.contents.map { it.toDouble() }.toMutableList()
}

val rand = Random(2L)
const val maxX = 1.0
const val maxY = 1.0
const val alpha = 0.01 // Step size
const val beta = 0.9   // Momentum
const val totalEpochs = 10
const val epochSize = 5
const val testSplit = 0.2 // Hold out test
val batchSize = D30
val paramSize = D30
val theta = DoublePrecision.Var30("theta")
val xBatchIn = DoublePrecision.Var30("xBatchIn")
val label = DoublePrecision.Var30("y")
val encodedInput = xBatchIn.sVars.vMap { row -> DoublePrecision.Vec(paramSize) { col -> row pow (col + 1) } }
val pred = encodedInput * theta
val squaredLoss = (pred - label).magnitude()
val interval: Double = 2 * maxX / batchSize.i

// Samples inputs randomly, but spaced evenly
// -maxX |----Train Split----|----Test Split----|----Train Split----| +maxX
fun sampleInputs(i: Int) = -maxX + rand.nextDouble(i * interval, (i + 1) * interval)

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
  lossHistoryCumulative: MutableList<List<Pair<Int, Double>>>,
  targetEq: SFun<DReal>): SFun<DReal> {
  var weightsNow = Vec(paramSize) { rand.nextDouble(-1.0, 1.0) }

  fun decodePolynomial(weights: Vec<DReal, D30>) = Vec(paramSize) { x pow (it + 1) } dot weights

  var totalLoss = 0.0
  var totalTime = 0L
  var weightUpdate = Vec(paramSize) { 0.0 }
  val lossHistory = mutableListOf<Pair<Int, Double>>()
  var weightMap: Array<Pair<Fun<DReal>, Any>>

  for (epochs in 1..(epochSize * totalEpochs)) {
    totalTime += System.nanoTime()
    val xInputs = Vec(batchSize, ::sampleInputs)
    val targets = xInputs.map { targetEq(it) }

    val batchInputs = arrayOf(xBatchIn to xInputs, label to targets())
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