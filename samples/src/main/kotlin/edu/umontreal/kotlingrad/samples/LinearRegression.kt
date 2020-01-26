package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import java.util.*

fun main() = with(DoublePrecision) {
  val rand = Random()
  val theta = Var2("theta")
  val input = Var3x2()
  val label = Var3("y")

  val loss = (input * theta - label).magnitude()

  var weights = Vec(rand.nextDouble() * 10, rand.nextDouble() * 10)
  println("Initial weights are: $weights")
  var totalLoss = 0.0
  var epochs = 0
  val alpha = wrap(0.001)

  val hiddenWeights = Vec(rand.nextDouble() * 10, rand.nextDouble() * 10)
  println("Target coefficients: $hiddenWeights")
  val consts = constants.entries.map { it.key to wrap(it.value) }

  val lossHistory = mutableListOf<Pair<Int, Double>>()

  do {
    val batch = Mat(D3, D2) { rand.nextDouble() }
    val noise = Vec(D3) { rand.nextDouble() - 0.5 }
    val targets = (batch * hiddenWeights + noise)()

    val batchLoss = loss(
      *(input.flatContents.mapIndexed { i, it -> it to batch.flatContents[i] } +
        label.contents.mapIndexed { i, it -> it to targets[i] }).toTypedArray()
    )

    val averageLoss = batchLoss(
      *(theta.contents.zip(weights.contents) + consts).toTypedArray()
    ).toDouble() / batch.rows.size

    val gradients = batchLoss.d(theta)(
      *(theta.contents.zip(weights.contents) + consts).toTypedArray()
    )()

    weights = (weights - alpha * gradients)() // Vanilla SGD

    if (epochs % 100 == 0 && 0 < epochs) {
      println("Average loss at ${epochs / 100} epochs: ${totalLoss / 100}")
      lossHistory += epochs / 100 to totalLoss / 100
      totalLoss = 0.0
    }

    totalLoss += averageLoss
    epochs++
  } while (epochs < 20000)

  println("Final weights: $weights")
  println("Target coefficients: $hiddenWeights")

  mapOf(
    "Epochs" to lossHistory.map { it.first },
    "Average Loss" to lossHistory.map { it.second }
  ).plot2D("Loss over time", "Epochs", "linear_regression_loss.svg")
}