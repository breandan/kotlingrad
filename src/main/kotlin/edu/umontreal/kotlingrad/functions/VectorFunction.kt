package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.calculus.Differentiable
import edu.umontreal.kotlingrad.functions.types.Vector

open class VectorFunction<X: Field<X>>(vararg vFuns: Function<X>):
    Vector<Function<X>>(*vFuns), Differentiable<X> {
  override fun diff(ind: X) = TODO()

  override val one: Vector<Function<X>>
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
  override val zero: Vector<Function<X>>
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

  override fun grad() = TODO("not implemented")
}