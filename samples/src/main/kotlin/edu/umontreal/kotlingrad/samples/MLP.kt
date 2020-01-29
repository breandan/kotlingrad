package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.utils.step
import kotlin.math.absoluteValue
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sign
import kotlin.random.Random

fun <T: SFun<T>> sigmoid(x: SFun<T>) = One<T>() / (One<T>() + E<T>().pow(-x))
fun <T: SFun<T>> layer(x: VFun<T, D5>): VFun<T, D5> = x.map { sigmoid(it) }
fun <T: SFun<T>> buildMLP(
  x: Var<T> = Var("x"),
  p1v: VVar<T, D5> = VVar("p1v", D5), b1: VVar<T, D5> = VVar("b1", D5),
  p2v: Mat<T, D5, D5> = MVar("p2v", D5, D5), b2: VVar<T, D5> = VVar("b1", D5),
  p3v: VVar<T, D5> = VVar("p3v", D5), b3: VVar<T, D5> = VVar("b1", D5)
): SFun<T> {
  val layer1 = layer(p1v * x + b1)
  val layer2 = layer(p2v * layer1 + b2)
  val output = layer2 dot p3v + b3
  return output
}

fun Double.clip(maxUnsignedVal: Double = 3.0) =
  if (maxUnsignedVal < log10(absoluteValue).absoluteValue) sign * 10.0.pow(log10(absoluteValue)) else this

fun main() = with(DoublePrecision) {
  val x = Var("x")
  val y = Var("y")
  val p1v = Var5("p1v")
  val p2v = Var5x5("p2v")
  val p3v = Var5("p5v")
  val b1 = Var5("b1v")
  val b2 = Var5("b2v")
  val b3 = Var5("b5v")

  val rand = Random(1)
  var w1 = Vec(D5) { rand.nextDouble(-0.6, 0.6) }
  var w2 = Mat(D5, D5) { _, _ -> rand.nextDouble(-0.6, 0.6) }
  var w3 = Vec(D5) { rand.nextDouble(-0.6, 0.6) }
  var v1 = Vec(D5) { 0 }
  var v2 = Vec(D5) { 0 }
  var v3 = Vec(D5) { 0 }

  val oracle = { it: Double -> (it / 10).pow(2) }//-it / 10 + 1 }
  val drawSample = { rand.nextDouble(0.0, 10.0).let { Pair(it, oracle(it)) } }
  val mlp = buildMLP(x, p1v, b1, p2v, b2, p3v, b3)

  var epochs = 0
  val batchSize = 5
  val α = wrap(0.05)
  val lossHistory = mutableListOf<Pair<Int, Double>>()

  mlp.show()

  do {
    var totalTime = System.nanoTime()
    var batchLoss = wrap(0)
    var evalCount = 0
    val closure = (p2v.flatContents.mapIndexed { i, it -> it to w2.flatContents[i] } + constants +
      b1.contents.mapIndexed { i, it -> it to v1[i] } +
      b2.contents.mapIndexed { i, it -> it to v2[i] } +
      b3.contents.mapIndexed { i, it -> it to v3[i] } +
      p3v.contents.mapIndexed { i, it -> it to w3[i] } +
      p1v.contents.mapIndexed { i, it -> it to w1[i] }).toTypedArray()

    do {
      val (X, Y) = drawSample()
      val sampleLoss = pow(mlp - wrap(Y), 2)
      val inputs = arrayOf<Pair<SFun<DReal>, SFun<DReal>>>(x to wrap(X), y to wrap(Y)) + constants

      batchLoss += sampleLoss(*inputs)
    } while (evalCount++ < batchSize)

    batchLoss = batchLoss.sqrt()
    val dw1 = batchLoss.d(p1v)
    val dw2 = batchLoss.d(p2v)
    val dw3 = batchLoss.d(p3v)
    val db1 = batchLoss.d(b1)
    val db2 = batchLoss.d(b2)
    val db3 = batchLoss.d(b3)

    val ew1 = dw1(*closure)
    val ew2 = dw2(*closure)
    val ew3 = dw3(*closure)
    val cw1 = db1(*closure)
    val cw2 = db2(*closure)
    val cw3 = db3(*closure)

    w1 = (w1 - α * ew1)(*constants)()
    w2 = (w2 - α * ew2)(*constants)()
    w3 = (w3 - α * ew3)(*constants)()
    v1 = (v1 - α * cw1)(*constants)()
    v2 = (v2 - α * cw2)(*constants)()
    v3 = (v3 - α * cw3)(*constants)()

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

  val closure = (p2v.flatContents.mapIndexed { i, it -> it to w2.flatContents[i] } + constants +
    b1.contents.mapIndexed { i, it -> it to v1[i] } +
    b2.contents.mapIndexed { i, it -> it to v2[i] } +
    b3.contents.mapIndexed { i, it -> it to v3[i] } +
    p3v.contents.mapIndexed { i, it -> it to w3[i] } +
    p1v.contents.mapIndexed { i, it -> it to w1[i] }).toTypedArray()

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
