package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import java.util.*

fun main() = with(DoublePrecision) {
  val rand = Random()
  val theta = Var2("theta")
  val input = Var3x2()
  val label = Var3("y")

  val loss = (input * theta - label).magnitude()

  var weights = Vec(D2) { rand.nextDouble() * 10 }
  println("Initial weights are: $weights")
  val hiddenWeights = Vec(D2) { rand.nextDouble() * 10 }
  println("Target coefficients: $hiddenWeights")

  var epochs = 0
  var totalLoss = 0.0
  var totalTime = 0.0
  val alpha = wrap(0.001)
  val lossHistory = mutableListOf<Pair<Int, Double>>()

  do {
    val startTime = System.nanoTime()
    val batch = Mat(D3, D2) { rand.nextDouble() }
    val noise = Vec(D3) { rand.nextDouble() - 0.5 }
    val targets = (batch * hiddenWeights + noise)()

    val batchLoss = loss(input to batch)(label to targets)
    val averageLoss = batchLoss(theta to weights)(*constants) / batch.rows.size
    val gradients = batchLoss.d(theta)(theta to weights)(*constants)

// TODO: Why is this SO MUCH faster?
//
//  val batchLoss = loss(
//    *(input.flatContents.mapIndexed { i, it -> it to batch.flatContents[i] } +
//      label.contents.mapIndexed { i, it -> it to targets[i] }).toTypedArray()
//  )
//
//  val averageLoss = batchLoss(
//    *(theta.contents.zip(weights.contents) + constants).toTypedArray()
//  ).toDouble() / batch.rows.size
//
//  val gradients = batchLoss.d(theta)(
//    *(theta.contents.zip(weights.contents) + constants).toTypedArray()
//  )()

    weights = (weights - alpha * gradients)() // Vanilla SGD

    if (epochs % 100 == 0 && 0 < epochs) {
      println("Average loss at ${epochs / 100} epochs: ${totalLoss / 100}")
      println("Average time: " + totalTime / 100 + "ns")
      lossHistory += epochs / 100 to totalLoss / 100
      totalTime = 0.0
      totalLoss = 0.0
    }

    totalLoss += averageLoss.toDouble()
    totalTime += System.nanoTime() - startTime
  } while (epochs++ < 20000)

  println("Final weights: $weights")
  println("Target coefficients: $hiddenWeights")

  mapOf(
    "Epochs" to lossHistory.map { it.first },
    "Average Loss" to lossHistory.map { it.second }
  ).plot2D("Training Loss", "Epochs", "linear_regression_loss.svg")
}