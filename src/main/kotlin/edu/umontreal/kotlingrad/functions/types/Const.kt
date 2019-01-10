package edu.umontreal.kotlingrad.functions.types


import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.algebra.RealPrototype
import edu.umontreal.kotlingrad.functions.NullaryFunction
import edu.umontreal.kotlingrad.numerical.FieldPrototype

open class Const<X: Field<X>>(val value: X, override val prototype: FieldPrototype<X>): NullaryFunction<X>() {
  override fun invoke(map: Map<Var<X>, X>): X = value

  override fun diff(ind: Var<X>) = Zero(prototype)

  override fun toString(): String = value.toString()

  override fun inverse(): Const<X> = Const(value.inverse(), prototype)

  override fun unaryMinus(): Const<X> = Const(-value, prototype)
}