package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.shapes.*

fun main() {
  val f = x pow 2
  println(f(x to 3.0))
  println("f(x) = $f")
  val df_dx = f.d(x)
  println("f'(x) = $df_dx")

  val g = x pow x
  println("g(x) = $g")
  val dg_dx = g.d(x)
  println("g'(x) = $dg_dx")

  val h = x + y
  println("h(x) = $h")
  val dh_dx = h.d(x)
  println("h'(x) = $dh_dx")

  val vf1 = Vec(y + x, y * 2)
  val bh = x * vf1
  val vf2 = Vec(x, y)
  val q = vf1 + vf2
  val r = q(x to 1.0, y to 2.0)
  println("r: $r")

  val mf1 = Mat2x1(y * y, x * y)

  val mf2 = Mat1x2(vf2)

  val qr = mf2 * Vec(x, y)

  val mf3 = Mat3x2(x, x, y, x, x, x)
  val mf4 = Mat2x2(vf2, vf2)
  val mf5 = Mat2x2(
    y * y, x * x,
    x * y, y * y
  )
  val mf6 = mf4 * mf5 * mf1

  println(mf1 * mf2) // 2*1 x 1*2
//    println(mf1 * vf1) // 2*1 x 2
  println(mf2 * vf1) // 1*2 x 2
  println(mf3 * vf1) // 3*2 x 2
//    println(mf3 * mf3) // 3*2 x 3*2
}