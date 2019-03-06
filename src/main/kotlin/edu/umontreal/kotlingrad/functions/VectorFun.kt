package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.AbelianGroup
import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.dependent.*
import java.util.*

// VFun should not be a List or the concatenation operator + will conflict with vector addition
open class VectorFun<X: Field<X>, MaxLength : `100`>(open val length: Nat<MaxLength>, val contents: ArrayList<X>): AbelianGroup<VectorFun<X, MaxLength>>, List<X> by contents {
  init {
    if (length.i != contents.size) throw IllegalArgumentException("Declared $length, but found ${contents.size}")
  }

//  val size
//    get() = length.i

//  operator fun get(i: Int): E = contents[i]

  companion object {
    operator fun <T: Field<T>> invoke(): VectorFun<T, `0`> = VectorFun(`0`, arrayListOf())
    operator fun <T: Field<T>> invoke(t: T): VectorFun<T, `1`> = VectorFun(`1`, arrayListOf(t))
    operator fun <T: Field<T>> invoke(t0: T, t1: T): VectorFun<T, `2`> = VectorFun(`2`, arrayListOf(t0, t1))
    operator fun <T: Field<T>> invoke(t0: T, t1: T, t2: T): VectorFun<T, `3`> = VectorFun(`3`, arrayListOf(t0, t1, t2))
    operator fun <T: Field<T>> invoke(t0: T, t1: T, t2: T, t3: T): VectorFun<T, `4`> = VectorFun(`4`, arrayListOf(t0, t1, t2, t3))
    operator fun <T: Field<T>> invoke(t0: T, t1: T, t2: T, t3: T, t4: T): VectorFun<T, `5`> = VectorFun(`5`, arrayListOf(t0, t1, t2, t3, t4))
    operator fun <T: Field<T>> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T): VectorFun<T, `6`> = VectorFun(`6`, arrayListOf(t0, t1, t2, t3, t4, t5))
    operator fun <T: Field<T>> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T): VectorFun<T, `7`> = VectorFun(`7`, arrayListOf(t0, t1, t2, t3, t4, t5, t6))
    operator fun <T: Field<T>> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T): VectorFun<T, `8`> = VectorFun(`8`, arrayListOf(t0, t1, t2, t3, t4, t5, t6, t7))
    operator fun <T: Field<T>> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T): VectorFun<T, `9`> = VectorFun(`9`, arrayListOf(t0, t1, t2, t3, t4, t5, t6, t7, t8))
  }

  override fun toString() = "$contents"
  override val one: VectorFun<X, MaxLength>
    get() = TODO("not implemented")
  override val zero: VectorFun<X, MaxLength>
    get() = TODO("not implemented")

  constructor(length: Nat<MaxLength>, vector: Collection<X>): this(length, ArrayList<X>(vector.size).apply { addAll(vector) })
  constructor(length: Nat<MaxLength>, vararg vector: X): this(length, arrayListOf(*vector))

  fun dot(vx: VectorFun<X, MaxLength>) =
      if (size != vx.size && size > 0) throw IllegalArgumentException("$size != ${vx.size}")
      else zip(vx).map { it.first * it.second }.reduce { acc, it -> acc + it }

  override fun unaryMinus() = VectorFun(length, map { -it })

  override fun plus(addend: VectorFun<X, MaxLength>): VectorFun<X, MaxLength> =
      if (size != addend.size) throw IllegalArgumentException("$size != ${addend.size}")
      else VectorFun(length, mapIndexedTo(ArrayList(size)) { index, value -> value + addend[index] })

  override fun minus(subtrahend: VectorFun<X, MaxLength>) =
      if (size != subtrahend.size) throw IllegalArgumentException("$size != ${subtrahend.size}")
      else VectorFun(length, mapIndexedTo(ArrayList(size)) { index, value -> value - subtrahend[index] })

  override fun times(multiplicand: VectorFun<X, MaxLength>) = VectorFun(length, zip(multiplicand).map { it.first * it.second })

  infix operator fun times(multiplicand: X) = VectorFun(length, map { it * multiplicand })

  infix operator fun div(divisor: X) = VectorFun(length, map { it / divisor })

//  operator fun times(scalarFun: ScalarFun<X>): VectorFun<X, MaxLength> = VectorFun(contents.map { it * scalarFun })

//  class VConst<X : Field<X>, MaxLength: `100`>(override val length: Nat<MaxLength>, vararg contents: ScalarFun.ScalarConst<X>) : VFun<X, MaxLength>(length, *contents)
}