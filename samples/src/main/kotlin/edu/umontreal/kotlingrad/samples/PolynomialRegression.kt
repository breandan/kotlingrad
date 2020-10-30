package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.api.*
import edu.umontreal.kotlingrad.shapes.*
import edu.umontreal.kotlingrad.utils.step
import java.io.*
import kotlin.random.Random
import kotlin.streams.toList

fun main() {
  (0..200).toList().parallelStream().map {
    val startTime = System.currentTimeMillis()
    val oracle = PolyGenerator.scaledRandomBiTree(5, maxX, maxY)
    val (model, history) = learnExpression(oracle)
    println("Finished $it in ${(startTime - System.currentTimeMillis()) / 60000.0}s")
    Triple(oracle, model, history)
  }.toList().also {
    val lossHistoryCumulative = it.map { it.third }
    lossHistoryCumulative.flatten().groupBy { it.first }.mapValues {
      listOf(it.key,
        it.value.map { it.second }.average(),
        it.value.map { it.second }.standardError(),
        it.value.map { it.third }.average(),
        it.value.map { it.third }.standardError()
      )
    }.forEach { println(it.value.joinToString(", ")) }
    val models = it.map { it.first to it.second }
    ObjectOutputStream(FileOutputStream("losses.hist")).use { it.writeObject(lossHistoryCumulative) }
    ObjectOutputStream(FileOutputStream("models.hist")).use { it.writeObject(models) }
  }
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
val theta by DReal.Var(D30)
val xBatchIn by DReal.Var(D30)
val label by DReal.Var(D30)
val encodedInput = xBatchIn.sVars.vMap { row -> DReal.Vec(paramSize) { col -> row pow (col + 1) } }
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

fun decodePolynomial(weights: Vec<DReal, D30>) =
  Vec(paramSize) { x pow (it + 1) } dot weights

fun learnExpression(targetEq: SFun<DReal>): Pair<Vec<DReal, D30>, List<Triple<Int, Double, Double>>> {
  var weightsNow = DReal.Vec(paramSize) { rand.nextDouble(-1.0, 1.0) }

  var totalTrainLoss = 0.0
  var totalTestLoss = 0.0
  var totalTime = 0L
  var momentum = DReal.Vec(paramSize) { 0.0 }
  val lossHistory = mutableListOf<Triple<Int, Double, Double>>()
  var weightMap: Array<Pair<Fun<DReal>, Any>>
  var initialTrainLoss = 0.0
  var initialTestLoss = 0.0

  for (epochs in 1..(epochSize * totalEpochs)) {
    totalTime += System.nanoTime()
    val xTrainInputs = DReal.Vec(batchSize, ::sampleInputs)
    val trainTargets = xTrainInputs.map { targetEq(it) }
    val xTestInputs = DReal.Vec(batchSize, ::sampleTestInputs)
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
//      println("Average time: " + -totalTime.toDouble() / (epochSize * 1000000) + "ms")
//      println("Weights: $weightsNow")
      if (initialTestLoss == 0.0) initialTestLoss = totalTestLoss
      if (initialTrainLoss == 0.0) initialTrainLoss = totalTrainLoss
      println("${epochs / epochSize}, ${totalTrainLoss / initialTrainLoss}, ${totalTestLoss / initialTestLoss}")
      lossHistory += Triple(epochs / epochSize,
        totalTrainLoss / initialTrainLoss,
        totalTestLoss / initialTestLoss
      )

//      val mdlNow = decodePolynomial(weightsNow)
//      testPoints.forEach { println("epoch_0${epochs/epochSize}, $it, ${mdlNow(it).toDouble()}") }
//      plotLoss(lossHistory)
      totalTrainLoss = 0.0
      totalTestLoss = 0.0
      totalTime = 0L
    }
  }

//  plotLoss(lossHistory)
//    println("Final weights: $weightsNow")
  return weightsNow to lossHistory
}

private fun plotLoss(lossHistory: MutableList<Triple<Int, Double, Double>>) {
  mapOf("Epochs" to lossHistory.map { it.first },
    "Train Loss" to lossHistory.map { it.second },
    "Test Loss" to lossHistory.map { it.third }
  ).plot2D("Loss", "polynomial_regression_loss.svg")
}

private fun plotVsOracle(oracle: SFun<DReal>, model: SFun<DReal>) {
  val t = ((-1.0..1.0) step 0.01).toList()
  mapOf("x" to t,
    "y" to t.map { oracle(it).toDouble() },
    "z" to t.map { model(it).toDouble() }
  ).plot2D("Oracle vs. Model", "compare_outputs.svg")
}