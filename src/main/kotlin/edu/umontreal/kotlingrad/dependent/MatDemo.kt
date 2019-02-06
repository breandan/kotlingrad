package edu.umontreal.kotlingrad.dependent

fun main() {
  // Inferred type: Mat<Int, `1`, `3`>
  val a = Mat(`1`, `3`, 1, 2, 3)
  println("a = $a")
  // Inferred type: Mat<Int, `3`, `1`>
  val b = a.transpose()
  println("b = $b")
  // Inferred type: Mat<Int, `1`, `1`>
  val c = a * b
  println("c = ab = $c")

// Does not compile, inner dimension mismatch
//  a * a
//  b * b

// Does not compile, incompatible shape
//  val b_ = Mat(`3`, `1`, 1, 2)
//  val c_ = Mat(`3`, `1`, 1, 2, 3, 4)

// Does not compile, incompatible shape
//  val b_ = Mat(`2`, `1`, 1)

  // Inferred type: Mat<Int, `2`, `3`>
  val d = Mat(`2`, `3`,
    1, 2, 3,
    4, 5, 6
  )
  println("d = $d")

  // Inferred type: Mat<Int, `3`, `2`>
  val e = Mat(`3`, `2`,
    1, 2,
    3, 4,
    5, 6
  )
  println("e = $e")

  // Inferred type: Mat<Int, `2`, `2`>
  val f = d * e
  println("f = de = $f")

// Does not compile, inner dimension mismatch
//  e * b
//  f * b

// Does not compile, incompatible size
//  val d_: Mat<Int, `2`, `3`> = Mat(`2`, `3`,
//    1, 2, 3,
//    4, 5
//  )

  // Inferred type: Mat<Int, `3`, `3`>
  val g = Mat(`3`, `3`,
    1, 2, 3,
    4, 5, 6,
    7, 8, 9
  )
  println("g = $g")

  // Inferred type: Mat<Int, `3`, `3`>
  val h = Mat(`3`, `3`,
    1, 2, 3,
    4, 5, 6,
    7, 8, 9
  )
  println("h = $g")

  // Inferred type: Mat<Int, `3`, `3`>
  val i = g * h
  println("i = gh = $i")
  val j = i * i
  println("j = ii = $j")
  val k = i * b
  println("k = ib = $j")

// Does not compile, inner dimension mismatch
//  i * f
//  i * d

 // Inferred type: Mat<Int, `4`, `3`>
  val l = Mat(`4`, `4`,
    1, 2, 3, 4,
    5, 6, 7, 8,
    9, 0, 0, 0,
    9, 0, 0, 0
  )
  println("l = $l")

  // Inferred type: Mat<Int, `4`, `3`>
  val m = Mat(`4`, `1`,
    1,
    2,
    3,
    4
  )
  println("m = $m")

  // Inferred type: Mat<Int, `4`, `1`>
  val lm = l * m
  println("lm = $lm")

// Does not compile, inner dimension mismatch
//  n * f
//  n * d

  val o = Mat(`9`, `9`,
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

  println(o*o - (o+o))

  // Unsafe construction of large matrices is possible
  val p = Mat(`1`, `100`, listOf(Vec(`100`, listOf(0))))
  // Type-checked matrix operations are still possible after unsafe construction
  val q: Mat<Int, `100`, `1`> = p.transpose() // Runtime failure: Index OOB
// Does not compile, inner dimension mismatch
//  q * f
}
