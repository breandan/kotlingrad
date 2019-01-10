package edu.umontreal.kotlingrad.algebra

interface Field<X: Field<X>>: CommutativeRing<X> {
  fun inverse(): X

  operator fun div(divisor: X): X = this * divisor.inverse()

  fun pow(exponent: X): X
}