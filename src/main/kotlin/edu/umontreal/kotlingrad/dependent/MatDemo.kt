package edu.umontreal.kotlingrad.dependent

// M multiplication MxN for M∈[1, 3] N∈[1, 3]
fun main() {
  // Inferred type: Mat<Double, `1`, `3`>
  val a = Mat(`1`, `3`, 1.0, 2.0, 3.0)
  println("a = $a")
  // Inferred type: Mat<Double, `3`, `1`>
  val b = a.transpose()
  println("b = $b")
  // Inferred type: Mat<Double, `1`, `1`>
  val c = a * b
  println("c = ab = $c")

// Does not compile, inner dimension mismatch
//  a * a
//  b * b

// Does not compile, incompatible shape
//  val b_ = Mat.invoke(`3`, `1`, 1.0, 2.0)
//  val b_ = Mat.invoke(`3`, `1`, 1.0, 2.0, 3.0, 4.0)

// Does not compile, incompatible shape
//  val b_ = Mat(`2`, `1`, 1.0)

  // Inferred type: Mat<Double, `2`, `3`>
  val d = Mat(`2`, `3`,
    1.0, 2.0, 3.0,
    4.0, 5.0, 6.0
  )
  println("d = $d")

  // Inferred type: Mat<Double, `3`, `2`>
  val e = Mat(`3`, `2`,
    1.0, 2.0,
    3.0, 4.0,
    5.0, 6.0
  )
  println("e = $e")

  // Inferred type: Mat<Double, `2`, `2`>
  val f = d * e
  println("f = de = $f")

// Does not compile, inner dimension mismatch
//  e * b
//  f * b

// Does not compile, incompatible size
//  val d_: Mat<Double, `2`, `3`> = Mat(`2`, `3`,
//    1.0, 2.0, 3.0,
//    4.0, 5.0
//  )

  // Inferred type: Mat<Double, `3`, `3`>
  val g = Mat(`3`, `3`,
    1.0, 2.0, 3.0,
    4.0, 5.0, 6.0,
    7.0, 8.0, 9.0
  )
  println("g = $g")

  // Inferred type: Mat<Double, `3`, `3`>
  val h = Mat(`3`, `3`,
    1.0, 2.0, 3.0,
    4.0, 5.0, 6.0,
    7.0, 8.0, 9.0
  )
  println("h = $g")

  // Inferred type: Mat<Double, `3`, `3`>
  val i = g * h
  println("i = gh = $i")
  val j = i * i
  println("j = ii = $j")
  val k = i * b
  println("k = ib = $j")

// Does not compile, inner dimension mismatch
//  i * f
//  i * d

  // Inferred type: Mat<Double, `4`, `3`>
  val l = Mat(`4`, `4`,
    1.0, 2.0, 3.0, 4.0,
    5.0, 6.0, 7.0, 8.0,
    9.0, 0.0, 0.0, 0.0,
    9.0, 0.0, 0.0, 0.0
  )
  println("l = $l")

  // Inferred type: Mat<Double, `4`, `3`>
  val m = Mat(`4`, `1`,
    1.0,
    2.0,
    3.0,
    4.0
  )
  println("m = $m")

  // Inferred type: Mat<Double, `4`, `1`>
  val lm = l * m
  println("lm = $lm")

// Does not compile, inner dimension mismatch
//  n * f
//  n * d

  println("Done.")
}
