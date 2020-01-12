package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import kotlin.random.Random

fun <X: Fun<X>> sigmoid(x: Fun<X>) = One<X>() / (One<X>() + E<X>().pow(-x))

fun <X: Fun<X>, D: D1> layer(x: VFun<X, D>): VFun<X, D> = x.map { sigmoid(it) }

fun main() {
  val XY = { Random.nextDouble().let { Pair(it, it * it) } }

  with(DoublePrecision) {
    var weights1 = Mat3x3(
      1.0, 1.0, 1.0,
      1.0, 1.0, 1.0,
      1.0, 1.0, 1.0
    )

    var weights2 = Mat3x3(
      1.0, 1.0, 1.0,
      1.0, 1.0, 1.0,
      1.0, 1.0, 1.0
    )

    var i = 0.0
    var totalLoss = 0.0

    while(i < 1000 || 0.5 < totalLoss / i) {
      val (X, Y) = XY()
      val inputs = Vec(1.0)
      val target = 1.0

      val w1 = Var3()
      val w2 = Var3x3()
      val w3 = Var3x3()
      val w4 = Var3()

      val layer1 = layer(w1 * inputs)
      val layer2 = layer(w2 * layer1)
      val layer3 = layer(w3 * layer2)
      val output = layer3 dot w4

      val loss = (output - target) pow 2.0
      totalLoss += loss(x to X, y to Y).asDouble()
      i++
//      val dl_dw1 = loss.d(w1)//(Bindings(w1.contents.zip(weights1.contents).toMap()))
    }

    // TODO: Gradient descent
  }
}