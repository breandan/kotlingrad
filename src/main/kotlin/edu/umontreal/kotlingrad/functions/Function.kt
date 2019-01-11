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

  override fun plus(addend: Function<X>): Function<X> = when {
    this is Zero -> addend
    addend is Zero -> this
    this is Const && addend is Const -> Const(value + addend.value, prototype)
    else -> Sum(this, addend)
  }

  override fun times(multiplicand: Function<X>): Function<X> = when {
    this is Zero -> this
    this is One -> multiplicand
    multiplicand is One -> this
    multiplicand is Zero -> multiplicand
    this is Const && multiplicand is Const -> Const(value * multiplicand.value, prototype)
    else -> Product(this, multiplicand)
  }

  override fun inverse(): Function<X> = Inverse(this)

  override fun unaryMinus(): Function<X> = when {
    this is Const -> Const(-value, prototype)
    else -> Negative(this)
  }

  override fun pow(exponent: Function<X>): Function<X> = when {
    this is Const && exponent is Const -> Const(value.pow(exponent.value), prototype)
    else -> PolynomialTerm(one, this, exponent)
  }

  open val prototype: FieldPrototype<X> by lazy { variables.first().prototype }

  val one: One<X> by lazy { One(prototype) }

  val zero: Zero<X> by lazy { Zero(prototype) }

  val two: Const<X> by lazy { Const(one.value + one.value, prototype) }
}