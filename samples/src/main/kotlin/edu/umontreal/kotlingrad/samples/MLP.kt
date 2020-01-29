package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.utils.step
import kotlin.math.absoluteValue
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sign
import kotlin.random.Random

fun <T: SFun<T>> sigmoid(x: SFun<T>) = One<T>() / (One<T>() + E<T>().pow(-x))
fun <T: SFun<T>> tanh(x: SFun<T>) = (E<T>().pow(Two<T>() * x) - One()) / (E<T>().pow(Two<T>() * x) + One())
fun <T: SFun<T>> layer(x: VFun<T, D5>): VFun<T, D5> = x.map { tanh(it) }
fun <T: SFun<T>> buildMLP(
  x: Var<T> = Var("x"),
  p1v: VVar<T, D5> = VVar("p1v", D5), b1: VVar<T, D5> = VVar("b1", D5),
  p2v: Mat<T, D5, D5> = MVar("p2v", D5, D5), b2: VVar<T, D5> = VVar("b2", D5),
  p3v: Mat<T, D5, D5> = MVar("p3v", D5, D5), b3: VVar<T, D5> = VVar("b3", D5),
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
  var w1 = Vec(D5) { rand.nextDouble(-0.6, 0.6) }
  var w2 = Mat(D5, D5) { _, _ -> rand.nextDouble(-0.6, 0.6) }
  var w3 = Mat(D5, D5) { _, _ -> rand.nextDouble(-0.6, 0.6) }
  var w4 = Vec(D5) { rand.nextDouble(-0.6, 0.6) }
  var v1 = Vec(D5) { 0 }
  var v2 = Vec(D5) { 0 }
  var v3 = Vec(D5) { 0 }
  var v4 = Vec(D5) { 0 }

  val oracle = { it: Double -> -(it / 10).pow(3)  }//kotlin.math.sin(it/2.0) }//(it / 10).pow(2) }//-it / 10 + 1 }
  val drawSample = { rand.nextDouble(0.0, 10.0).let { Pair(it, oracle(it)) } }
  val mlp = buildMLP(x, p1v, b1, p2v, b2, p3v, b3, p4v, b4)

  var epochs = 0
  val batchSize = 10
  val α = wrap(0.01)
  val lossHistory = mutableListOf<Pair<Int, Double>>()

  mlp.show()
  var closure: Array<Pair<SFun<DReal>, SFun<DReal>>> = arrayOf()
  do {
    var totalTime = System.nanoTime()
    closure = (p1v.contents.zip(w1.contents) +
      p2v.flatContents.zip(w2.flatContents) +
      p3v.flatContents.zip(w3.flatContents) +
      p4v.contents.zip(w4.contents) +
      b1.contents.zip(v1.contents) +
      b2.contents.zip(v2.contents) +
      b3.contents.zip(v3.contents) +
      b4.contents.zip(v4.contents)).toTypedArray() + constants

    var evalCount = 0
    var batchLoss = wrap(0)
    do {
      val (X, Y) = drawSample()
      val sampleLoss = pow(mlp - wrap(Y), 2)
      batchLoss += sampleLoss(x to wrap(X), y to wrap(Y))
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

    w1 = (w1 - α * dw1)(*closure)()
    w2 = (w2 - α * dw2)(*closure)()
    w3 = (w3 - α * dw3)(*closure)()
    w4 = (w4 - α * dw4)(*closure)()
    v1 = (v1 - α * db1)(*closure)()
    v2 = (v2 - α * db2)(*closure)()
    v3 = (v3 - α * db3)(*closure)()
    v4 = (v4 - α * db4)(*closure)()

    batchLoss = batchLoss(*closure)
    totalTime -= -System.nanoTime()

    if (epochs % 10 == 0) {
      println(p1v)
      plotVsOracle(oracle, closure, x, mlp)
      validate(drawSample, x, mlp, closure)
    }

    println("Average loss at $epochs epochs: $batchLoss")
    println("Average time: " + totalTime / 100 + "ns")
    lossHistory += epochs to batchLoss.toDouble()
  } while (epochs++ < 100)

  println("Final weights: w1:$w1\nw2:$w2\nw3:$w3")

  mapOf(
    "Epochs" to lossHistory.map { it.first },
    "Average Loss" to lossHistory.map { it.second }
  ).plot2D("Training Loss", "mlp_loss.svg")

  plotVsOracle(oracle, closure, x, mlp)
}

private fun DoublePrecision.plotVsOracle(
  oracle: (Double) -> Double,
  closure: Array<Pair<SFun<DReal>, SFun<DReal>>>, x: Var<DReal>,
  mlp: SFun<DReal>
) {
  val t = ((-10.0..10.0) step 0.01).toList()
  mapOf(
    "x" to t,
    "y" to t.map { oracle(it) },
    "z" to t.map { value ->
      val inputs = closure + arrayOf<Pair<SFun<DReal>, SFun<DReal>>>(x to wrap(value))
      mlp(*inputs).toDouble()
    }
  ).plot2D("Oracle vs. Model", "compare_outputs.svg")
}

fun DoublePrecision.validate(
  drawSample: () -> Pair<Double, Double>,
  x: Var<DReal>,
  mlp: SFun<DReal>,
  closure: Array<Pair<SFun<DReal>, SFun<DReal>>>) {
  val preds = mutableListOf<Double>()
  repeat(10) {
    val (X, Y) = drawSample()
    val inputs = arrayOf<Pair<SFun<DReal>, SFun<DReal>>>(x to wrap(X))
//        mlp(*inputs).show()
    val pred = mlp(*(closure + inputs))().toDouble()
    preds += pred
    println("X: $X\tY: $Y\tY_PRED: $pred")
  }
  val v = (preds.fold(0.0) { i, j -> i + j * j } - preds.sum().pow(2.0) / preds.size) / preds.size
  println("Variance: $v")
}
