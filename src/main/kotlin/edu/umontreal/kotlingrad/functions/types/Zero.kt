package edu.umontreal.kotlingrad.functions.types

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.algebra.RealPrototype

class Zero<X: Field<X>>(realPrototype: RealPrototype<X>): Const<X>(realPrototype.zero, realPrototype) {
  override fun unaryMinus() = this
}