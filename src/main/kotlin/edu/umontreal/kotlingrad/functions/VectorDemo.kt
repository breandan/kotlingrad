package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.numerical.DoublePrecision
import edu.umontreal.kotlingrad.numerical.DoubleReal

fun main() {
  with(DoublePrecision) {
    val y = Var("y")
    val c = VectorFun(DoubleReal(1), DoubleReal(2), DoubleReal(3))
    val d = VectorFun(DoubleReal(1), DoubleReal(2), DoubleReal(3))
    val f = c dot d
    println("f = $f")
//    val e = c * 1
    val t = VectorFun(1 * y, 2 * y, 3 * y)
    val z = VectorFun(1 * y, 2 * y, 3 * y)
    val l = t dot z
    println("l = $l")
    val m = t * z * y
    println("m = $m")
    val q = z * 3
    val r = 3 * q
    val p = y * y * q
    println("p = $p")
//    p.invoke(y to 1)
  }
}