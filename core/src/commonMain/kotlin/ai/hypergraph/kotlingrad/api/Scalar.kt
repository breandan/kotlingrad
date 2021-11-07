package ai.hypergraph.kotlingrad.api

/*
 * Algebraic primitives.
 *
 * https://arxiv.org/pdf/1910.09336.pdf#page=4
 * https://core.ac.uk/download/pdf/200977628.pdf#page=12
 * https://raw.githubusercontent.com/breandan/algebraic-structures/patch-1/graph.svg
 */

interface Group<X: Group<X>> {
  operator fun unaryMinus(): X
  operator fun unaryPlus(): X
  operator fun plus(addend: X): X
  operator fun minus(subtrahend: X): X = this + -subtrahend
}

interface Field<X: Field<X>>: Group<X> {
  operator fun div(divisor: X): X
  operator fun times(multiplicand: X): X
  infix fun pow(exponent: X): X
  fun log(base: X): X
}