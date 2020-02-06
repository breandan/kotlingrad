package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import java.util.*

fun main() = with(DoublePrecision) {
  val seed = 1L
  val rand = Random(seed)
  val theta = Var9("theta")
  val batchIn = Var9("xIn")
  val bias = Var("bias")
  val batchSize = D9
  val biasVec = Vec<DReal, D9>(List(batchSize.i) { bias })
  val label = Var9("y")

  val encodedInput = Mat<DReal, D9, D9>(batchIn.sVars.contents.map { row -> Vec<DReal, D9>(List(9) { col -> row pow col }) })
  val loss = (encodedInput * theta + biasVec - label).magnitude()
  var weightsNow = Vec(batchSize) { rand.nextDouble() * 10 }
  var biasNow = rand.nextDouble() * 10
  println("Initial weights are: $weightsNow")
  val hiddenWeights = Vec(batchSize) { rand.nextDouble() * 10 }
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
    val noise = Vec(batchSize) { (rand.nextDouble() - 0.5) * 0.1 }
    val xInputs = Vec(D9) { Random(seed + epochs).nextDouble() }
    val xEncoded = Mat<DReal, D9, D9>(xInputs.contents.map { row -> Vec<DReal, D9>(List(9) { col -> row pow col }) })
    val targets = (xEncoded * hiddenWeights).map { it + hiddenBias } + noise

    val batchInputs: Array<Pair<Fun<DReal>, Any>> = arrayOf(batchIn to xInputs, label to targets())
    val batchLoss = loss(*batchInputs)

    weightMap = arrayOf(theta to weightsNow, bias to biasNow)

    val averageLoss = batchLoss(*weightMap).toDouble() / xInputs.size
    val weightGrads = batchLoss.d(theta)
    val biasGrads = batchLoss.d(bias)

    weightsNow = (weightsNow - alpha * weightGrads)(*weightMap)()
    biasNow = (biasNow - alpha * biasGrads)(*weightMap).toDouble()

    if (epochs % 100 == 0) {
      println("Average loss at ${epochs / 100} epochs: ${totalLoss / 100}")
      totalTime -= System.nanoTime()
      println("Average time: " + -totalTime / 100 + "ns")
      lossHistory += epochs / 100 to totalLoss / 100
      totalLoss = 0.0
    }

    totalLoss += averageLoss
  } while (epochs++ % 100 < 100)

  println("Final weights: $weightsNow, bias: $biasNow")
  println("Target coefficients: $hiddenWeights, $hiddenBias")

  mapOf(
    "Epochs" to lossHistory.map { it.first },
    "Average Loss" to lossHistory.map { it.second }
  ).plot2D("Training Loss", "linear_regression_loss.svg")
}