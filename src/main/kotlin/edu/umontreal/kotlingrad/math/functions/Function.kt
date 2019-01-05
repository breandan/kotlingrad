package edu.umontreal.kotlingrad.math.functions

import edu.umontreal.kotlingrad.math.algebra.Field
import edu.umontreal.kotlingrad.math.calculus.Differentiable
import edu.umontreal.kotlingrad.math.operators.Inverse
import edu.umontreal.kotlingrad.math.operators.Negative
import edu.umontreal.kotlingrad.math.operators.Product
import edu.umontreal.kotlingrad.math.operators.Sum
import edu.umontreal.kotlingrad.math.types.UnivariatePolynomialTerm
import edu.umontreal.kotlingrad.math.types.Var

interface Function<X: Field<X>>: Field<Function<X>>, Differentiable<X, Function<X>>, kotlin.Function<X> {
  operator fun invoke(map: Map<Var<X>, X> = emptyMap()): X

  operator fun invoke(vararg pair: Pair<Var<X>, X>) = invoke(pair.toMap())

  fun independentVariables(): Set<Var<X>> = emptySet()

  override fun toString(): String

  override fun grad(): Map<Var<X>, Function<X>> = independentVariables().associateWith { diff(it) }

  override fun diff(ind: Var<X>): Function<X> = grad()[ind]!!

  override fun plus(addend: Function<X>): Function<X> = Sum(this, addend)

  override fun minus(subtrahend: Function<X>): Function<X> = this + -subtrahend

  override fun times(multiplicand: Function<X>): Function<X> = Product(this, multiplicand)

  override fun div(divisor: Function<X>): Function<X> = this * divisor.inverse()

  override fun inverse(): Function<X> = Inverse(this)

  override fun unaryMinus(): Function<X> = Negative(this)

  override fun times(multiplicand: Long): Function<X> = UnivariatePolynomialTerm(multiplicand, this, 1)

  override fun pow(exponent: Int): Function<X> = UnivariatePolynomialTerm(1L, this, exponent)
}