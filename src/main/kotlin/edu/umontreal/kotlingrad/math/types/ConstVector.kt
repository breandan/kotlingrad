package edu.umontreal.kotlingrad.math.types

import edu.umontreal.kotlingrad.math.algebra.Field
import edu.umontreal.kotlingrad.math.algebra.FieldPrototype
import edu.umontreal.kotlingrad.math.functions.VectorFunction


class ConstVector<X: Field<X>>: VectorFunction<X> {
  constructor(ffc: FieldPrototype<X>, vararg contents: Const<X>): super(ffc, Vector(*contents))
  constructor(ffc: FieldPrototype<X>, contents: Collection<Const<X>>): super(ffc, Vector(contents))

  override fun get(i: Int) = vFuns[i] as Const<X>
}