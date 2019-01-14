package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.types.Vector

open class VectorFunction<X: Field<X>>(vararg vFuns: Function<X>):
    Vector<Function<X>>(*vFuns), Differentiable<X, VectorFunction<X>> {
  override fun diff(ind: Var<X>): VectorFunction<X> = VectorFunction(*mapTo(ArrayList(size)) { it.diff(ind) }.toTypedArray())

  override fun grad(): Map<Var<X>, VectorFunction<X>> = TODO("not implemented")
}