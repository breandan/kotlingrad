package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import kotlin.random.Random

@Suppress("NonAsciiCharacters")
class MultilayerPerceptron<T: SFun<T>>(
  val x: Var<T> = Var(),
  val y: Var<T> = Var(),
  val p1v: VVar<T, D3> = VVar(),
  val p2v: MVar<T, D3, D3> = MVar(),
  val p3v: VVar<T, D3> = VVar()
): (VFun<T, D3>,
    MFun<T, D3, D3>,
    VFun<T, D3>) -> SFun<T> {
  override operator fun invoke(
    p1: VFun<T, D3>,
    p2: MFun<T, D3, D3>,
    p3: VFun<T, D3>
  ): SFun<T> {
    val layer1 = layer(p1v * x)
    val layer2 = layer(p2v * layer1)
    val output = layer2 dot p3v
    val lossFun = (output - y) pow Two()
    return lossFun(p1v to p1)(p2v to p2)(p3v to p3)
  }

  private fun sigmoid(x: SFun<T>) = One<T>() / (One<T>() + E<T>().pow(-x))

  private fun layer(x: VFun<T, D3>): VFun<T, D3> = x.map { sigmoid(it) }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) = with(DoublePrecision) {
      var w1: VFun<DReal, D3> = Vec(1.0, 1.0, 1.0)

      var w2: MFun<DReal, D3, D3> = Mat3x3(
        1.0, 1.0, 1.0,
        1.0, 1.0, 1.0,
        1.0, 1.0, 1.0
      )

      var w3: VFun<DReal, D3> = Vec(1.0, 1.0, 1.0)

      val oracle = { it: Double -> it * it }
      val drawSample = { Random.nextDouble().let { Pair(it, oracle(it)) } }
      val mlp = MultilayerPerceptron<DReal>()

      var i = 1.0
      var totalLoss = 0.0
      val α = DReal(0.01)

      do {
        val (X, Y) = drawSample()
        val m = mlp(p1 = w1, p2 = w2, p3 = w3)

        totalLoss += m(mlp.x to X, mlp.y to Y).asDouble()

        val dw1 = m.d(mlp.p1v)
        val dw2 = m.d(mlp.p2v)
        val dw3 = m.d(mlp.p3v)

        w1 += α * dw1(mlp.x to X, mlp.y to Y)
        w2 += α * dw2(mlp.x to X, mlp.y to Y)
        w3 += α * dw3(mlp.x to X, mlp.y to Y)

        val avgLoss = totalLoss / i
        println(avgLoss)
      } while (i++ < 100 || 0.5 < avgLoss)
    }
  }
}