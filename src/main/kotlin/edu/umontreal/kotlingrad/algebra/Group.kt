package edu.umontreal.kotlingrad.algebra

interface Group<X: Group<X>> {
  operator fun unaryMinus(): X

  operator fun plus(addend: X): X

  operator fun minus(subtrahend: X): X = this + -subtrahend

  operator fun times(multiplicand: Long): X
}