package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.utils.step
import kotlin.math.absoluteValue
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sign
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun <T: SFun<T>> sigmoid(x: SFun<T>) = One<T>() / (One<T>() + E<T>().pow(-x))
fun <T: SFun<T>> tanh(x: SFun<T>) = (E<T>().pow(Two<T>() * x) - One()) / (E<T>().pow(Two<T>() * x) + One())
fun <T: SFun<T>> layer(x: VFun<T, D5>): VFun<T, D5> = x.map { tanh(it) }
fun <T: SFun<T>> mlp(
  x: SVar<T> = SVar("x"),
  p1v: VVar<T, D5> = VVar("p1v", D5), b1: VVar<T, D5> = VVar("b1", D5),
  p2v: MVar<T, D5, D5> = MVar("p2v", D5, D5), b2: VVar<T, D5> = VVar("b2", D5),
  p3v: MVar<T, D5, D5> = MVar("p3v", D5, D5), b3: VVar<T, D5> = VVar("b3", D5),
  p4v: VVar<T, D5> = VVar("p4v", D5), b4: VVar<T, D5> = VVar("b4", D5)
): SFun<T> {
  val layer1 = layer(p1v * x + b1)
  val layer2 = layer(p2v * layer1 + b2)
  val layer3 = layer(p3v * layer2 + b3)
  val output = layer2 dot p4v + b4
  return output
}

fun Double.clip(maxUnsignedVal: Double = 3.0) =
  if (maxUnsignedVal < log10(absoluteValue).absoluteValue) sign * 10.0.pow(log10(absoluteValue)) else this

fun main() = with(DoublePrecision) {
  val x = Var("x")
  val y = Var("y")
  val p1v = Var5("p1v")
  val p2v = Var5x5("p2v")
  val p3v = Var5x5("p3v")
  val p4v = Var5("p5v")
  val b1 = Var5("b1v")
  val b2 = Var5("b2v")
  val b3 = Var5("b3v")
  val b4 = Var5("b4v")

  val rand = Random(1)
  var w1: VFun<DReal, D5> = Vec(D5) { rand.nextDouble(-0.6, 0.6) }
  var w2: MFun<DReal, D5, D5> = Mat(D5, D5) { _, _ -> rand.nextDouble(-0.6, 0.6) }
  var w3: MFun<DReal, D5, D5> = Mat(D5, D5) { _, _ -> rand.nextDouble(-0.6, 0.6) }
  var w4: VFun<DReal, D5> = Vec(D5) { rand.nextDouble(-0.6, 0.6) }
  var v1: VFun<DReal, D5> = Vec(D5) { 0 }
  var v2: VFun<DReal, D5> = Vec(D5) { 0 }
  var v3: VFun<DReal, D5> = Vec(D5) { 0 }
  var v4: VFun<DReal, D5> = Vec(D5) { 0 }

  val oracle = { it: Double -> -(it / 10).pow(3) }//kotlin.math.sin(it/2.0) }//(it / 10).pow(2) }//-it / 10 + 1 }
  val drawSample = { rand.nextDouble(0.0, 10.0).let { Pair(it, oracle(it)) } }
  val mlp = mlp(x, p1v, b1, p2v, b2, p3v, b3, p4v, b4)

  var epochs = 0
  val batchSize = 10
  val α = wrap(0.01)
  val lossHistory = mutableListOf<Pair<Int, Double>>()

  mlp.show()
  var trainedMLP: SFun<DReal>
  var weights: Array<Pair<Fun<DReal>, Fun<DReal>>>
  do {
    weights = arrayOf(
      p1v to w1, p2v to w2, p3v to w3, p4v to w4,
      b1 to v1, b2 to v2, b3 to v3, b4 to v4)

    measureTimeMillis {
      var evalCount = 0
      var batchLoss: SFun<DReal> = wrap(0)
      do {
        val (X, Y) = drawSample()
        val sampleLoss = pow(mlp - Y, 2)
        batchLoss += sampleLoss(x to X, y to Y)
      } while (evalCount++ < batchSize)

      batchLoss = batchLoss.sqrt()
      val dw1 = batchLoss.d(p1v)
      val dw2 = batchLoss.d(p2v)
      println("DW2: " + dw2(*weights).bindings.allFreeVariables().keys)
      val dw3 = batchLoss.d(p3v)
      val dw4 = batchLoss.d(p4v)
      val db1 = batchLoss.d(b1)
      val db2 = batchLoss.d(b2)
      val db3 = batchLoss.d(b3)
      val db4 = batchLoss.d(b4)

      p1v %= w1
      p2v %= w2
      p3v %= w3
      p4v %= w4
      b1 %= v1
      b2 %= v2
      b3 %= v3
      b4 %= v4

      w1 = (w1 - α * dw1)(*weights)(constants)
      val mv= (-dw2 * α + w2)
      w2 = mv(*weights)(constants)
      w3 = (-dw3 * α + w3)(*weights)(constants)
      w4 = (-dw4 * α + w4)(*weights)(constants)
      v1 = (-db1 * α + v1)(*weights)(constants)
      v2 = (-db2 * α + v2)(*weights)(constants)
      v3 = (-db3 * α + v3)(*weights)(constants)
      v4 = (-db4 * α + v4)(*weights)(constants)

      batchLoss = batchLoss(*weights)(constants)

      println("Batch free variables:" + batchLoss.bindings.allFreeVariables().keys)
      println("Average loss at $epochs epochs: $batchLoss".take(100))
      lossHistory += epochs to batchLoss.toDouble()
    }.let { println("Batch time: $it ms") }

    trainedMLP = mlp(*weights)(constants)

    if (epochs % 1 == 0) {
      println(p1v)
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

private fun DoublePrecision.plotVsOracle(oracle: (Double) -> Double, mlp: SFun<DReal>, x: SFun<DReal>) {
  val t = ((-10.0..10.0) step 0.01).toList()
  mapOf(
    "x" to t,
    "y" to t.map { oracle(it) },
    "z" to t.map { value -> mlp(x to value)(constants).toDouble() }
  ).plot2D("Oracle vs. Model", "compare_outputs.svg")
}

fun DoublePrecision.validate(drawSample: () -> Pair<Double, Double>, mlp: SFun<DReal>, x: SFun<DReal>) {
  val preds = mutableListOf<Double>()
  repeat(10) {
    val (X, Y) = drawSample()
    preds += mlp(x to X)(constants).toDouble().also { println("X: $X\tY: $Y\tY_PRED: $it") }
  }
  val v = (preds.fold(0.0) { i, j -> i + j * j } - preds.sum().pow(2.0) / preds.size) / preds.size
  println("Variance: $v")
}