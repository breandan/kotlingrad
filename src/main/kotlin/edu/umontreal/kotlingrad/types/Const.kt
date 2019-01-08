package edu.umontreal.kotlingrad.types


import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.algebra.FieldPrototype
import edu.umontreal.kotlingrad.functions.NullaryFunction

open class Const<X: Field<X>>(val value: X, val prototype: FieldPrototype<X>): NullaryFunction<X>() {
  override fun invoke(map: Map<Var<X>, X>): X = value

//  override val isConstant = true

  override fun diff(ind: Var<X>) = Zero(prototype)

  override fun toString(): String = value.toString()

//  override fun plus(addend: Function<X>) =
//      if (addend.isConstant) Const(addend.value + value, prototype) else super.plus(addend)

//  override fun times(multiplicand: Function<X>) =
//      if (multiplicand.isConstant) Const(multiplicand.value * value, prototype) else super.times(multiplicand)

  override fun inverse(): Const<X> = Const(value.inverse(), prototype)

  override fun unaryMinus(): Const<X> = Const(-value, prototype)
}