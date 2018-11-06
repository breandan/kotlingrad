package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.algebra.FieldPrototype
import co.ndan.kotlingrad.math.calculus.Function

class Zero<X : Field<X>>(fieldPrototype: FieldPrototype<X>) : Const<X>(fieldPrototype.zero, fieldPrototype) {
  override fun plus(addend: Function<X>) = addend
  override fun times(multiplicand: Function<X>) = this
  override fun unaryMinus() = this
}
