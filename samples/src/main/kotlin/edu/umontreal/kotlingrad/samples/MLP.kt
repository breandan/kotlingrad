package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import kotlin.random.Random

fun <T: SFun<T>> sigmoid(x: SFun<T>) = One<T>() / (One<T>() + E<T>().pow(-x))
fun <T: SFun<T>> layer(x: VFun<T, D3>): VFun<T, D3> = x.map { sigmoid(it) }
fun <T: SFun<T>> buildMLP(
  x: Var<T> = Var("x"),
  y: Var<T> = Var("y"),
  p1v: VVar<T, D3> = VVar("p1v", D3),
  p2v: Mat<T, D3, D3> = MVar("p2v", D3, D3),
  p3v: VVar<T, D3> = VVar("p3v", D3)
): SFun<T> {
  val layer1 = layer(p1v * x)
  val layer2 = layer(p2v * layer1)
  val output = layer2 dot p3v
  val lossFun = (output - y) pow Two()
  return lossFun
}

fun main() = with(DoublePrecision) {
  val x = Var("x")
  val y = Var("y")
  val p1v = Var3("p3v")
  val p2v = Var3x3()
  val p3v = Var3("p3v")

  val rand = java.util.Random()
  var w1 = Vec(D3) { rand.nextDouble() }
  var w2 = Mat(D3, D3) { rand.nextDouble() }
  var w3 = Vec(D3) { rand.nextDouble() }

  val oracle = { it: Double -> it * it }
  val drawSample = { Random.nextDouble().let { Pair(it, oracle(it)) } }
  val mlp = buildMLP(x, y, p1v, p2v, p3v)

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
      val inputs = arrayOf<Pair<SFun<DReal>, SFun<DReal>>>(x to wrap(X), y to wrap(Y)) + constants
      val sampleLoss = mlp(*inputs)
      val closure = (p2v.flatContents.mapIndexed { i, it -> it to w2.flatContents[i] } +
        p3v.contents.mapIndexed { i, it -> it to w3[i] } +
        p1v.contents.mapIndexed { i, it -> it to w1[i] }).toTypedArray()

      val dw1 = sampleLoss.d(p1v)
      val dw2 = sampleLoss.d(p2v)
      val dw3 = sampleLoss.d(p3v)

      val ew1 = dw1(*closure)
      val ew2 = dw2(*closure)
      val ew3 = dw3(*closure)

      t1 = (t1 + ew1)(*constants)
      t2 = (t2 + ew2)(*constants)
      t3 = (t3 + ew3)(*constants)
      batchLoss += sampleLoss(*closure).toDouble()
    } while (evalCount++ < batchSize)

    println("T1: $t1")
    println("T2: $t2")
    println("T3: $t3")

    w1 = (w1 - α * t1)(*constants)()
    w2 = (w2 - α * t2)(*constants)()
    w3 = (w3 - α * t3)(*constants)()

    avgLoss /= batchSize
    totalTime -= -System.nanoTime()
    println("Average loss at $epochs epochs: $avgLoss")
    println("Average time: " + totalTime / 100 + "ns")
    localHistory += epochs to avgLoss
  } while (epochs++ < 1000)
}
