package edu.umontreal.kotlingrad.math.types

import edu.umontreal.kotlingrad.math.algebra.Field
import edu.umontreal.kotlingrad.math.algebra.FieldPrototype
import edu.umontreal.kotlingrad.math.functions.Function

class One<X: Field<X>>(fieldPrototype: FieldPrototype<X>): Const<X>(fieldPrototype.one, fieldPrototype) {
  override fun times(multiplicand: Function<X>) = multiplicand
}