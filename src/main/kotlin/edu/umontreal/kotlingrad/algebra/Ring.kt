package edu.umontreal.kotlingrad.algebra

interface Ring<X: Ring<X>>: AbelianGroup<X> {
  operator fun times(multiplicand: X): X

  fun pow(exponent: Int): X
}