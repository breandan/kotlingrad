package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.calculus.Differentiable
import edu.umontreal.kotlingrad.functions.operators.Inverse
import edu.umontreal.kotlingrad.functions.operators.Negative
import edu.umontreal.kotlingrad.functions.operators.Product
import edu.umontreal.kotlingrad.functions.operators.Sum
import edu.umontreal.kotlingrad.functions.types.*
import edu.umontreal.kotlingrad.numerical.FieldPrototype

abstract class Function<X: Field<X>>(open val variables: Set<Var<X>>):
    Field<Function<X>>, Differentiable<X, Function<X>>, kotlin.Function<X> {
  abstract operator fun invoke(map: Map<Var<X>, X> = emptyMap()): X

  operator fun invoke(vararg pair: Pair<Var<X>, X>) = invoke(pair.toMap())

  abstract override fun toString(): String

  override fun grad(): Map<Var<X>, Function<X>> = variables.associateWith { diff(it) }

  override fun diff(ind: Var<X>): Function<X> = grad()[ind]!!

  override fun plus(addend: Function<X>): Function<X> =
      when {
        this is Zero -> addend
        addend is Zero -> this
        this is Const && addend is Const -> Const(value + addend.value, prototype)
        else -> Sum(this, addend)
      }

  override fun times(multiplicand: Function<X>): Function<X> =
      when {
        this is Zero -> this
        this is One -> multiplicand
        multiplicand is One -> this
        multiplicand is Zero -> multiplicand
        this is Const && multiplicand is Const -> Const(value * multiplicand.value, prototype)
        else -> Product(this, multiplicand)
      }

  override fun inverse(): Function<X> = Inverse(this)

  override fun unaryMinus(): Function<X> = Negative(this)

  fun times(multiplicand: Const<X>): Function<X> =
      PolynomialTerm(multiplicand, this, one)

  override fun pow(exponent: Function<X>): Function<X> =
      PolynomialTerm(one, this, exponent)

  open val prototype: FieldPrototype<X>
      get() = variables.first().prototype

  val one: One<X>
    get() = One(prototype)

  val zero: Zero<X>
    get() = Zero(prototype)

  val two: Const<X>
    get() = Const(one.value + one.value, prototype)
}