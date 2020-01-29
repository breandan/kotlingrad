package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.experimental.DoublePrecision.toDouble
import edu.umontreal.kotlingrad.utils.step
import kotlin.math.absoluteValue
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sign
import kotlin.random.Random

fun <T: SFun<T>> sigmoid(x: SFun<T>) = One<T>() / (One<T>() + E<T>().pow(-x))
fun <T: SFun<T>> layer(x: VFun<T, D3>): VFun<T, D3> = x.map { sigmoid(it) }
fun <T: SFun<T>> buildMLP(
  x: Var<T> = Var("x"),
  p1v: VVar<T, D3> = VVar("p1v", D3), b1: VVar<T, D3> = VVar("b1", D3),
  p2v: Mat<T, D3, D3> = MVar("p2v", D3, D3), b2: VVar<T, D3> = VVar("b1", D3),
  p3v: VVar<T, D3> = VVar("p3v", D3), b3: VVar<T, D3> = VVar("b1", D3)
): SFun<T> {
  val layer1 = layer(p1v * x + b1)
  val layer2 = layer(p2v * layer1 + b2)
  val output = layer2 dot p3v + b3
  return output
}

fun Double.clip(maxUnsignedVal: Double = 3.0) =
  if (maxUnsignedVal < log10(absoluteValue).absoluteValue) sign * 10.0.pow(log10(absoluteValue)) else this

fun <E: D1> VFun<DReal, E>.clipGradient() =
  map { DoublePrecision.wrap(it.toDouble().clip()) }

fun <R: D1, C: D1> MFun<DReal, R, C>.clipGradient() =
  map { DoublePrecision.wrap(it.toDouble().clip()) }

fun main() = with(DoublePrecision) {
  val x = Var("x")
  val y = Var("y")
  val p1v = Var3("p1v")
  val p2v = Var3x3("p2v")
  val p3v = Var3("p3v")
  val b1 = Var3("b1v")
  val b2 = Var3("b2v")
  val b3 = Var3("b3v")
//  val p1v = Var3("p1v")
//  val p2v = Mat(D3, D3) { r, c -> if(c == 3) wrap(1.0) else Var() } //Add column of biases
//  val p3v = Var3("p3v")

  val rand = Random(1)
  var w1 = Vec(D3) { rand.nextDouble(-0.6, 0.6) }
  var w2 = Mat(D3, D3) { _, _ -> rand.nextDouble(-0.6, 0.6) }
  var w3 = Vec(D3) { rand.nextDouble(-0.6, 0.6) }
  var v1 = Vec(D3) { 0 }
  var v2 = Vec(D3) { 0 }
  var v3 = Vec(D3) { 0 }

  val oracle = { it: Double -> -it / 10 + 1 }
  val drawSample = { rand.nextDouble(0.0, 10.0).let { Pair(it, oracle(it)) } }
  val mlp = buildMLP(x, p1v, b1, p2v, b2, p3v, b3)

  var epochs = 0
  val batchSize = 20
  val α = wrap(0.05)
  val lossHistory = mutableListOf<Pair<Int, Double>>()

  for (i in (-10.0..10.0) step 0.5) {
    val inputs = arrayOf<Pair<SFun<DReal>, SFun<DReal>>>(x to wrap(i))

    val closure = (p2v.flatContents.mapIndexed { i, it -> it to w2.flatContents[i] } + constants +
      b1.contents.mapIndexed { i, it -> it to v1[i] } +
      b2.contents.mapIndexed { i, it -> it to v2[i] } +
      b3.contents.mapIndexed { i, it -> it to v3[i] } +
      p3v.contents.mapIndexed { i, it -> it to w3[i] } +
      p1v.contents.mapIndexed { i, it -> it to w1[i] }).toTypedArray()

    println("X: $i, Y: $i, Y_PRED: ${mlp(*(closure + inputs))}")
  }

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

//    println("T1: $t1")
//    println("T2: $t2")
//    println("T3: $t3")

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
      validate(drawSample, x, mlp, closure)
    }

    println("Average loss at $epochs epochs: $batchLoss")
    println("Average time: " + totalTime / 100 + "ns")
    lossHistory += epochs to batchLoss.toDouble()
  } while (epochs++ < 200)

  println("Final weights: w1:$w1\nw2:$w2\nw3:$w3")

  mapOf(
    "Epochs" to lossHistory.map { it.first },
    "Average Loss" to lossHistory.map { it.second }
  ).plot2D("Training Loss", "mlp_loss.svg")

  val t = ((0.0..1.0) step 0.01).toList()

  mapOf(
    "x" to t,
    "y" to t.map { oracle(it) },
    "z" to t.map { value ->
      val closure = (
        p2v.flatContents.mapIndexed { i, it -> it to w2.flatContents[i] } +
          arrayOf<Pair<SFun<DReal>, SFun<DReal>>>(x to wrap(value)) +
          b1.contents.mapIndexed { i, it -> it to v1[i] } +
          b2.contents.mapIndexed { i, it -> it to v2[i] } +
          b3.contents.mapIndexed { i, it -> it to v3[i] } +
          p3v.contents.mapIndexed { i, it -> it to w3[i] } +
          p1v.contents.mapIndexed { i, it -> it to w1[i] } + constants).toTypedArray()

      mlp(*closure).toDouble()
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
