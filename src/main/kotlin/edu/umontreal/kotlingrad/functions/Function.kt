package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.numerical.FieldPrototype
import edu.umontreal.kotlingrad.calculus.Differentiable
import edu.umontreal.kotlingrad.functions.operators.*
import edu.umontreal.kotlingrad.functions.types.Const
import edu.umontreal.kotlingrad.functions.types.One
import edu.umontreal.kotlingrad.functions.types.Var
import edu.umontreal.kotlingrad.functions.types.Zero

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
    this is Const && addend is Const -> Const(value + addend.value)
    this == addend -> two * this
    else -> Sum(this, addend)
  }

  override fun times(multiplicand: Function<X>): Function<X> = when {
    this is Zero -> this
    this is One -> multiplicand
    multiplicand is One -> this
    multiplicand is Zero -> multiplicand
    this is Const && multiplicand is Const -> Const(value * multiplicand.value)
    this == multiplicand -> pow(two)
    else -> Product(this, multiplicand)
  }

  override fun div(divisor: Function<X>): Function<X> = when {
    this is Zero -> this
    this is One -> this
    divisor is One -> divisor.inverse()
    divisor is Zero -> throw Exception("Cannot divide by $divisor")
    this is Const && divisor is Const -> Const(value / divisor.value)
    this == divisor -> one
    else -> super.div(divisor)
  }

  override fun equals(other: Any?) =
    if (this is Var<*> || other is Var<*>) this === other
    //TODO implement tree comparison for semantic equals
    else super.equals(other)

  fun ln(): Function<X> = Log(this)

  override fun inverse(): Function<X> = Inverse(this)

  override fun unaryMinus(): Function<X> = when {
    this is Const -> Const(-value)
    else -> Negative(this)
  }

  override fun pow(exponent: Function<X>): Function<X> = when {
    this is Const && exponent is Const -> Const(value.pow(exponent.value))
    else -> Power(this, exponent)
  }

  open val prototype: FieldPrototype<X> by lazy { variables.first().prototype }

  val one: One<X> by lazy { One(prototype) }

  val zero: Zero<X> by lazy { Zero(prototype) }

  val two: Const<X> by lazy { Const(one.value + one.value) }

  val e: Const<X> by lazy { Const(prototype.one) }
}