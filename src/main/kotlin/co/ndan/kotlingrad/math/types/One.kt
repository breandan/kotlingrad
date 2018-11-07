package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.algebra.FieldPrototype
import co.ndan.kotlingrad.math.calculus.Function

class One<X : Field<X>>(fieldPrototype: FieldPrototype<X>) : Const<X>(fieldPrototype.one, fieldPrototype) {
  override fun times(multiplicand: Function<X>) = multiplicand
}