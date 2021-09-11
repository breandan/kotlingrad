package ai.hypergraph.kotlingrad.samples

import ai.hypergraph.kotlingrad.api.*
import ai.hypergraph.kotlingrad.shapes.*
import ai.hypergraph.kotlingrad.utils.step
import kotlin.math.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun <T: SFun<T>> sigmoid(x: SFun<T>) = One<T>() / (One<T>() + E<T>().pow(-x))
fun <T: SFun<T>> tanh(x: SFun<T>) = (E<T>().pow(Two<T>() * x) - One()) / (E<T>().pow(Two<T>() * x) + One())
fun <T: SFun<T>> layer(x: VFun<T, D5>): VFun<T, D5> = x.map { tanh(it) }
fun <T: SFun<T>> mlp(
  x: VVar<T, D5>,
  p1v: VVar<T, D5>, b1: VVar<T, D5>,
  p2v: MVar<T, D5, D5>, b2: VVar<T, D5>,
  p3v: MVar<T, D5, D5>, b3: VVar<T, D5>,
  p4v: VVar<T, D5>, b4: VVar<T, D5>
): VFun<T, D5> {
  val layer1 = layer(p1v ʘ x + b1)
  val layer2 = layer(p2v * layer1 + b2)
  val layer3 = layer(p3v * layer2 + b3)
  val output = layer3 + b4
  return output
}

fun Double.clip(maxUnsignedVal: Double = 3.0) =
  if (maxUnsignedVal < log10(absoluteValue).absoluteValue) sign * 10.0.pow(log10(absoluteValue)) else this

fun main() {
  val p1v by DReal.Var(D5)
  val p2v by DReal.Var(D5, D5)
  val p3v by DReal.Var(D5, D5)
  val p4v by DReal.Var(D5)
  val b1 by DReal.Var(D5)
  val b2 by DReal.Var(D5)
  val b3 by DReal.Var(D5)
  val b4 by DReal.Var(D5)

  val rand = Random.Default
  var w1: VFun<DReal, D5> = DReal.Vec(D5) { rand.nextDouble(-0.6, 0.6) }
  var w2: MFun<DReal, D5, D5> = DReal.Mat(D5, D5) { _, _ -> rand.nextDouble(-0.6, 0.6) }
  var w3: MFun<DReal, D5, D5> = DReal.Mat(D5, D5) { _, _ -> rand.nextDouble(-0.6, 0.6) }
  var w4: VFun<DReal, D5> = DReal.Vec(D5) { rand.nextDouble(-0.6, 0.6) }
  var v1: VFun<DReal, D5> = DReal.Vec(D5) { 0 }
  var v2: VFun<DReal, D5> = DReal.Vec(D5) { 0 }
  var v3: VFun<DReal, D5> = DReal.Vec(D5) { 0 }
  var v4: VFun<DReal, D5> = DReal.Vec(D5) { 0 }

  val oracle = { it: Double -> DReal(-(it / 10).pow(3)) }//kotlin.math.sin(it/2.0) }//(it / 10).pow(2) }//-it / 10 + 1 }
  val sample =
    {
      (1..5).map {
        rand.nextDouble(0.0, 10.0).let { DReal(it) to oracle(it) }
      }.unzip().let { (data, targets) ->
        Vec<DReal, D5>(data) to Vec<DReal, D5>(targets)
      }
    }

  var epochs = 0
  val α = DReal(0.01)
  val lossHistory = mutableListOf<Pair<Int, Double>>()
  val batchIn by DReal.Var(D5)
  val batchOut by DReal.Var(D5)
  val mlp = mlp(batchIn, p1v, b1, p2v, b2, p3v, b3, p4v, b4)
  val batchLoss = (mlp - batchOut).magnitude()

  val dw1 = batchLoss.d(p1v)
  val dw2 = batchLoss.d(p2v)
  val dw3 = batchLoss.d(p3v)
  val dw4 = batchLoss.d(p4v)
  val db1 = batchLoss.d(b1)
  val db2 = batchLoss.d(b2)
  val db3 = batchLoss.d(b3)
  val db4 = batchLoss.d(b4)

  var trainedMLP: VFun<DReal, D5>
  var inputs: Array<Pair<Fun<DReal>, Fun<DReal>>>
  do {
    val (data, targets) = sample()
    inputs = arrayOf(
      p1v to w1, p2v to w2, p3v to w3, p4v to w4,
      b1 to v1, b2 to v2, b3 to v3, b4 to v4,
      batchIn to data, batchOut to targets
    )

    println(Bindings(*inputs).toString())

    measureTimeMillis {
      w1 = (w1 - α * dw1)(*inputs)
      w2 = (w2 - α * dw2)(*inputs)
      w3 = (w3 - α * dw3)(*inputs)
      w4 = (w4 - α * dw4)(*inputs)
      v1 = (v1 - α * db1)(*inputs)
      v2 = (v2 - α * db2)(*inputs)
      v3 = (v3 - α * db3)(*inputs)
      v4 = (v4 - α * db4)(*inputs)

      val loss = batchLoss(*inputs)
//      println("Batch weights: w1:$w1\nw2:$w2\nw3:$w3")
//      println("Batch free variables:" + loss.bindings.allFreeVariables.keys)
      println("Average loss at $epochs epochs: $loss".take(100))
      lossHistory += epochs to loss(*inputs).toDouble()
    }.let { println("Batch time: $it ms") }

    trainedMLP = mlp(*inputs)

    if (epochs % 1 == 0) {
      plotVsOracle(oracle, trainedMLP, batchIn)
      validate(sample, trainedMLP, batchIn)
    }
  } while (epochs++ < 10)

  println("Final weights: w1:$w1\nw2:$w2\nw3:$w3")

  mapOf("Epochs" to lossHistory.map { it.first },
    "Average Loss" to lossHistory.map { it.second }
  ).plot2D("Training Loss", "mlp_loss.svg")
}

private fun plotVsOracle(oracle: (Double) -> DReal, mlp: VFun<DReal, D5>, x: VVar<DReal, D5>) {
  val t = ((-10.0..10.0) step 0.01).toList()
  mapOf(
    "x" to t,
    "y" to t.map { oracle(it).toDouble() },
    "z" to t.map { value -> mlp(x to (1..5).map { value })()[0].toDouble() }
  ).plot2D("Oracle vs. Model", "compare_outputs.svg")
}

fun validate(drawSample: () -> Pair<Vec<DReal, D5>, Vec<DReal, D5>>, mlp: VFun<DReal, D5>, x: VVar<DReal, D5>) {
  val preds = mutableListOf<Double>()
  repeat(10) {
    val (X, Y) = drawSample()
    preds += (mlp - Y).magnitude()(x to X).toDouble().also { println("X: $X\tY: $Y\tY_PRED: $it") }
  }
  val v = (preds.fold(0.0) { i, j -> i + j * j } - preds.sum().pow(2.0) / preds.size) / preds.size
  println("Variance: $v")
}