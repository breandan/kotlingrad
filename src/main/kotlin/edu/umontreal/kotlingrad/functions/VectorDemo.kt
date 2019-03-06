package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.numerical.DoublePrecision

//infix operator fun <Y: `100`, X: Field<X>> Number.times(fn: VectorFun<X, Y>): VectorFun<X, Y> = fn * ScalarConst(wrap(this))
//infix fun <Y: `100`, X: Field<X>> ScalarFun<X>.mult(fn: VectorFun<X, Y>): VectorFun<X, Y> = fn * this

fun main() {
  with(DoublePrecision) {
    val y = Var("y")

    val t = VectorFun(1 * y, 2 * y, 3 * y)
    val z = VectorFun(1 * y, 2 * y, 3 * y)
    val m = t * z * y
    val q = z * 3
    val r = q * 3

    println(r)
  }
}