package edu.umontreal.kotlingrad.math.functions

import edu.umontreal.kotlingrad.math.algebra.AbelianGroup
import edu.umontreal.kotlingrad.math.algebra.Field
import edu.umontreal.kotlingrad.math.algebra.FieldPrototype
import edu.umontreal.kotlingrad.math.calculus.Differentiable
import edu.umontreal.kotlingrad.math.types.Var
import edu.umontreal.kotlingrad.math.types.Vector
import edu.umontreal.kotlingrad.math.types.Zero
import java.util.*

open class VectorFunction<X: Field<X>>(
    val prototype: FieldPrototype<X>,
    val vFuns: Vector<Function<X>>
): AbelianGroup<VectorFunction<X>>, Differentiable<X, VectorFunction<X>>, List<Function<X>> by vFuns {
  override fun diff(ind: Var<X>): VectorFunction<X> =
      VectorFunction(prototype, Vector(mapTo(ArrayList(size)) { it.diff(ind) } as ArrayList<Function<X>>))

  override fun grad(): Map<Var<X>, VectorFunction<X>> {
    TODO("not implemented")
  }

  fun dot(vfx: VectorFunction<X>): Function<X> {
    if (vfx.size != size) throw IllegalArgumentException("$size != ${vfx.size}")
    var norm: Function<X> = Zero(prototype)
    for (i in 0 until size) norm += this[i] * vfx[i]
    return norm
  }

  override fun unaryMinus() = VectorFunction(prototype, -vFuns)

  override fun plus(addend: VectorFunction<X>) = VectorFunction(prototype, vFuns + addend.vFuns)

  override fun minus(subtrahend: VectorFunction<X>) = VectorFunction(prototype, vFuns - subtrahend.vFuns)

  override fun times(multiplicand: Long) = VectorFunction(prototype, vFuns * multiplicand)

  fun times(multiplicand: Function<X>) = VectorFunction(prototype, vFuns * multiplicand)

  operator fun div(divisor: Function<X>) = VectorFunction(prototype, vFuns / divisor)
}