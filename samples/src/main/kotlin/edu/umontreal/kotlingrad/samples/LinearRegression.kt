package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import java.util.*

fun main() = with(DoublePrecision) {
  val rand = Random()
  val t1 = Var("t1"); val t2 = Var("t2")
  val theta = Vec(t1, t2)
  val i1 = Var("i1"); val i2 = Var("i2"); val i3 = Var("i3"); val i4 = Var("i4"); val i5 = Var("i5"); val i6 = Var("i6")
  val input = Mat3x2(i1, i2, i3, i4, i5, i6)
  val y1 = Var("y1"); val y2 = Var("y2"); val y3 = Var("y3")
  val label: Vec<DReal, D3> = Vec(y1, y2, y3)

  val loss = (input * theta - label).magnitude()

  var weights = Vec(rand.nextDouble() * 10, rand.nextDouble() * 10)
  println("Initial weights are: $weights")
  var totalLoss = 0.0
  var epochs = 0.0
  val alpha = wrap(0.001)

  val hiddenWeights = Vec(rand.nextDouble() * 10, rand.nextDouble() * 10)
  println("Target coefficients: $hiddenWeights")
  val consts = constants.entries.map { it.key to wrap(it.value) }

  val lossHistory = mutableListOf<Pair<Double, Double>>()

  do {
    val batch = Mat3x2(
      rand.nextDouble(), rand.nextDouble(),
      rand.nextDouble(), rand.nextDouble(),
      rand.nextDouble(), rand.nextDouble()
    )
    val targets = (batch * hiddenWeights)()

    val fixInputs = loss.invoke(
      *(input.flatContents.mapIndexed { i, it -> it to batch.flatContents[i] } +
        label.contents.mapIndexed { i, it -> it to targets[i] }).toTypedArray()
    )

    val batchLoss = loss.invoke(
      *(input.flatContents.zip(batch.flatContents) +
        label.contents.zip(targets.contents) +
        theta.contents.zip(weights.contents) + consts).toTypedArray()
    )

    val dv1 = fixInputs.d(t1)(t1 to weights[0], t2 to weights[1])
    val dv2 = fixInputs.d(t2)(t1 to weights[0], t2 to weights[1])

    val deltas: Vec<DReal, D2> = Vec(dv1, dv2)(
      *(theta.contents.zip(weights.contents) + consts).toTypedArray()
    )()

    if (epochs % 100 == 0.0 && 0 < epochs) {
      println("Average loss at ${epochs / 100} epochs: ${totalLoss / 100}")
      lossHistory += epochs / 100 to totalLoss / 100
      totalLoss = 0.0
    }

    weights = (weights - alpha * deltas)()
    totalLoss += batchLoss.toDouble()
    epochs++
  } while (epochs < 20000)

  println("Final weights: $weights")

  mapOf("x" to lossHistory.map { it.first }, "y" to lossHistory.map { it.second }).plot("Loss over time", "linear_regression_loss.svg")
}