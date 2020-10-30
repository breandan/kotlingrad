package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.api.*
import edu.umontreal.kotlingrad.shapes.*
import org.nield.kotlinstatistics.standardDeviation
import java.util.stream.Stream
import kotlin.streams.toList

fun main() {
  (0..200).toList().parallelStream().map {
    val startTime = System.currentTimeMillis()
    val oracle = PolyGenerator.scaledRandomBiTree(5, maxX, maxY)
    val (model, history) = learnExpression(oracle)
//  println("Finished $it in ${(startTime - System.currentTimeMillis()) / 60000.0}s")
    Triple(oracle, model, history)
    testPolynomial(model, oracle)
  }.toList()

  // TODO: Fun is not serializable?
//  ObjectInputStream(FileInputStream("models.hist")).use {
//    val t = it.readObject()
//    (t as List<Pair<SFun<DReal>, Vec<DReal, D30>>>).forEach { (oracle, model) ->
//      DoublePrecision.testPolynomial(model, oracle)
//    }
//  }
}

val numSteps = 100
val budget = batchSize.i * 100
val testPoints = List(budget) { rand.nextDouble(-maxX, maxX) }.sorted()

fun testPolynomial(weights: Vec<DReal, D30>, targetEq: SFun<DReal>) {
  val model = decodePolynomial(weights)
  val trueError = (model - targetEq) pow 2
  val trueErrors = testPoints.map { it to trueError(it).toDouble() }.toMap()
  val maxError = trueErrors.entries.maxBy { it.value }
  val avgError = trueErrors.values.average().also { println("Mean true error: $it") }
  val stdError = trueErrors.values.standardDeviation().also { println("StdDev true error: $it") }

  val batches: List<Pair<Vec<DReal, D30>, Vec<DReal, D30>>> = trueErrors.entries.chunked(batchSize.i)
    .map { chunk -> DReal.Vec(batchSize) { chunk[it].key } to DReal.Vec(batchSize) { targetEq(chunk[it].key).toDouble() } }
  val (newModel, surrogateLoss) = attack(weights, batches)

//  println("Oracle vs. Model")
//  trueErrors.map { println("${it.key}, ${targetEq(it.key).toDouble()}, ${model(it.key).toDouble()}") }
//
//  println("True vs. Surrogate Loss")
//  trueErrors.map { println("${it.key}, ${it.value}, ${surrogateLoss(it.key).toDouble()}") }
//
//  plotVsOracle(trueError, surrogateLoss)

  val adErrors = (0..100).toList().parallelStream().flatMap { sampleAndAscend(surrogateLoss) }.toList()
  println("StdDevs from Mean, Random Efficiency, Adversarial Efficiency")
  for (i in 0..numSteps) {
    val stdDevs = 3.0 * i / numSteps
    val threshold = avgError + stdDevs * stdError

    val seffPG = trueErrors.values.parallelStream().filter { threshold <= it }.count().toDouble() / budget
    val seffAD = adErrors.parallelStream().filter { threshold <= trueError(it).toDouble() }.count().toDouble() / budget

    println("${stdDevs}, $seffPG, $seffAD")
  }
}

private fun sampleAndAscend(surrogateLoss: SFun<DReal>): Stream<Double> {
  var particles = DReal.Vec(paramSize) { rand.nextDouble(-maxX, maxX) }
  var momentum = DReal.Vec(paramSize) { 0.0 }
  val dx = surrogateLoss.d(x).d(x)

  for (step in 0..1000) {
//    particles.forEachIndexed { i, p -> println("step_$step, particle_$i, $p, ${surrogateLoss(p).toDouble()}") }
    val dxs = particles.map { DReal(dx(it).toDouble()) }
    momentum = (beta * momentum + (1 - beta) * dxs)()
    particles = (particles + 0.1 * momentum)()
  }

//  println("Start trajectories: ")
//  trajectories.forEach { p -> p.first.forEachIndexed { i, it -> println("${it.toDouble()}, ${p.second[i]}") } }

  return particles.contents.map { it.toDouble() }.stream()
}

private fun attack(
  weights: Vec<DReal, D30>, batches: List<Pair<Vec<DReal, D30>, Vec<DReal, D30>>>
): Pair<SFun<DReal>, SFun<DReal>> {
  val model = decodePolynomial(weights)
  var newWeights = weights
  var update = DReal.Vec(paramSize) { 0.0 }

  batches.forEachIndexed { i, batch ->
    val batchInputs = arrayOf(xBatchIn to batch.first, label to batch.second)
    val batchLoss = squaredLoss(*batchInputs)

    val weightGrads = batchLoss.d(theta)(theta to newWeights)
    update = (beta * update + (1 - beta) * weightGrads)()
    newWeights = (newWeights - alpha * update)()
  }

  val adModel = decodePolynomial(newWeights)
  val surrogateLoss = (model - adModel) pow 2

  return decodePolynomial(newWeights) to surrogateLoss
}