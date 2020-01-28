package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.experimental.DoublePrecision.toDouble
import edu.umontreal.kotlingrad.utils.step
import kotlin.math.*
import kotlin.random.Random

fun <T: SFun<T>> sigmoid(x: SFun<T>) = One<T>() / (One<T>() + E<T>().pow(-x))
fun <T: SFun<T>> layer(x: VFun<T, D3>): VFun<T, D3> = x.map { sigmoid(it) }
fun <T: SFun<T>> buildMLP(
  x: Var<T> = Var("x"),
  p1v: VVar<T, D3> = VVar("p1v", D3),
  p2v: Mat<T, D3, D3> = MVar("p2v", D3, D3),
  p3v: VVar<T, D3> = VVar("p3v", D3)
): SFun<T> {
  val layer1 = layer(p1v * x)
  val layer2 = layer(p2v * layer1)
  val output = sigmoid(layer2 dot p3v)
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
  val p2v = Mat(D3, D3) { r, c -> if(c == 3) wrap(1.0) else Var() } //Add column of biases
  val p3v = Var3("p3v")
//  val p1v = Var3("p1v")
//  val p2v = Mat(D3, D3) { r, c -> if(c == 3) wrap(1.0) else Var() } //Add column of biases
//  val p3v = Var3("p3v")

  val rand = java.util.Random()
  var w1 = Vec(D3) { rand.nextDouble() }
  var w2 = Mat(D3, D3) { _, _ -> rand.nextDouble() }
  var w3 = Vec(D3) { rand.nextDouble() }

  val oracle = { it: Double -> it }
  val drawSample = { Random.nextDouble().let { Pair(it, oracle(it)) } }
  val mlp = buildMLP(x, p1v, p2v, p3v)

  var epochs = 1
  val batchSize = 10
  val α = wrap(0.1) / batchSize
  val lossHistory = mutableListOf<Pair<Int, Double>>()

  println("Starting...")
  do {
    var totalTime = System.nanoTime()
    var batchLoss = 0.0
    var evalCount = 0
    var t1: VFun<DReal, D3> = Vec(D3) { 0 }
    var t2: MFun<DReal, D3, D3> = Mat(D3, D3) { _, _ -> 0 }
    var t3: VFun<DReal, D3> = Vec(D3) { 0 }
    do {
      val (X, Y) = drawSample()
      val inputs = arrayOf<Pair<SFun<DReal>, SFun<DReal>>>(x to wrap(X), y to wrap(Y)) + constants
      val sampleLoss = pow(mlp(*inputs) - wrap(Y), 2)
      val closure = (p2v.flatContents.mapIndexed { i, it -> it to w2.flatContents[i] } + constants +
        p3v.contents.mapIndexed { i, it -> it to w3[i] } +
        p1v.contents.mapIndexed { i, it -> it to w1[i] }).toTypedArray()

      val dw1 = sampleLoss.d(p1v)
      val dw2 = sampleLoss.d(p2v)
      val dw3 = sampleLoss.d(p3v)


      val ew1 = try {dw1.invoke(*closure) } catch(e: Exception) {
        dw1.show()
        throw e
      }
      val ew2 = dw2(*closure)
      val ew3 = dw3(*closure)


      t1 += ew1
      t2 += ew2
      t3 += ew3

      batchLoss += sampleLoss(*closure).toDouble()
      if(epochs % 2 == 0 && evalCount % 5 == 0) println("X: $X\tY: $Y\tY_PRED: ${mlp(*(closure + inputs))()}")
    } while (evalCount++ < batchSize)

//    println("T1: $t1")
//    println("T2: $t2")
//    println("T3: $t3")

    w1 = (w1 - α * t1)(*constants)()
    w2 = (w2 - α * t2)(*constants)()
    w3 = (w3 - α * t3)(*constants)()
//    println("Final weights: w1:$w1\nw2:$w2\nw3:$w3")

    batchLoss /= batchSize
    totalTime -= -System.nanoTime()
    println("Average loss at $epochs epochs: $batchLoss")
    println("Average time: " + totalTime / 100 + "ns")
    lossHistory += epochs to batchLoss
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
        p3v.contents.mapIndexed { i, it -> it to w3[i] } +
        p1v.contents.mapIndexed { i, it -> it to w1[i] } + constants ).toTypedArray()

      mlp(*closure).toDouble().also { println("$value,$it")}
    }
  ).plot2D("Oracle vs. Model", "compare_outputs.svg")
}
