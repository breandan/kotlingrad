package edu.umontreal.kotlingrad.samples

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

//{
//  // Inferred type: Mat<Int, D1, D3>
//  val a = edu.umontreal.kotlingrad.dependent.Mat(
//    edu.umontreal.kotlingrad.dependent.D1,
//    edu.umontreal.kotlingrad.dependent.D3,
//    1,
//    2,
//    3
//  )
//  println("a = $a")
//  // Inferred type: Mat<Int, D3, D1>
//  val b = a.transpose()
//  println("b = $b")
//  // Inferred type: Mat<Int, D1, D1>
//  val c = a * b
//  println("c = ab = $c")
//
//// Does not compile, inner dimension mismatch
////  a * a
////  b * b
//
//// Does not compile, incompatible shape
////  val b_ = Mat(D3, D1, 1, 2)
////  val c_ = Mat(D3, D1, 1, 2, 3, 4)
//
//// Does not compile, incompatible shape
////  val b_ = Mat(D2, D1, 1)
//
//  // Inferred type: Mat<Int, D2, D3>
//  val d = edu.umontreal.kotlingrad.dependent.Mat(
//    edu.umontreal.kotlingrad.dependent.D2,
//    edu.umontreal.kotlingrad.dependent.D3,
//    1,
//    2,
//    3,
//    4,
//    5,
//    6
//  )
//  println("d = $d")
//
//  // Inferred type: Mat<Int, D3, D2>
//  val e = edu.umontreal.kotlingrad.dependent.Mat(
//    edu.umontreal.kotlingrad.dependent.D3,
//    edu.umontreal.kotlingrad.dependent.D2,
//    1,
//    2,
//    3,
//    4,
//    5,
//    6
//  )
//  println("e = $e")
//
//  // Inferred type: Mat<Int, D2, D2>
//  val f = d * e
//  println("f = de = $f")
//
//// Does not compile, inner dimension mismatch
////  e * b
////  f * b
//
//// Does not compile, incompatible size
////  val d_: Mat<Int, D2, D3> = Mat(D2, D3,
////    1, 2, 3,
////    4, 5
////  )
//
//  // Inferred type: Mat<Int, D3, D3>
//  val g = edu.umontreal.kotlingrad.dependent.Mat(
//    edu.umontreal.kotlingrad.dependent.D3,
//    edu.umontreal.kotlingrad.dependent.D3,
//    1,
//    2,
//    3,
//    4,
//    5,
//    6,
//    7,
//    8,
//    9
//  )
//  println("g = $g")
//
//  // Inferred type: Mat<Int, D3, D3>
//  val h = edu.umontreal.kotlingrad.dependent.Mat(
//    edu.umontreal.kotlingrad.dependent.D3,
//    edu.umontreal.kotlingrad.dependent.D3,
//    1,
//    2,
//    3,
//    4,
//    5,
//    6,
//    7,
//    8,
//    9
//  )
//  println("h = $g")
//
//  // Inferred type: Mat<Int, D3, D3>
//  val i = g * h
//  println("i = gh = $i")
//  val j = i * i
//  println("j = ii = $j")
//  val k = i * b
//  println("k = ib = $j")
//
//// Does not compile, inner dimension mismatch
////  i * f
////  i * d
//
//  // Inferred type: Mat<Int, D4, D4>
//  val l = edu.umontreal.kotlingrad.dependent.Mat(
//    edu.umontreal.kotlingrad.dependent.D4,
//    edu.umontreal.kotlingrad.dependent.D4,
//    1,
//    2,
//    3,
//    4,
//    5,
//    6,
//    7,
//    8,
//    9,
//    0,
//    0,
//    0,
//    9,
//    0,
//    0,
//    0
//  )
//  println("l = $l")
//
//  // Inferred type: Mat<Int, D4, D3>
//  val m = edu.umontreal.kotlingrad.dependent.Mat(
//    edu.umontreal.kotlingrad.dependent.D4,
//    edu.umontreal.kotlingrad.dependent.D3,
//    1,
//    1,
//    1,
//    2,
//    2,
//    2,
//    3,
//    3,
//    3,
//    4,
//    4,
//    4
//  )
//
//  // Inferred type: Mat<Int, D4, D3>
//  val lm = l * m
//  println("lm = $lm")
//
//// Does not compile, inner dimension mismatch
////  lm * f
////  ln * d
//
//  val o = edu.umontreal.kotlingrad.dependent.Mat(
//    edu.umontreal.kotlingrad.dependent.D9,
//    edu.umontreal.kotlingrad.dependent.D9,
//    1,
//    2,
//    3,
//    4,
//    5,
//    6,
//    7,
//    8,
//    9,
//    1,
//    2,
//    3,
//    4,
//    5,
//    6,
//    7,
//    8,
//    9,
//    1,
//    2,
//    3,
//    4,
//    5,
//    6,
//    7,
//    8,
//    9,
//    1,
//    2,
//    3,
//    4,
//    5,
//    6,
//    7,
//    8,
//    9,
//    1,
//    2,
//    3,
//    4,
//    5,
//    6,
//    7,
//    8,
//    9,
//    1,
//    2,
//    3,
//    4,
//    5,
//    6,
//    7,
//    8,
//    9,
//    1,
//    2,
//    3,
//    4,
//    5,
//    6,
//    7,
//    8,
//    9,
//    1,
//    2,
//    3,
//    4,
//    5,
//    6,
//    7,
//    8,
//    9,
//    1,
//    2,
//    3,
//    4,
//    5,
//    6,
//    7,
//    8,
//    9
//  )
//
//  println(o * o - (o + o))
//
//  val p = edu.umontreal.kotlingrad.dependent.Mat(
//    edu.umontreal.kotlingrad.dependent.D1,
//    edu.umontreal.kotlingrad.dependent.D100,
//    0
//  )
//  // Type-checked matrix operations are still possible after unsafe construction
//  val q = p.transpose()
//  q * p
//// Does not compile, inner dimension mismatch
////  q * f
//
//  // Functional initializers
//  val r = edu.umontreal.kotlingrad.dependent.Mat(
//    edu.umontreal.kotlingrad.dependent.D20,
//    edu.umontreal.kotlingrad.dependent.D20
//  ) { y, z -> if (y == z) 1 else 0 }
//  println("r: $r")
//  val s = edu.umontreal.kotlingrad.dependent.Mat(
//    edu.umontreal.kotlingrad.dependent.D11,
//    edu.umontreal.kotlingrad.dependent.D11,
//    listOf(
//      1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
//      1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
//      1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
//      1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
//      1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
//      1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
//      1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
//      1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
//      1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
//      1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
//      1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
//    )
//  )
//
//  println("s: $s")
//  val t = edu.umontreal.kotlingrad.dependent.Mat(
//    edu.umontreal.kotlingrad.dependent.D1,
//    edu.umontreal.kotlingrad.dependent.D1,
//    1
//  )
//  println("t: $t")
//
//  // Unsafe construction with a list of the wrong size it will fail at runtime
//  try {
//    edu.umontreal.kotlingrad.dependent.Mat(
//      edu.umontreal.kotlingrad.dependent.D2,
//      edu.umontreal.kotlingrad.dependent.D2,
//      listOf(1, 2, 3)
//    )
//    assert(false)
//  } catch (e: IllegalArgumentException) {
//    println(e)
//  }
//}