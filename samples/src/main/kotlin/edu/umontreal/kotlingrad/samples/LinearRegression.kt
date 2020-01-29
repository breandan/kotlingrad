package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import java.util.*

fun main() = with(DoublePrecision) {
  val rand = Random()
  val theta = Var2("theta")
  val input = Var3x2("input")
  val bias = Var("bias")
  val label = Var3("y")

  val loss = ((input * theta).map { it + bias } - label).magnitude()

  var weights = Vec(D2) { rand.nextDouble() * 10 }
  var biasNow = wrap(rand.nextDouble() * 10)
  println("Initial weights are: $weights")
  val hiddenWeights = Vec(D2) { rand.nextDouble() * 10 }
  val hiddenBias = wrap(rand.nextDouble() * 10)
  println("Target coefficients: $hiddenWeights")

  var epochs = 1
  var totalLoss = 0.0
  var totalTime = 0L
  val alpha = wrap(0.001)
  val lossHistory = mutableListOf<Pair<Int, Double>>()

  do {
    totalTime = System.nanoTime()
    val batch = Mat(D3, D2) { _, _ -> rand.nextDouble() }
    val noise = Vec(D3) { rand.nextDouble() - 0.5 }
    val targets = ((batch * hiddenWeights).map { it + hiddenBias } + noise)()

//  TODO: Why is this SO MUCH slower?
//  val batchLoss = loss(input to batch)(label to targets)
//  val averageLoss = batchLoss(theta to weights)(*constants) / batch.numRows
//  val gradients = batchLoss.d(theta)(theta to weights)(*constants)

    val inputs = (input.flatContents.mapIndexed { i, it -> it to batch.flatContents[i] } + constants +
      label.contents.mapIndexed { i, it -> it to targets[i] }).toTypedArray()
    val batchLoss: SFun<DReal> = loss(*inputs)

    val weightClosure = (theta.contents.zip(weights.contents) + arrayOf(bias to biasNow) + constants).toTypedArray()
    val averageLoss = batchLoss(*weightClosure).toDouble() / batch.rows.size
    val gradients = batchLoss.d(theta)
    val biasGrads = batchLoss.d(bias)

    // gradients.show()

    weights = (weights - alpha * gradients)(*weightClosure)() // Vanilla SGD
    biasNow = (biasNow - alpha * biasGrads)(*weightClosure)() // Vanilla SGD

    if (epochs % 100 == 0) {
      println("Average loss at ${epochs / 100} epochs: ${totalLoss / 100}")
      totalTime -= System.nanoTime()
      println("Average time: " + -totalTime / 100 + "ns")
      lossHistory += epochs / 100 to totalLoss / 100
      totalLoss = 0.0
    }

    totalLoss += averageLoss
  } while (epochs++ < 20000)

  println("Final weights: $weights, bias: $biasNow")
  println("Target coefficients: $hiddenWeights, $hiddenBias")

  mapOf(
    "Epochs" to lossHistory.map { it.first },
    "Average Loss" to lossHistory.map { it.second }
  ).plot2D("Training Loss", "linear_regression_loss.svg")
}