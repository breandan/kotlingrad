package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import kotlin.random.Random

@Suppress("NonAsciiCharacters")
class MLP<T: SFun<T>>(
  val x: Var<T> = Var("x"),
  val y: Var<T> = Var("y"),
  val p1v: VVar<T, D3> = VVar("p1v", D3),
  val p2v: MVar<T, D3, D3> = MVar("p2v", D3, D3),
  val p3v: VVar<T, D3> = VVar("p3v", D3)
) {
  operator fun invoke(p1: VFun<T, D3>, p2: MFun<T, D3, D3>, p3: VFun<T, D3>) =
    asFun()(p1v to p1)(p2v to p2)(p3v to p3)

  fun asFun(): SFun<T> {
    val layer1 = layer(p1v * x)
    val layer2 = layer(p2v * layer1)
    val output = layer2 dot p3v
    val lossFun = (output - y) pow Two()
    return lossFun
  }

  private fun layer(x: VFun<T, D3>): VFun<T, D3> = x.map { sigmoid(it) }
}

fun <T: SFun<T>> sigmoid(x: SFun<T>) = One<T>() / (One<T>() + E<T>().pow(-x))

fun main() = with(DoublePrecision) {
  val rand = java.util.Random()
  var w1 = Vec(D3) { rand.nextDouble() }
  var w2 = Mat(D3, D3) { rand.nextDouble() }
  var w3 = Vec(D3) { rand.nextDouble() }

  val oracle = { it: Double -> it * it }
  val drawSample = { Random.nextDouble().let { Pair(it, oracle(it)) } }
  val mlp = MLP<DReal>()
  val loss = mlp.asFun()

  var epochs = 1
  val batchSize = 10
  val α = wrap(0.1) / batchSize
  val localHistory = mutableListOf<Pair<Int, Double>>()

  println("Starting...")
  do {
    var totalTime = System.nanoTime()
    var batchLoss = 0.0
    var avgLoss = 0.0
    var evalCount = 0
    var t1: VFun<DReal, D3> = Vec(D3) { 0 }
    var t2: MFun<DReal, D3, D3> = Mat(D3, D3) { 0 }
    var t3: VFun<DReal, D3> = Vec(D3) { 0 }
    do {
      val (X, Y) = drawSample()
      val inputs = arrayOf<Pair<SFun<DReal>, SFun<DReal>>>(mlp.x to wrap(X), mlp.y to wrap(Y)) + constants
      println("Evaluating sample loss")
      val sampleLoss = loss(*inputs)
      val closure = (mlp.p2v.flatContents.mapIndexed { i, it -> it to w2.flatContents[i] } +
        mlp.p3v.contents.mapIndexed { i, it -> it to w3[i] } +
        mlp.p1v.contents.mapIndexed { i, it -> it to w1[i] }).toTypedArray()

      val dw1 = sampleLoss.d(mlp.p1v)
      val dw2 = sampleLoss.d(mlp.p2v)
      val dw3 = sampleLoss.d(mlp.p3v)

      println("Evaluating derivatives: $dw1")
      val ew1 = dw1(*closure)
      val ew2 = dw2(*closure)
      val ew3 = dw3(*closure)

      t1 += ew1
      t2 += ew2
      t3 += ew3
      batchLoss += sampleLoss(*closure).toDouble()
      println(batchLoss)
    } while (evalCount++ < batchSize)

    w1 = (w1 - α * t1)()
    w2 = (w2 - α * t2)()
    w3 = (w3 - α * t3)()

    avgLoss /= batchSize
    totalTime -= -System.nanoTime()
    println("Average loss at $epochs epochs: $avgLoss")
    println("Average time: " + totalTime / 100 + "ns")
    localHistory += epochs to avgLoss
  } while (epochs++ < 1000)
}
