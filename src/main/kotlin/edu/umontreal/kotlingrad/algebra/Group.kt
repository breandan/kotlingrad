package edu.umontreal.kotlingrad.algebra

interface Group<X: Group<X>> {
  operator fun unaryMinus(): X

  infix operator fun plus(addend: X): X

  infix operator fun minus(subtrahend: X): X = this + -subtrahend

  infix operator fun times(multiplicand: X): X
}