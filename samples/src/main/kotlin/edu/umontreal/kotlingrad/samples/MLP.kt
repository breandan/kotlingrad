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
fun <T: SFun<T>> layer(x: VFun<T, D3>): VFun<T, D3> = x.map { tanh(it) }
fun <T: SFun<T>> buildMLP(
  x: Var<T> = Var("x"),
  p1v: VVar<T, D3> = VVar("p1v", D3), b1: VVar<T, D3> = VVar("b1", D3),
  p2v: Mat<T, D3, D3> = MVar("p2v", D3, D3), b2: VVar<T, D3> = VVar("b2", D3),
  p3v: Mat<T, D3, D3> = MVar("p3v", D3, D3), b3: VVar<T, D3> = VVar("b3", D3),
  p4v: VVar<T, D3> = VVar("p4v", D3), b4: VVar<T, D3> = VVar("b4", D3)
): SFun<T> {
  val layer1 = layer(p1v * x + b1)
  val layer2 = p2v * layer1 + b2
  val layer3 = p3v * layer2 + b3
  val output = layer3 dot p4v + b4
  return output
}

fun Double.clip(maxUnsignedVal: Double = 3.0) =
  if (maxUnsignedVal < log10(absoluteValue).absoluteValue) sign * 10.0.pow(log10(absoluteValue)) else this

fun main() = with(DoublePrecision) {
  val x = Var("x")
  val y = Var("y")
  val p1v = Var3("p1v")
  val p2v = Var3x3("p2v")
  val p3v = Var3x3("p3v")
  val p4v = Var3("p5v")
  val b1 = Var3("b1v")
  val b2 = Var3("b2v")
  val b3 = Var3("b3v")
  val b4 = Var3("b4v")

  val rand = Random(1)
  var w1 = Vec(D3) { rand.nextDouble(-0.6, 0.6) }
  var w2 = Mat(D3, D3) { _, _ -> rand.nextDouble(-0.6, 0.6) }
  var w3 = Mat(D3, D3) { _, _ -> rand.nextDouble(-0.6, 0.6) }
  var w4 = Vec(D3) { rand.nextDouble(-0.6, 0.6) }
  var v1 = Vec(D3) { 0 }
  var v2 = Vec(D3) { 0 }
  var v3 = Vec(D3) { 0 }
  var v4 = Vec(D3) { 0 }

  val oracle = { it: Double -> -(it / 10).pow(3)  }//kotlin.math.sin(it/2.0) }//(it / 10).pow(2) }//-it / 10 + 1 }
  val drawSample = { rand.nextDouble(0.0, 10.0).let { Pair(it, oracle(it)) } }
  val mlp = buildMLP(x, p1v, b1, p2v, b2, p3v, b3, p4v, b4)

  var epochs = 0
  val batchSize = 10
  val α = wrap(0.01)
  val lossHistory = mutableListOf<Pair<Int, Double>>()

  mlp.show()

  do {
    var totalTime = System.nanoTime()
    var batchLoss = wrap(0)
    var evalCount = 0
    val closure = (p1v.contents.mapIndexed { i, it -> it to w1[i] } + constants +
        p2v.flatContents.mapIndexed { i, it -> it to w2.flatContents[i] } +
      p3v.flatContents.mapIndexed { i, it -> it to w3.flatContents[i] } +
      p4v.contents.mapIndexed { i, it -> it to w4[i] } +
      b1.contents.mapIndexed { i, it -> it to v1[i] } +
      b2.contents.mapIndexed { i, it -> it to v2[i] } +
      b3.contents.mapIndexed { i, it -> it to v3[i] } +
      b4.contents.mapIndexed { i, it -> it to v4[i] }).toTypedArray()

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
    val dw4 = batchLoss.d(p4v)
    val db1 = batchLoss.d(b1)
    val db2 = batchLoss.d(b2)
    val db3 = batchLoss.d(b3)
    val db4 = batchLoss.d(b4)

    val ew1 = dw1(*closure)
    val ew2 = dw2(*closure)
    val ew3 = dw3(*closure)
    val ew4 = dw4(*closure)
    val cw1 = db1(*closure)
    val cw2 = db2(*closure)
    val cw3 = db3(*closure)
    val cw4 = db4(*closure)

    w1 = (w1 - α * ew1)(*constants)()
    w2 = (w2 - α * ew2)(*constants)()
    w3 = (w3 - α * ew3)(*constants)()
    w4 = (w4 - α * ew4)(*constants)()
    v1 = (v1 - α * cw1)(*constants)()
    v2 = (v2 - α * cw2)(*constants)()
    v3 = (v3 - α * cw3)(*constants)()
    v4 = (v4 - α * cw4)(*constants)()

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

  val closure = (p1v.contents.mapIndexed { i, it -> it to w1[i] } + constants +
    p2v.flatContents.mapIndexed { i, it -> it to w2.flatContents[i] } +
    p3v.flatContents.mapIndexed { i, it -> it to w3.flatContents[i] } +
    p4v.contents.mapIndexed { i, it -> it to w4[i] } +
    b1.contents.mapIndexed { i, it -> it to v1[i] } +
    b2.contents.mapIndexed { i, it -> it to v2[i] } +
    b3.contents.mapIndexed { i, it -> it to v3[i] } +
    b4.contents.mapIndexed { i, it -> it to v4[i] }).toTypedArray()

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
