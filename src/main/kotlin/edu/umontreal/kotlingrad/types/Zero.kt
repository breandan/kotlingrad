package edu.umontreal.kotlingrad.types

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.algebra.FieldPrototype
import edu.umontreal.kotlingrad.functions.Function

class Zero<X: Field<X>>(fieldPrototype: FieldPrototype<X>): Const<X>(fieldPrototype.zero, fieldPrototype) {
  override fun plus(addend: Function<X>) = addend
  override fun times(multiplicand: Function<X>) = this
  override fun unaryMinus() = this
}