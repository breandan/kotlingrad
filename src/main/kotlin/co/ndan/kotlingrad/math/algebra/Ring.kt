package co.ndan.kotlingrad.math.algebra

interface Ring<X> : AbelianGroup<X> {
  operator fun times(multiplicand: X): X

  fun pow(exponent: Int): X
}
