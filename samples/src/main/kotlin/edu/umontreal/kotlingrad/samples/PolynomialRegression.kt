package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.experimental.DoublePrecision.magnitude
import edu.umontreal.kotlingrad.experimental.DoublePrecision.pow
import edu.umontreal.kotlingrad.utils.step
import org.nield.kotlinstatistics.standardDeviation
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import kotlin.random.Random
import kotlin.streams.toList

fun main() = with(DoublePrecision) {
  val lossHistoryCumulative = mutableListOf<List<Triple<Int, Double, Double>>>()
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
  val budget = batchSize.i * 100
  val trueErrors = List(budget) { rand.nextDouble(-maxX, maxX) }.map { Pair(it, trueError(it).toDouble()) }.toMap()
  val maxError = trueErrors.entries.maxBy { it.value }
  val avgError = trueErrors.values.average().also { println("Mean true error: $it") }
  val stdError = trueErrors.values.standardDeviation().also { println("StdDev true error: $it") }

  val chunked: List<Pair<Vec<DReal, D30>, Vec<DReal, D30>>> = trueErrors.entries.chunked(batchSize.i)
    .map { chunk -> Pair(Vec(batchSize) { chunk[it].key }, Vec(batchSize) { chunk[it].value }) }
  val surrogateLoss = attack(weights, chunked)
//  plotVsOracle(trueError, surrogateLoss)
  val adErrors = sampleAndAscend(surrogateLoss)

  println("StdDevs from Mean, Random Efficiency, Adversarial Efficiency")
  for (i in 0..numSteps) {
    val stdDevs = 3.0 * i / numSteps
    val threshold = avgError + stdDevs * stdError

    val seffPG = trueErrors.values.parallelStream().filter { threshold <= it }.count().toDouble() / budget
    val seffAD = adErrors.parallelStream().filter { threshold <= trueError(it).toDouble() }.count().toDouble() / budget

    println("${stdDevs}, $seffPG, $seffAD")
  }
}

private fun DoublePrecision.sampleAndAscend(surrogateLoss: SFun<DReal>) =
  (0..100).toList().parallelStream().flatMap {
    var proposals = Vec(paramSize) { sampleInputs(it) }
    val batchLoss = proposals.map { surrogateLoss(it) }.magnitude()
    val dx = batchLoss.d(x).d(x)

    var momentum = Vec(paramSize) { 0.0 }
    for (step in 0..1000) {
      val dxs = proposals.map { wrap(dx(it).toDouble()) }
      momentum = (beta * momentum + (1 - beta) * dxs)()
      proposals = (proposals + alpha * momentum)()
    }

    proposals.contents.map { it.toDouble() }.stream()
  }.toList()

private fun DoublePrecision.attack(
  weights: Vec<DReal, D30>, batches: List<Pair<Vec<DReal, D30>, Vec<DReal, D30>>>
): SFun<DReal> {
  val model = decodePolynomial(weights)
  var newWeights = weights
  var update = Vec(paramSize) { 0.0 }

  batches.forEachIndexed { i, batch ->
    val batchInputs = arrayOf(xBatchIn to batch.first, label to batch.second)
    val batchLoss = squaredLoss(*batchInputs)

    val weightGrads = batchLoss.d(theta)(theta to newWeights)
    update = (beta * update + (1 - beta) * weightGrads)()
    newWeights = (newWeights - alpha * update)()
  }

  val adModel = decodePolynomial(newWeights)
  val surrogateLoss = (model - adModel) pow 2

  return surrogateLoss
}

val rand = Random(2L)
const val maxX = 1.0
const val maxY = 1.0
const val alpha = 0.01 // Step size
const val beta = 0.9   // Momentum
const val totalEpochs = 50
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
val interval: Double = (maxX - maxX * testSplit) / batchSize.i
val testInterval: Double = testSplit / batchSize.i

// Samples inputs randomly, but spaced evenly
// -maxX |----Train Split----|----Test Split----|----Train Split----| +maxX
fun sampleInputs(i: Int) = (if(i % 2 == 0) -1 else 1) *
  (testSplit * maxX + rand.nextDouble(i * interval, (i + 2) * interval))

fun sampleTestInputs(i: Int) = (if(i % 2 == 0) -1 else 1) *
  rand.nextDouble(i * testInterval, (i + 2) * testInterval)


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
  lossHistoryCumulative: MutableList<List<Triple<Int, Double, Double>>>,
  targetEq: SFun<DReal>): Vec<DReal, D30> {
  var weightsNow = Vec(paramSize) { rand.nextDouble(-1.0, 1.0) }

  var totalTrainLoss = 0.0
  var totalTestLoss = 0.0
  var totalTime = 0L
  var momentum = Vec(paramSize) { 0.0 }
  val lossHistory = mutableListOf<Triple<Int, Double, Double>>()
  var weightMap: Array<Pair<Fun<DReal>, Any>>
  var initialTrainLoss = 0.0
  var initialTestLoss = 0.0

  for (epochs in 1..(epochSize * totalEpochs)) {
    totalTime += System.nanoTime()
    val xTrainInputs = Vec(batchSize, ::sampleInputs)
    val trainTargets = xTrainInputs.map { targetEq(it) }
    val xTestInputs = Vec(batchSize, ::sampleTestInputs)
    val testTargets = xTestInputs.map { targetEq(it) }

    val trainInputs = arrayOf(xBatchIn to xTrainInputs, label to trainTargets())
    val trainLoss = squaredLoss(*trainInputs)
    val testInputs = arrayOf(xBatchIn to xTestInputs, label to testTargets())
    val testLoss = squaredLoss(*testInputs)

    weightMap = arrayOf(theta to weightsNow)

    totalTrainLoss += trainLoss(*weightMap).toDouble() / xTrainInputs.size
    totalTestLoss += testLoss(*weightMap).toDouble() / (100.0 * xTestInputs.size)
    val weightGrads = trainLoss.d(theta)

    momentum = (beta * momentum + (1 - beta) * weightGrads)(*weightMap)()
    weightsNow = (weightsNow - alpha * momentum)()

    totalTime -= System.nanoTime()
    if (epochs % epochSize == 0) {
//      plotVsOracle(targetEq, decodePolynomial(weightsNow))
//      println("Average loss at ${epochs / epochSize} / $totalEpochs epochs: ${totalLoss / epochSize}")
//      println("Average time: " + -totalTime.toDouble() / (epochSize * 1000000) + "ms")
//      println("Weights: $weightsNow")
      if (initialTestLoss == 0.0) initialTestLoss = totalTestLoss
      if (initialTrainLoss == 0.0) initialTrainLoss = totalTrainLoss
      lossHistory += Triple(epochs / epochSize,
        totalTrainLoss / initialTrainLoss,
        totalTestLoss / initialTestLoss
      )
//      plotLoss(lossHistory)
      totalTrainLoss = 0.0
      totalTestLoss = 0.0
      totalTime = 0L
    }
  }

//  plotLoss(lossHistory)
//    println("Final weights: $weightsNow")
  lossHistoryCumulative += lossHistory

  return weightsNow
}

private fun plotLoss(lossHistory: MutableList<Triple<Int, Double, Double>>) {
  mapOf("Epochs" to lossHistory.map { it.first },
    "Train Loss" to lossHistory.map { it.second },
    "Test Loss" to lossHistory.map { it.third }
  ).plot2D("Loss", "polynomial_regression_loss.svg")
}

private fun DoublePrecision.plotVsOracle(oracle: SFun<DReal>, model: SFun<DReal>) {
  val t = ((-1.0..1.0) step 0.01).toList()
  mapOf("x" to t,
    "y" to t.map { oracle(it).toDouble() },
    "z" to t.map { model(it).toDouble() }
  ).plot2D("Oracle vs. Model", "compare_outputs.svg")
}