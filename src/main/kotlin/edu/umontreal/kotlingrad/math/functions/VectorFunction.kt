package edu.umontreal.kotlingrad.math.functions

import edu.umontreal.kotlingrad.math.algebra.Field
import edu.umontreal.kotlingrad.math.calculus.Differentiable
import edu.umontreal.kotlingrad.math.types.Var
import edu.umontreal.kotlingrad.math.types.Vector

open class VectorFunction<X: Field<X>>(vararg vFuns: Function<X>):
    Vector<Function<X>>(*vFuns), Differentiable<X, VectorFunction<X>> {
  override fun diff(ind: Var<X>): VectorFunction<X> = VectorFunction(*mapTo(ArrayList(size)) { it.diff(ind) }.toTypedArray())

  override fun grad(): Map<Var<X>, VectorFunction<X>> = TODO("not implemented")
}