package edu.umontreal.kotlingrad.types

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.algebra.FieldPrototype
import edu.umontreal.kotlingrad.functions.Function

class One<X: Field<X>>(fieldPrototype: FieldPrototype<X>): Const<X>(fieldPrototype.one, fieldPrototype) {
  override fun times(multiplicand: Function<X>) = multiplicand
}