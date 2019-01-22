package edu.umontreal.kotlingrad.algebra

interface Field<X: Field<X>>: CommutativeRing<X> {
  fun inverse(): X

  infix operator fun div(divisor: X): X = this * divisor.inverse()

  infix fun pow(exp: X): X
}