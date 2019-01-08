package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.calculus.Differentiable
import edu.umontreal.kotlingrad.operators.Inverse
import edu.umontreal.kotlingrad.operators.Negative
import edu.umontreal.kotlingrad.operators.Product
import edu.umontreal.kotlingrad.operators.Sum
import edu.umontreal.kotlingrad.types.UnivariatePolynomialTerm
import edu.umontreal.kotlingrad.types.Var

interface Function<X: Field<X>>: Field<Function<X>>, Differentiable<X, Function<X>>, kotlin.Function<X> {
  operator fun invoke(map: Map<Var<X>, X> = emptyMap()): X

  operator fun invoke(vararg pair: Pair<Var<X>, X>) = invoke(pair.toMap())

  fun independentVariables(): Set<Var<X>> = emptySet()

  override fun toString(): String

  override fun grad(): Map<Var<X>, Function<X>> = independentVariables().associateWith { diff(it) }

  override fun diff(ind: Var<X>): Function<X> = grad()[ind]!!

  override fun plus(addend: Function<X>): Function<X> = Sum(this, addend)

  override fun times(multiplicand: Function<X>): Function<X> = Product(this, multiplicand)

  override fun inverse(): Function<X> = Inverse(this)

  override fun unaryMinus(): Function<X> = Negative(this)

  override fun times(multiplicand: Long): Function<X> = UnivariatePolynomialTerm(multiplicand, this, 1)

  override fun pow(exponent: Int): Function<X> = UnivariatePolynomialTerm(1L, this, exponent)
}