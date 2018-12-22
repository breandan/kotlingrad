package co.ndan.kotlingrad.math.functions

import co.ndan.kotlingrad.math.algebra.*
import co.ndan.kotlingrad.math.calculus.Differentiable
import co.ndan.kotlingrad.math.types.*
import co.ndan.kotlingrad.math.types.Vector
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

  override fun differentiate(ind: Var<X>): VectorFunction<X> {
    val v = ArrayList<Function<X>>(size)
    for (i in 0 until size) v.add(this[i].differentiate(ind))
    return VectorFunction(prototype, *v.toTypedArray())
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