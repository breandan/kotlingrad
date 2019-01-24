package edu.umontreal.kotlingrad.dependent

fun main(args: Array<String>) {
  // Inferred type: Mat<Double, `1`, `3`>
  val a = Mat(`1`, `3`, 1.0, 2.0, 3.0)
  // Inferred type: Mat<Double, `3`, `1`>
  val b = Mat(`3`, `1`, 1.0, 2.0, 3.0)
  // Inferred type: Mat<Double, `1`, `1`>
  val c = a * b

// Does not compile, inner dimension mismatch
//  a * a
//  b * b

// Does not compile, incompatible shape
//  val b_ = Mat(`3`, `1`, 1.0, 2.0)

// Does not compile, incompatible shape
//  val b_ = Mat(`2`, `1`, 1.0, 2.0, 3.0)

  // Inferred type: Mat<Double, `2`, `3`>
  val d: Mat<Double, `2`, `3`> = Mat(`2`, `3`,
    1.0, 2.0, 3.0,
    4.0, 5.0, 6.0
  )

  // Inferred type: Mat<Double, `3`, `2`>
  val e = Mat(`3`, `2`,
    1.0, 2.0,
    3.0, 4.0,
    5.0, 6.0
  )

  // Inferred type: Mat<Double, `2`, `2`>
  val f = d * e

// Does not compile, inner dimension mismatch
//  val g = e * b
//  val h = f * b

// Does not compile, incompatible size
//  val d_: Mat<Double, `2`, `3`> = Mat(`2`, `3`,
//    1.0, 2.0, 3.0,
//    4.0, 5.0
//  )
}
