package edu.umontreal.kotlingrad.samples

fun <X: Fun<X>> sigmoid(x: Fun<X>) = One<X>() / (One<X>() + E<X>().pow(-x))

fun <X: Fun<X>> layer(x: VFun<X, D3>) = x.map { sigmoid(it) }

fun main() {
  with(DoublePrecision) {
    var weights1 = Mat3x3(1.0, 1.0, 1.0,
                          1.0, 1.0, 1.0,
                          1.0, 1.0, 1.0)

    var weights2 = Mat3x3(1.0, 1.0, 1.0,
                          1.0, 1.0, 1.0,
                          1.0, 1.0, 1.0)

    while(true) {
      val inputs = Vec(1.0, 2.0, 3.0)
      val target = 1.0

      val w1 = Var3x3()
      val w2 = Var3x3()
      val w3 = Var3()

      val layer1 = layer(w1 * inputs)
      val layer2 = layer(w2 * layer1)
      val output = layer2 dot w3

      val loss = (output - target) pow 2.0

      val dl_dw1 = loss.d(w1)//(Bindings(w1.contents.zip(weights1.contents).toMap()))
    }

    // TODO: Gradient descent
  }
}