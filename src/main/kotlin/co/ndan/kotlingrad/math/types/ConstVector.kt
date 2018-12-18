package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.algebra.FieldPrototype
import co.ndan.kotlingrad.math.calculus.VectorFunction


class ConstVector<X: Field<X>>: VectorFunction<X> {
  constructor(ffc: FieldPrototype<X>, vararg contents: Const<X>): super(ffc, *contents)
  constructor(ffc: FieldPrototype<X>, contents: Collection<Const<X>>): super(ffc, *contents.toTypedArray())

  override fun get(i: Int) = vector[i] as Const<X>
}