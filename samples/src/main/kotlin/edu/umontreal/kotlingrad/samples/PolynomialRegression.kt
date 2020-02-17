package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.experimental.DoublePrecision.magnitude
import edu.umontreal.kotlingrad.experimental.DoublePrecision.pow
import edu.umontreal.kotlingrad.utils.step
import org.nield.kotlinstatistics.standardDeviation
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.util.stream.Stream
import kotlin.random.Random
import kotlin.streams.toList

fun main() = with(DoublePrecision) {
  val lossHistoryCumulative = mutableListOf<List<Pair<Int, Double>>>()
  for (expNum in 0..100) {
    val oracle = ExpressionGenerator.scaledRandomBiTree(5, maxX, maxY)
    val model = learnExpression(lossHistoryCumulative, oracle)

    testPolynomial(model, oracle)

    if (expNum % 10 == 0)
      ObjectOutputStream(FileOutputStream("checkpoint.hist")).use { it.writeObject(lossHistoryCumulative) }
  }
}

fun DoublePrecision.testPolynomial(weights: Vec<DReal, D30>, targetEq: SFun<DReal>) {
  val model = decodePolynomial(weights)
  val trueError = (model - targetEq) pow 2
  val numSteps = 100
  val budget = batchSize.i * 10
  val trueErrors = List(budget) { rand.nextDouble(-maxX, maxX) }.map { Pair(it, trueError(it).toDouble()) }.toMap()
  val maxError = trueErrors.entries.maxBy { it.value }
  val avgError = trueErrors.values.average().also { println("Mean true error: $it") }
  val stdError = trueErrors.values.standardDeviation().also { println("StdDev true error: $it") }

  println("StdDevs from Mean, Random Efficiency, Adversarial Efficiency")
  for (i in 0..numSteps) {
    val stdDevs = 10.0 * i / numSteps
    val threshold = avgError + stdDevs * stdError

    val seffPG = trueErrors.values.parallelStream()
      .filter { threshold <= it }.count().toDouble() / budget
    val seffAD = trueErrors.entries.chunked(batchSize.i)
      .map { chunk -> Pair(Vec(batchSize) { chunk[it].key }, Vec(batchSize) { chunk[it].value }) }
      .toMap().entries.parallelStream().flatMap { attack(weights, it.key, it.value) }.toList().run {
        filter { threshold <= trueError(it).toDouble() }.count().toDouble() / size
      }

    println("${stdDevs}, $seffPG, $seffAD")
  }
}

private fun DoublePrecision.attack(
  weights: Vec<DReal, D30>, batchInput: Vec<DReal, D30>, targets: Vec<DReal, D30>
): Stream<Double> {
  val model = decodePolynomial(weights)
  val batchInputs = arrayOf(xBatchIn to batchInput, label to targets())
  val batchLoss = squaredLoss(*batchInputs)

  var newWeights = weights
  var update = Vec(paramSize) { 0.0 }
  for (step in 0..10) {
    val weightGrads = batchLoss.d(theta)(theta to newWeights)
    update = (beta * update + (1 - beta) * weightGrads)()
    newWeights = (newWeights - alpha * update)()
  }

  val adModel = decodePolynomial(newWeights)

  var proposals = Vec(paramSize) { sampleInputs(it) }
  val surrogateLoss = (proposals.map { model(it) - adModel(it) }).magnitude()
  val dx = surrogateLoss.d(x)

  update = Vec(paramSize) { 0.0 }
  for (step in 0..1000) {
    val dxs = proposals.map { wrap(dx(it).toDouble()) }
    update = (beta * update + (1 - beta) * dxs)()
    proposals = (proposals + alpha * update)()
  }

  return proposals.contents.map { it.toDouble() }.stream()
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

fun DoublePrecision.decodePolynomial(weights: Vec<DReal, D30>) =
  Vec(paramSize) { x pow (it + 1) } dot weights

private fun DoublePrecision.learnExpression(
  lossHistoryCumulative: MutableList<List<Pair<Int, Double>>>,
  targetEq: SFun<DReal>): Vec<DReal, D30> {
  var weightsNow = Vec(paramSize) { rand.nextDouble(-1.0, 1.0) }

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

  return weightsNow
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