package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.calculus.Differentiable
import edu.umontreal.kotlingrad.functions.operators.Inverse
import edu.umontreal.kotlingrad.functions.operators.Negative
import edu.umontreal.kotlingrad.functions.operators.Product
import edu.umontreal.kotlingrad.functions.operators.Sum
import edu.umontreal.kotlingrad.functions.types.Const
import edu.umontreal.kotlingrad.functions.types.One
import edu.umontreal.kotlingrad.functions.types.PolynomialTerm
import edu.umontreal.kotlingrad.functions.types.Var

abstract class Function<X: Field<X>>(open val variables: Set<Var<X>>):
    Field<Function<X>>, Differentiable<X, Function<X>>, kotlin.Function<X> {
  abstract operator fun invoke(map: Map<Var<X>, X> = emptyMap()): X

  operator fun invoke(vararg pair: Pair<Var<X>, X>) = invoke(pair.toMap())

  abstract override fun toString(): String

  override fun grad(): Map<Var<X>, Function<X>> = variables.associateWith { diff(it) }

  override fun diff(ind: Var<X>): Function<X> = grad()[ind]!!

  override fun plus(addend: Function<X>): Function<X> = Sum(this, addend)

  override fun times(multiplicand: Function<X>): Function<X> = Product(this, multiplicand)

  override fun inverse(): Function<X> = Inverse(this)

  override fun unaryMinus(): Function<X> = Negative(this)

  fun times(multiplicand: Const<X>): Function<X> =
      PolynomialTerm(multiplicand, this, one)

  override fun pow(exponent: Function<X>): Function<X> =
      PolynomialTerm(one, this, exponent)

  val one: One<X>
      get() = One(variables.first().prototype)
}