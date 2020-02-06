package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import kotlin.math.pow
import kotlin.random.Random

fun main() = with(DoublePrecision) {
  val seed = 1L
  val rand = Random(seed)
  val theta = Var9("theta")
  val batchIn = Var9("xIn")
  val batchSize = D9
  val label = Var9("y")

  val eg = ExpressionGenerator(DoublePrecision, rand)
  val targetEq = eg.randomBiTree(8)
  val encodedInput = Mat<DReal, D9, D9>(batchIn.sVars.contents.map { row -> Vec<DReal, D9>(List(9) { col -> row pow col }) })
  val loss = (encodedInput * theta - label).magnitude()
  var weightsNow = Vec(batchSize) { rand.nextDouble() * 10 }
  println("Initial weights are: $weightsNow")

  println("Target equation: $targetEq")

  var epochs = 1
  var totalLoss = 0.0
  var totalTime = 0L
  val alpha = 0.001
  val lossHistory = mutableListOf<Pair<Int, Double>>()
  var weightMap: Array<Pair<Fun<DReal>, Any>>

  do {
    totalTime = System.nanoTime()
    val noise = Vec(batchSize) { (rand.nextDouble() - 0.5) * 0.1 }
    val xInputs = Vec(D9) { Random(seed + epochs).nextDouble() }
    val targets = xInputs.map { row -> targetEq(x to row) }

    val batchInputs: Array<Pair<Fun<DReal>, Any>> = arrayOf(batchIn to xInputs, label to targets())
    val batchLoss = loss(*batchInputs)

    weightMap = arrayOf(theta to weightsNow)

    val averageLoss = batchLoss(*weightMap).toDouble() / xInputs.size
    val weightGrads = batchLoss.d(theta)

    weightsNow = (weightsNow - alpha * weightGrads)(*weightMap)()

    if (epochs % 100 == 0) {
      println("Average loss at ${epochs / 100} epochs: ${totalLoss / 100}")
      totalTime -= System.nanoTime()
      println("Average time: " + -totalTime / 100 + "ns")
      lossHistory += epochs / 100 to totalLoss / 100
      totalLoss = 0.0
    }

    totalLoss += averageLoss
  } while (epochs++ / 100 < 20)

  println("Final weights: $weightsNow")

  mapOf(
    "Epochs" to lossHistory.map { it.first },
    "Average Loss" to lossHistory.map { it.second }
  ).plot2D("Training Loss", "linear_regression_loss.svg")
}

class ExpressionGenerator<X: SFun<X>>(val proto: Protocol<X>, val rng: Random) {
  val sum = { x: SFun<X>, y: SFun<X> -> x + y }
  val sub = { x: SFun<X>, y: SFun<X> -> x - y }
  val mul = { x: SFun<X>, y: SFun<X> -> x * y }
  val div = { x: SFun<X>, y: SFun<X> -> x / y }

  val operators = listOf(sum, sub, mul)
  val variables = listOf(proto.x)

  infix fun SFun<X>.wildOp(that: SFun<X>) = operators.random(rng)(this, that)

  fun randomBiTree(height: Int = 5): SFun<X> =
    if (height == 0) (listOf(proto.wrap(rng.nextDouble())) + variables).random(rng)
    else randomBiTree(height - 1) wildOp randomBiTree(height - 1)
}