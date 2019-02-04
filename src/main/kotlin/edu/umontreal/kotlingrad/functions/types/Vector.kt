package edu.umontreal.kotlingrad.functions.types

import edu.umontreal.kotlingrad.algebra.AbelianGroup
import edu.umontreal.kotlingrad.algebra.Field
import java.util.*

open class Vector<X: Field<X>>(val vector: ArrayList<X>): AbelianGroup<Vector<X>>, List<X> by vector {
  override val one: Vector<X>
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
  override val zero: Vector<X>
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

  constructor(vector: Collection<X>): this(ArrayList<X>(vector.size).apply { addAll(vector) })
  constructor(vararg vector: X): this(arrayListOf(*vector))

  fun dot(vx: Vector<X>) =
      if (size != vx.size && size > 0) throw IllegalArgumentException("$size != ${vx.size}")
      else zip(vx).map { it.first * it.second }.reduce { acc, it -> acc + it }

  override fun unaryMinus() = Vector(map { -it })

  override fun plus(addend: Vector<X>): Vector<X> =
      if (size != addend.size) throw IllegalArgumentException("$size != ${addend.size}")
      else Vector(mapIndexedTo(ArrayList(size)) { index, value -> value + addend[index] } as ArrayList<X>)

  override fun minus(subtrahend: Vector<X>) =
      if (size != subtrahend.size) throw IllegalArgumentException("$size != ${subtrahend.size}")
      else Vector(mapIndexedTo(ArrayList(size)) { index, value -> value - subtrahend[index] } as ArrayList<X>)

  override fun times(multiplicand: Vector<X>) = Vector(zip(multiplicand).map { it.first * it.second })

  infix operator fun times(multiplicand: X) = Vector(map { it * multiplicand })

  infix operator fun div(divisor: X) = Vector(map { it / divisor })
}