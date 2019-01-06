package edu.umontreal.kotlingrad.math.algebra

interface Field<X: Field<X>>: CommutativeRing<X> {
  fun inverse(): X

  operator fun div(divisor: X): X
}