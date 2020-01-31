package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import java.util.*

fun main() = with(DoublePrecision) {
  val rand = Random(1)
  val theta = Var2("theta")
  val input = Var3x2("input")
  val bias = Var("bias")
  val label = Var3("y")

  val loss = ((input * theta).map { it + bias } - label).magnitude()

  var weightsNow = Vec(D2) { rand.nextDouble() * 10 }
  var biasNow = wrap(rand.nextDouble() * 10)
  println("Initial weights are: $weightsNow")
  val hiddenWeights = Vec(D2) { rand.nextDouble() * 10 }
  val hiddenBias = rand.nextDouble() * 10
  println("Target coefficients: $hiddenWeights")

  var epochs = 1
  var totalLoss = 0.0
  var totalTime = 0L
  val alpha = 0.001
  val lossHistory = mutableListOf<Pair<Int, Double>>()
  var weightMap: Array<Pair<Fun<DReal>, Any>>

  do {
    totalTime = System.nanoTime()
    val noise = Vec(D3) { rand.nextDouble() - 0.5 }
    val batch = Mat(D3, D2) { _, _ -> rand.nextDouble() }
    val targets = ((batch * hiddenWeights).map { it + hiddenBias } + noise)()

    val batchInputs = arrayOf(input to batch, label to targets)
    val batchLoss = loss(*batchInputs)

    weightMap = arrayOf(theta to weightsNow, bias to biasNow)

    println("Input bnds: " + Bindings(batchInputs.toList().bind()))
    println("Combined: " + (loss.bindings + Bindings(batchInputs.toList().bind())))
    println("Weights: " + Bindings(weightMap.toList().bind()))
    println("Combined2: " + (loss.bindings + Bindings(batchInputs.toList().bind()) + Bindings(weightMap.toList().bind())))
    println("Combined3: " + (loss(*batchInputs).bindings + Bindings(weightMap.toList().bind())))
    println("Combined4: " + (Composition(loss, constants + batchInputs.toList().bind() + weightMap.toList().bind()).evaluate))
    println("Batchloss: " + Composition(loss, constants + batchInputs.toList().bind()).bindings)
    println("losseval" + loss(*batchInputs))

    val averageLoss = batchLoss(*weightMap).toDouble() / batch.rows.size
    val weightGrads = batchLoss.d(theta)
    val biasGrads = batchLoss.d(bias)

    weightsNow = (weightsNow - alpha * weightGrads)(*weightMap)()
    biasNow = (biasNow - alpha * biasGrads)(*weightMap)()

    if (epochs % 100 == 0) {
      println("Average loss at ${epochs / 100} epochs: ${totalLoss / 100}")
      totalTime -= System.nanoTime()
      println("Average time: " + -totalTime / 100 + "ns")
      lossHistory += epochs / 100 to totalLoss / 100
      totalLoss = 0.0
    }

    totalLoss += averageLoss
  } while (epochs++ < 20000)

  println("Final weights: $weightsNow, bias: $biasNow")
  println("Target coefficients: $hiddenWeights, $hiddenBias")

  mapOf(
    "Epochs" to lossHistory.map { it.first },
    "Average Loss" to lossHistory.map { it.second }
  ).plot2D("Training Loss", "linear_regression_loss.svg")
}