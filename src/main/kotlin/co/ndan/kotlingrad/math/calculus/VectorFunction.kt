package co.ndan.kotlingrad.math.calculus

import co.ndan.kotlingrad.math.algebra.*
import co.ndan.kotlingrad.math.types.Vector
import co.ndan.kotlingrad.math.types.Var
import co.ndan.kotlingrad.math.types.Zero
import java.lang.IllegalArgumentException
import java.util.*

open class VectorFunction<X : Field<X>> : AbelianGroup<VectorFunction<X>>, Differentiable<X, VectorFunction<X>> { //, VectorDifferential<X, MatrixFunction<X>> {
  protected var Prototype: FieldPrototype<X>
  protected var vector: Vector<Function<X>>
  private val size: Int

  constructor(fieldPrototype: FieldPrototype<X>, vararg fx: Function<X>) {
    Prototype = fieldPrototype
    vector = Vector(*fx)
    size = vector.size
  }

  protected constructor(fieldPrototype: FieldPrototype<X>, vfx: Vector<Function<X>>) {
    Prototype = fieldPrototype
    vector = vfx
    size = vector.size
  }

  open operator fun get(i: Int) = vector[i]

  override fun differentiate(arg: Var<X>): VectorFunction<X> {
    val v = ArrayList<Function<X>>(size)
    for (i in 0 until size) v.add(this[i].differentiate(arg))
    return VectorFunction(Prototype, *v.toTypedArray())
  }

  fun dot(vfx: VectorFunction<X>): Function<X> {
    if (vfx.size != size) throw IllegalArgumentException("$size != ${vfx.size}")
    var norm: Function<X> = Zero(Prototype)
    for (i in 0 until size) norm += this[i] * vfx[i]
    return norm
  }

  override fun unaryMinus() = VectorFunction(Prototype, -vector)

  override fun plus(addend: VectorFunction<X>) = VectorFunction(Prototype, vector + addend.vector)

  override fun minus(subtrahend: VectorFunction<X>) = VectorFunction(Prototype, vector - subtrahend.vector)

  override fun times(multiplicand: Long) = VectorFunction(Prototype, vector * multiplicand)

  fun times(multiplicand: Function<X>) = VectorFunction(Prototype, vector * multiplicand)

  operator fun div(divisor: Function<X>) = VectorFunction(Prototype, vector / divisor)
}
