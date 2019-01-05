package edu.umontreal.kotlingrad.math.functions

import edu.umontreal.kotlingrad.math.algebra.AbelianGroup
import edu.umontreal.kotlingrad.math.algebra.Field
import edu.umontreal.kotlingrad.math.algebra.FieldPrototype
import edu.umontreal.kotlingrad.math.calculus.Differentiable
import edu.umontreal.kotlingrad.math.types.Var
import edu.umontreal.kotlingrad.math.types.Vector
import edu.umontreal.kotlingrad.math.types.Zero
import java.util.*

open class VectorFunction<X: Field<X>>: AbelianGroup<VectorFunction<X>>, Differentiable<X, VectorFunction<X>> {


  protected var prototype: FieldPrototype<X>
  protected var vector: Vector<Function<X>>
  private val size: Int

  constructor(fieldPrototype: FieldPrototype<X>, vararg fx: Function<X>) {
    prototype = fieldPrototype
    vector = Vector(*fx)
    size = vector.size
  }

  protected constructor(fieldPrototype: FieldPrototype<X>, vfx: Vector<Function<X>>) {
    prototype = fieldPrototype
    vector = vfx
    size = vector.size
  }

  open operator fun get(i: Int) = vector[i]

  override fun diff(ind: Var<X>): VectorFunction<X> {
    val v = ArrayList<Function<X>>(size)
    for (i in 0 until size) v.add(this[i].diff(ind))
    return VectorFunction(prototype, *v.toTypedArray())
  }

  override fun grad(): Map<Var<X>, VectorFunction<X>> {
    TODO("not implemented")
  }

  fun dot(vfx: VectorFunction<X>): Function<X> {
    if (vfx.size != size) throw IllegalArgumentException("$size != ${vfx.size}")
    var norm: Function<X> = Zero(prototype)
    for (i in 0 until size) norm += this[i] * vfx[i]
    return norm
  }

  override fun unaryMinus() = VectorFunction(prototype, -vector)

  override fun plus(addend: VectorFunction<X>) = VectorFunction(prototype, vector + addend.vector)

  override fun minus(subtrahend: VectorFunction<X>) = VectorFunction(prototype, vector - subtrahend.vector)

  override fun times(multiplicand: Long) = VectorFunction(prototype, vector * multiplicand)

  fun times(multiplicand: Function<X>) = VectorFunction(prototype, vector * multiplicand)

  operator fun div(divisor: Function<X>) = VectorFunction(prototype, vector / divisor)
}