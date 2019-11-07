package edu.umontreal.kotlingrad.samples

fun <X: Fun<X>> sigmoid(x: Fun<X>) = One<X>() / (One<X>() + E<X>().pow(-x))

fun <X: Fun<X>> layer(x: VFun<X, D3>) = x.elementwise { sigmoid(it) }

fun main() {
  with(DoublePrecision) {
    val weights1 = Mat3x3(1.0, 1.0, 1.0,
                          1.0, 1.0, 1.0,
                          1.0, 1.0, 1.0)

    val weights2 = Mat3x3(1.0, 1.0, 1.0,
                          1.0, 1.0, 1.0,
                          1.0, 1.0, 1.0)

    val inputs = Vec(1.0, 2.0, 3.0)

    val layer1 = layer(weights1 * inputs)
    val layer2 = layer(weights2 * layer1)
    val output = Vec(x, x, x) dot Vec(1.0, 2.0, 3.0)

    val gradient = output.grad()

    // TODO: Gradient descent
  }
}