package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import org.nield.kotlinstatistics.standardDeviation
import java.io.FileInputStream
import java.io.ObjectInputStream
import kotlin.streams.toList

fun main() {
  ObjectInputStream(FileInputStream("models.hist")).use {
    val t = it.readObject()
    (t as List<Pair<SFun<DReal>, Vec<DReal, D30>>>).forEach { (oracle, model) ->
      DoublePrecision.testPolynomial(model, oracle)
    }
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