package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.utils.step
import kotlin.math.absoluteValue
import kotlin.random.Random

fun main() = with(DoublePrecision) {
  val seed = 2L
  val rand = Random(seed)
  val theta = Var9("theta")
  val xBatchIn = Var9("xBatchIn")
  val batchSize = D9
  val label = Var9("y")

  val eg = ExpressionGenerator(rand)
  val maxX = 1.0
  val maxY = 1.0
  val targetEq = eg.scaledRandomBiTree(4, maxX, maxY)
  targetEq.show()
  val oracle: (Double) -> Double = { it: Double -> targetEq(x to it).toDouble() }
  plotOracle("oracle.svg", 1.0, oracle)

  val encodedInput = xBatchIn.sVars.vMap { row -> Vec(batchSize) { col -> row pow (col + 1) } }
  val loss = (encodedInput * theta - label).magnitude()
  var weightsNow = Vec(batchSize) { rand.nextDouble() - 0.5 }
  println("Initial weights are: $weightsNow")
  println("Target equation: $targetEq")

  val epochSize = 1000
  var totalLoss = 0.0
  var totalTime = 0L
  val alpha = 0.001
  val lossHistory = mutableListOf<Pair<Int, Double>>()
  var weightMap: Array<Pair<Fun<DReal>, Any>>

  for(epochs in 1..(epochSize * 100)) {
    totalTime = System.currentTimeMillis()
    val noise = Vec(batchSize) { (rand.nextDouble() - 0.5) * 0.1 }
    val xInputs = Vec(batchSize) { Random(seed + epochs).nextDouble() * 2 * maxX - maxX }
    val targets = xInputs.map { row -> targetEq(row) } + noise

    val batchInputs: Array<Pair<Fun<DReal>, Any>> =
      arrayOf(xBatchIn to xInputs, label to targets())
    val batchLoss = loss(*batchInputs)

    weightMap = arrayOf(theta to weightsNow)

    val averageLoss = batchLoss(*weightMap).toDouble() / xInputs.size
    val weightGrads = batchLoss.d(theta)

    weightsNow = (weightsNow - alpha * weightGrads)(*weightMap)()

    if (epochs % epochSize == 0) {
      plotVsOracle(oracle, Vec(D9) { x } dot weightsNow, x)
      println("Average loss at ${epochs / epochSize} epochs: ${totalLoss / epochSize}")
      totalTime -= System.currentTimeMillis()
      println("Average time: " + -totalTime / 100 + "ms")
      lossHistory += epochs / 100 to totalLoss / 100
      totalLoss = 0.0
    }

    totalLoss += averageLoss
  }

  println("Final weights: $weightsNow")

  mapOf(
    "Epochs" to lossHistory.map { it.first },
    "Average Loss" to lossHistory.map { it.second }
  ).plot2D("Training Loss", "linear_regression_loss.svg")
}

class ExpressionGenerator(val rng: Random): Protocol<DReal>(DReal) {
  val sum = { x: SFun<DReal>, y: SFun<DReal> -> x + y }
  val sub = { x: SFun<DReal>, y: SFun<DReal> -> x - y }
  val mul = { x: SFun<DReal>, y: SFun<DReal> -> x * y }
//  val div = { x: SFun<DReal>, y: SFun<DReal> -> x / y }

  val operators = listOf(sum, sub, mul)
  override val variables = listOf(x)

  infix fun SFun<DReal>.wildOp(that: SFun<DReal>) = operators.random(rng)(this, that)

  fun randomBiTree(height: Int = 5): SFun<DReal> =
    if (height == 0) (listOf(wrap(rng.nextDouble())) + variables).random(rng)
    else randomBiTree(height - 1) wildOp randomBiTree(height - 1)

  fun scaledRandomBiTree(height: Int, maxX: Double = 1.0, maxY: Double = 1.0) =
      randomBiTree(height).let { it - it.invoke(0.0) }.let {
        it * wrap(maxY) / ((-maxX..maxX) step 0.01).toList()
          .map { num -> it.invoke(num).toDouble().absoluteValue }.max()!!
      }
}

private fun plotOracle(filename: String, maxX: Double = 1.0, oracle: (Double) -> Double) {
  val t = ((-maxX..maxX) step 0.01).toList()
  mapOf( "x" to t, "y" to t.map { oracle(it) }).plot2D("Oracle", filename)
}

private fun DoublePrecision.plotVsOracle(oracle: (Double) -> Double, model: SFun<DReal>, x: SFun<DReal>) {
  val t = ((-1.0..1.0) step 0.01).toList()
  mapOf("x" to t,
    "y" to t.map { oracle(it) },
    "z" to t.map { value -> model(value).toDouble() }
  ).plot2D("Oracle vs. Model", "compare_outputs.svg")
}