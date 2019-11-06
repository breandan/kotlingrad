package edu.umontreal.kotlingrad.dependent

fun main() {
  // Inferred type: Mat<Int, D1, D3>
  val a = Mat(D1, D3, 1, 2, 3)
  println("a = $a")
  // Inferred type: Mat<Int, D3, D1>
  val b = a.transpose()
  println("b = $b")
  // Inferred type: Mat<Int, D1, D1>
  val c = a * b
  println("c = ab = $c")

// Does not compile, inner dimension mismatch
//  a * a
//  b * b

// Does not compile, incompatible shape
//  val b_ = Mat(D3, D1, 1, 2)
//  val c_ = Mat(D3, D1, 1, 2, 3, 4)

// Does not compile, incompatible shape
//  val b_ = Mat(D2, D1, 1)

  // Inferred type: Mat<Int, D2, D3>
  val d = Mat(D2, D3,
    1, 2, 3,
    4, 5, 6
  )
  println("d = $d")

  // Inferred type: Mat<Int, D3, D2>
  val e = Mat(D3, D2,
    1, 2,
    3, 4,
    5, 6
  )
  println("e = $e")

  // Inferred type: Mat<Int, D2, D2>
  val f = d * e
  println("f = de = $f")

// Does not compile, inner dimension mismatch
//  e * b
//  f * b

// Does not compile, incompatible size
//  val d_: Mat<Int, D2, D3> = Mat(D2, D3,
//    1, 2, 3,
//    4, 5
//  )

  // Inferred type: Mat<Int, D3, D3>
  val g = Mat(D3, D3,
    1, 2, 3,
    4, 5, 6,
    7, 8, 9
  )
  println("g = $g")

  // Inferred type: Mat<Int, D3, D3>
  val h = Mat(D3, D3,
    1, 2, 3,
    4, 5, 6,
    7, 8, 9
  )
  println("h = $g")

  // Inferred type: Mat<Int, D3, D3>
  val i = g * h
  println("i = gh = $i")
  val j = i * i
  println("j = ii = $j")
  val k = i * b
  println("k = ib = $j")

// Does not compile, inner dimension mismatch
//  i * f
//  i * d

  // Inferred type: Mat<Int, D4, D4>
  val l = Mat(D4, D4,
    1, 2, 3, 4,
    5, 6, 7, 8,
    9, 0, 0, 0,
    9, 0, 0, 0
  )
  println("l = $l")

  // Inferred type: Mat<Int, D4, D3>
  val m = Mat(D4, D3,
    1, 1, 1,
    2, 2, 2,
    3, 3, 3,
    4, 4, 4
  )

  // Inferred type: Mat<Int, D4, D3>
  val lm = l * m
  println("lm = $lm")

// Does not compile, inner dimension mismatch
//  lm * f
//  ln * d

  val o = Mat(D9, D9,
    1, 2, 3, 4, 5, 6, 7, 8, 9,
    1, 2, 3, 4, 5, 6, 7, 8, 9,
    1, 2, 3, 4, 5, 6, 7, 8, 9,
    1, 2, 3, 4, 5, 6, 7, 8, 9,
    1, 2, 3, 4, 5, 6, 7, 8, 9,
    1, 2, 3, 4, 5, 6, 7, 8, 9,
    1, 2, 3, 4, 5, 6, 7, 8, 9,
    1, 2, 3, 4, 5, 6, 7, 8, 9,
    1, 2, 3, 4, 5, 6, 7, 8, 9
  )

  println(o * o - (o + o))

  val p = Mat(D1, D100, 0)
  // Type-checked matrix operations are still possible after unsafe construction
  val q = p.transpose()
  q * p
// Does not compile, inner dimension mismatch
//  q * f

  // Functional initializers
  val r = Mat(D20, D20) { y, z -> if (y == z) 1 else 0 }
  println("r: $r")
  val s = Mat(D11, D11, listOf(
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11))

  println("s: $s")
  val t = Mat(D1, D1, 1)
  println("t: $t")

  // Unsafe construction with a list of the wrong size it will fail at runtime
  try {
    Mat(D2, D2, listOf(1, 2, 3))
    assert(false)
  } catch (e: IllegalArgumentException) {
    println(e)
  }
}
