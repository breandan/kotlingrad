package edu.umontreal.kotlingrad.functions.types


import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.algebra.FieldPrototype
import edu.umontreal.kotlingrad.functions.NullaryFunction

open class Const<X: Field<X>>(
  val value: X
): NullaryFunction<X>() {
  override fun invoke(map: Map<Var<X>, X>): X = value

  override fun diff(ind: Var<X>) = Const(value - value)

  override fun toString(): String = value.toString()

  override fun inverse(): Const<X> = Const(value.inverse())

  override fun unaryMinus(): Const<X> = Const(-value)
}