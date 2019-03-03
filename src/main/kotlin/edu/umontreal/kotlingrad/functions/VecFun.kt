package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.AbelianGroup
import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.dependent.*
import java.util.*

// VFun should not be a List or the concatenation operator + will conflict with vector addition
open class VecFun<X: Field<X>, MaxLength : `100`>(open val length: Nat<MaxLength>, val contents: ArrayList<X>): AbelianGroup<VecFun<X, MaxLength>>, List<X> by contents {
  init {
    if (length.i != contents.size) throw IllegalArgumentException("Declared $length, but found ${contents.size}")
  }
//  val size
//    get() = length.i

//  operator fun get(i: Int): E = contents[i]

  companion object {
    operator fun <T: Field<T>> invoke(): VecFun<T, `0`> = VecFun(`0`, arrayListOf())
    operator fun <T: Field<T>> invoke(t: T): VecFun<T, `1`> = VecFun(`1`, arrayListOf(t))
    operator fun <T: Field<T>> invoke(t0: T, t1: T): VecFun<T, `2`> = VecFun(`2`, arrayListOf(t0, t1))
    operator fun <T: Field<T>> invoke(t0: T, t1: T, t2: T): VecFun<T, `3`> = VecFun(`3`, arrayListOf(t0, t1, t2))
    operator fun <T: Field<T>> invoke(t0: T, t1: T, t2: T, t3: T): VecFun<T, `4`> = VecFun(`4`, arrayListOf(t0, t1, t2, t3))
    operator fun <T: Field<T>> invoke(t0: T, t1: T, t2: T, t3: T, t4: T): VecFun<T, `5`> = VecFun(`5`, arrayListOf(t0, t1, t2, t3, t4))
    operator fun <T: Field<T>> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T): VecFun<T, `6`> = VecFun(`6`, arrayListOf(t0, t1, t2, t3, t4, t5))
    operator fun <T: Field<T>> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T): VecFun<T, `7`> = VecFun(`7`, arrayListOf(t0, t1, t2, t3, t4, t5, t6))
    operator fun <T: Field<T>> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T): VecFun<T, `8`> = VecFun(`8`, arrayListOf(t0, t1, t2, t3, t4, t5, t6, t7))
    operator fun <T: Field<T>> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T): VecFun<T, `9`> = VecFun(`9`, arrayListOf(t0, t1, t2, t3, t4, t5, t6, t7, t8))
  }

  override fun toString() = "$contents"
  override val one: VecFun<X, MaxLength>
    get() = TODO("not implemented")
  override val zero: VecFun<X, MaxLength>
    get() = TODO("not implemented")

  constructor(length: Nat<MaxLength>, vector: Collection<X>): this(length, ArrayList<X>(vector.size).apply { addAll(vector) })
  constructor(length: Nat<MaxLength>, vararg vector: X): this(length, arrayListOf(*vector))

  fun dot(vx: VecFun<X, MaxLength>) =
      if (size != vx.size && size > 0) throw IllegalArgumentException("$size != ${vx.size}")
      else zip(vx).map { it.first * it.second }.reduce { acc, it -> acc + it }

  override fun unaryMinus() = VecFun(length, map { -it })

  override fun plus(addend: VecFun<X, MaxLength>): VecFun<X, MaxLength> =
      if (size != addend.size) throw IllegalArgumentException("$size != ${addend.size}")
      else VecFun(length, mapIndexedTo(ArrayList(size)) { index, value -> value + addend[index] })

  override fun minus(subtrahend: VecFun<X, MaxLength>) =
      if (size != subtrahend.size) throw IllegalArgumentException("$size != ${subtrahend.size}")
      else VecFun(length, mapIndexedTo(ArrayList(size)) { index, value -> value - subtrahend[index] })

  override fun times(multiplicand: VecFun<X, MaxLength>) = VecFun(length, zip(multiplicand).map { it.first * it.second })

  infix operator fun times(multiplicand: X) = VecFun(length, map { it * multiplicand })

  infix operator fun div(divisor: X) = VecFun(length, map { it / divisor })

//  class VConst<X : Field<X>, MaxLength: `100`>(override val length: Nat<MaxLength>, vararg contents: Fun.Const<X>) : VFun<X, MaxLength>(length, *contents)
}