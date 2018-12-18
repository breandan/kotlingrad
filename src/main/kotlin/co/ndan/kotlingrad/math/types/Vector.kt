package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.AbelianGroup
import co.ndan.kotlingrad.math.algebra.Field
import java.util.*

class Vector<X: Field<X>>(val vector: ArrayList<X>): AbelianGroup<Vector<X>> {
  constructor(vector: Collection<X>): this(ArrayList<X>(vector.size).apply { addAll(vector) })
  constructor(vararg vector: X): this(arrayListOf(*vector))

  val size = vector.size

  operator fun get(i: Int) = vector[i]

  fun dot(vx: Vector<X>): X {
    if (size != vx.size && size > 0) throw IllegalArgumentException("$size != ${vx.size}")
    var ret = this[0] * vx[0]
    for (i in 1 until size) ret += get(i) * vx[i]
    return ret
  }

  override fun unaryMinus(): Vector<X> {
    val v = ArrayList<X>(size)
    for (i in 0 until size) v += -this[i]
    return Vector(v)
  }

  override fun plus(addend: Vector<X>): Vector<X> {
    if (size != addend.size) throw IllegalArgumentException("$size != ${addend.size}")
    val v = ArrayList<X>(size)
    for (i in 0 until size) v += this[i] + addend[i]
    return Vector(v)
  }

  override fun minus(subtrahend: Vector<X>): Vector<X> {
    if (size != subtrahend.size) throw IllegalArgumentException("$size != ${subtrahend.size}")
    val v = ArrayList<X>(size)
    for (i in 0 until size) v += this[i] - subtrahend[i]
    return Vector(v)
  }

  override fun times(multiplicand: Long): Vector<X> {
    val v = ArrayList<X>(size)
    for (i in 0 until size) v += this[i] * multiplicand
    return Vector(v)
  }

  operator fun times(multiplicand: X): Vector<X> {
    val v = ArrayList<X>(size)
    for (i in 0 until size) v += this[i] * multiplicand
    return Vector(v)
  }

  operator fun div(divisor: X): Vector<X> {
    val v = ArrayList<X>(size)
    for (i in 0 until size) v += this[i] / divisor
    return Vector(v)
  }
}