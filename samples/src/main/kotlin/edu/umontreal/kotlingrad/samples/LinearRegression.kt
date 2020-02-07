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
  var biasNow = rand.nextDouble() * 10
  println("Initial weights are: $weightsNow")
  val hiddenWeights = Vec(D2) { rand.nextDouble() * 10 }
  val hiddenBias = rand.nextDouble() * 10
  println("Target coefficients: $hiddenWeights")

  var totalLoss = 0.0
  val epochSize = 1000
  var totalTime = 0L
  val alpha = 0.001
  val lossHistory = mutableListOf<Pair<Int, Double>>()
  var weightMap: Array<Pair<Fun<DReal>, Any>>

  loss.saveToFile("test.dot")

  for(epochs in 1..(epochSize * 100)) {
    totalTime = System.currentTimeMillis()
    val noise = Vec(D3) { rand.nextDouble() - 0.5 }
    val batch = Mat(D3, D2) { _, _ -> rand.nextDouble() }
    val targets = (batch * hiddenWeights).map { it + hiddenBias } + noise

    val batchInputs: Array<Pair<Fun<DReal>, Any>> = arrayOf(input to batch, label to targets())
    val batchLoss = loss(*batchInputs)

    weightMap = arrayOf(theta to weightsNow, bias to biasNow)

    val averageLoss = batchLoss(*weightMap).toDouble() / batch.rows.size
    val weightGrads = batchLoss.d(theta)
    val biasGrads = batchLoss.d(bias)

    weightsNow = (weightsNow - alpha * weightGrads)(*weightMap)()
    biasNow = (biasNow - alpha * biasGrads)(*weightMap).toDouble()

    if (epochs % epochSize == 0) {
      println("Average loss at ${epochs / epochSize} epochs: ${totalLoss / epochSize}")
      totalTime -= System.currentTimeMillis()
      println("Average time: " + -totalTime / 100 + "ms")
      lossHistory += epochs / 100 to totalLoss / 100
      totalLoss = 0.0
    }

    totalLoss += averageLoss
  }

  println("Final weights: $weightsNow, bias: $biasNow")
  println("Target coefficients: $hiddenWeights, $hiddenBias")

  mapOf(
    "Epochs" to lossHistory.map { it.first },
    "Average Loss" to lossHistory.map { it.second }
  ).plot2D("Training Loss", "linear_regression_loss.svg")
}