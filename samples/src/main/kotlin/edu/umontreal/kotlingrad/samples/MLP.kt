package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import kotlin.random.Random

@Suppress("NonAsciiCharacters")
class MLP<T: SFun<T>>(
  val x: Var<T> = Var("x"),
  val y: Var<T> = Var("y"),
  val p1v: VVar<T, D3> = VVar("p1v", D3),
  val p2v: MVar<T, D3, D3> = MVar(),
  val p3v: VVar<T, D3> = VVar("p2v", D3)
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
  var w1: VFun<DReal, D3> = Vec(1.0, 1.0, 1.0)

  var w2: MFun<DReal, D3, D3> = Mat3x3(
    1.0, 1.0, 1.0,
    1.0, 1.0, 1.0,
    1.0, 1.0, 1.0
  )

  var w3: VFun<DReal, D3> = Vec(1.0, 1.0, 1.0)

  val oracle = { it: Double -> it * it }
  val drawSample = { Random.nextDouble().let { Pair(it, oracle(it)) } }
  val mlp = MLP<DReal>()
  val mlpf = mlp.asFun()

  var i = 1.0
  var totalLoss = 0.0
  val α = DReal(0.01)

  do {
    val (X, Y) = drawSample()
    val t = mlp(w1, w2, w3)(mlp.x to X, mlp.y to Y)

    totalLoss += t.toDouble()

    val dw1 = mlpf.d(mlp.p1v)
    val dw2 = mlpf.d(mlp.p2v)
    val dw3 = mlpf.d(mlp.p3v)

    val t1 = dw1(mlp.p1v to w1)(mlp.p2v to w2)(mlp.p3v to w3)(mlp.x to X, mlp.y to Y)
    val t2 = dw2(mlp.p1v to w1)(mlp.p2v to w2)(mlp.p3v to w3)(mlp.x to X, mlp.y to Y)
    val t3 = dw3(mlp.p1v to w1)(mlp.p2v to w2)(mlp.p3v to w3)(mlp.x to X, mlp.y to Y)
    val s1 = α * t1
    println(s1)
    w1 += s1
    w2 += α * t2
    w3 += α * t3

    val avgLoss = totalLoss / i
    println(avgLoss)
  } while (i++ < 100 || 0.5 < avgLoss)
}
