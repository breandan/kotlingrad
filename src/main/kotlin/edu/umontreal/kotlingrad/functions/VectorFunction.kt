package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.calculus.Differentiable
import edu.umontreal.kotlingrad.functions.types.Vector

open class VectorFunction<X: Field<X>>(vararg vFuns: Function<X>):
    Vector<Function<X>>(*vFuns), Differentiable<X, VectorFunction<X>> {
  override val one: Vector<Function<X>>
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
  override val zero: Vector<Function<X>>
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

  override fun diff(ind: Function.Var<X>): VectorFunction<X> =
    VectorFunction(*mapTo(ArrayList(size)) { it.diff(ind) }.toTypedArray())

  override fun grad(): Map<Function.Var<X>, VectorFunction<X>> = TODO("not implemented")
}