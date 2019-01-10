package edu.umontreal.kotlingrad.functions.types

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.numerical.FieldPrototype

class Zero<X: Field<X>>(fieldPrototype: FieldPrototype<X>): Const<X>(fieldPrototype.zero, fieldPrototype) {
  override fun unaryMinus() = this
}