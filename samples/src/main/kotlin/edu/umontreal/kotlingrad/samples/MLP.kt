package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.utils.step
import kotlin.math.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun <T: SFun<T>> sigmoid(x: SFun<T>) = One<T>() / (One<T>() + E<T>().pow(-x))
fun <T: SFun<T>> tanh(x: SFun<T>) = (E<T>().pow(Two<T>() * x) - One()) / (E<T>().pow(Two<T>() * x) + One())
fun <T: SFun<T>> layer(x: VFun<T, D5>): VFun<T, D5> = x.map { tanh(it) }
fun <T: SFun<T>> mlp(
  x: SVar<T>,
  p1v: VVar<T, D5>, b1: VVar<T, D5>,
  p2v: MVar<T, D5, D5>, b2: VVar<T, D5>,
  p3v: MVar<T, D5, D5>, b3: VVar<T, D5>,
  p4v: VVar<T, D5>, b4: VVar<T, D5>
): SFun<T> {
  val layer1 = layer(p1v * x + b1)
  val layer2 = layer(p2v * layer1 + b2)
  val layer3 = layer(p3v * layer2 + b3)
  val output = layer2 dot p4v + b4
  return output
}

fun Double.clip(maxUnsignedVal: Double = 3.0) =
  if (maxUnsignedVal < log10(absoluteValue).absoluteValue) sign * 10.0.pow(log10(absoluteValue)) else this

fun main() {
  val x by DReal.Var()
  val y by DReal.Var()
  val p1v by DReal.Var(D5)
  val p2v by DReal.Var(D5, D5)
  val p3v by DReal.Var(D5, D5)
  val p4v by DReal.Var(D5)
  val b1 by DReal.Var(D5)
  val b2 by DReal.Var(D5)
  val b3 by DReal.Var(D5)
  val b4 by DReal.Var(D5)

  val rand = Random(1)
  var w1: VFun<DReal, D5> = DReal.Vec(D5) { rand.nextDouble(-0.6, 0.6) }
  var w2: MFun<DReal, D5, D5> = DReal.Mat(D5, D5) { _, _ -> rand.nextDouble(-0.6, 0.6) }
  var w3: MFun<DReal, D5, D5> = DReal.Mat(D5, D5) { _, _ -> rand.nextDouble(-0.6, 0.6) }
  var w4: VFun<DReal, D5> = DReal.Vec(D5) { rand.nextDouble(-0.6, 0.6) }
  var v1: VFun<DReal, D5> = DReal.Vec(D5) { 0 }
  var v2: VFun<DReal, D5> = DReal.Vec(D5) { 0 }
  var v3: VFun<DReal, D5> = DReal.Vec(D5) { 0 }
  var v4: VFun<DReal, D5> = DReal.Vec(D5) { 0 }

  val oracle = { it: Double -> -(it / 10).pow(3) }//kotlin.math.sin(it/2.0) }//(it / 10).pow(2) }//-it / 10 + 1 }
  val drawSample = { rand.nextDouble(0.0, 10.0).let { it to oracle(it) } }
  val mlp = mlp(x, p1v, b1, p2v, b2, p3v, b3, p4v, b4)

  var epochs = 0
  val batchSize = 10
  val α = DReal(0.01)
  val lossHistory = mutableListOf<Pair<Int, Double>>()

  mlp.show()
  var trainedMLP: SFun<DReal>
  var weights: Array<Pair<Fun<DReal>, Fun<DReal>>>
  do {
    weights = arrayOf(
      p1v to w1, p2v to w2, p3v to w3, p4v to w4,
      b1 to v1, b2 to v2, b3 to v3, b4 to v4)

    EAGER = true

    measureTimeMillis {
      var evalCount = 0
      var batchLoss: SFun<DReal> = DReal(0)
      do {
        val (X, Y) = drawSample()
        val sampleLoss = pow(mlp - Y, 2)
        batchLoss += sampleLoss(x to X, y to Y)
      } while (evalCount++ < batchSize)

      batchLoss = batchLoss.sqrt()

      val dw1 = batchLoss.d(p1v)
      val dw2 = batchLoss.d(p2v)
      val dw3 = batchLoss.d(p3v)
      val dw4 = batchLoss.d(p4v)
      val db1 = batchLoss.d(b1)
      val db2 = batchLoss.d(b2)
      val db3 = batchLoss.d(b3)
      val db4 = batchLoss.d(b4)

      w1 = (w1 - α * dw1)(*weights)
      w2 = (w2 - α * dw2)(*weights)
      w3 = (w3 - α * dw3)(*weights)
      w4 = (w4 - α * dw4)(*weights)
      v1 = (v1 - α * db1)(*weights)
      v2 = (v2 - α * db2)(*weights)
      v3 = (v3 - α * db3)(*weights)
      v4 = (v4 - α * db4)(*weights)

      batchLoss = batchLoss(*weights)

      println("Batch weights: w1:$w1\nw2:$w2\nw3:$w3")
      println("Batch free variables:" + batchLoss.bindings.allFreeVariables.keys)
      println("Average loss at $epochs epochs: $batchLoss".take(100))
      lossHistory += epochs to batchLoss.toDouble()
    }.let { println("Batch time: $it ms") }

    trainedMLP = mlp(*weights)

    if (epochs % 1 == 0) {
      plotVsOracle(oracle, trainedMLP, x)
      validate(drawSample, trainedMLP, x)
    }
  } while (epochs++ < 10)

  println("Final weights: w1:$w1\nw2:$w2\nw3:$w3")

  mapOf("Epochs" to lossHistory.map { it.first },
    "Average Loss" to lossHistory.map { it.second }
  ).plot2D("Training Loss", "mlp_loss.svg")

  plotVsOracle(oracle, trainedMLP, x)
}

private fun plotVsOracle(oracle: (Double) -> Double, mlp: SFun<DReal>, x: SFun<DReal>) {
  val t = ((-10.0..10.0) step 0.01).toList()
  mapOf(
    "x" to t,
    "y" to t.map { oracle(it) },
    "z" to t.map { value -> mlp(x to value).toDouble() }
  ).plot2D("Oracle vs. Model", "compare_outputs.svg")
}

fun validate(drawSample: () -> Pair<Double, Double>, mlp: SFun<DReal>, x: SFun<DReal>) {
  val preds = mutableListOf<Double>()
  repeat(10) {
    val (X, Y) = drawSample()
    preds += mlp(x to X).toDouble().also { println("X: $X\tY: $Y\tY_PRED: $it") }
  }
  val v = (preds.fold(0.0) { i, j -> i + j * j } - preds.sum().pow(2.0) / preds.size) / preds.size
  println("Variance: $v")
}