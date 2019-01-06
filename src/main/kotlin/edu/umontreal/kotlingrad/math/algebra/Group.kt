package edu.umontreal.kotlingrad.math.algebra

interface Group<X: Group<X>> {
  operator fun unaryMinus(): X

  operator fun plus(addend: X): X

  operator fun minus(subtrahend: X): X

  operator fun times(multiplicand: Long): X
}