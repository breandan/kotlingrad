@file:Suppress("ClassName")
package edu.umontreal.kotlingrad.samples

@Suppress("DuplicatedCode")
fun main() {
  with(DoublePrecision) {
    val x = Var("x", 0.0)
    val y = Var("y", 0.0)

    val f = x pow 2
    println(f(x to 3.0))
    println("f(x) = $f")
    val df_dx = f.diff(x)
    println("f'(x) = $df_dx")

    val g = x pow x
    println("g(x) = $g")
    val dg_dx = g.diff(x)
    println("g'(x) = $dg_dx")

    val h = x + y
    println("h(x) = $h")
    val dh_dx = h.diff(x)
    println("h'(x) = $dh_dx")

    val vf1 = VFun(y + x, y * 2)
    val bh = x * vf1
    val vf2 = VFun(x, y)
    val q = vf1 + vf2
    val z = q(x to 1.0, y to 2.0)
    println("z: $z")
  }
}

/**
 * Vector function.
 */

open class VFun<X: Fun<X>, E: `1`>(
  open val length: Nat<E>,
  val sVars: Set<Var<X>> = emptySet(),
  open val vVars: Set<VVar<X, *>> = emptySet(),
  open vararg val contents: Fun<X>
): List<Fun<X>> by contents.toList() {
  constructor(length: Nat<E>, contents: List<Fun<X>>): this(length, contents.flatMap { it.vars }.toSet(), emptySet(), *contents.toTypedArray())
  constructor(length: Nat<E>, vararg contents: Fun<X>): this(length, contents.flatMap { it.vars }.toSet(), emptySet(), *contents)
  constructor(length: Nat<E>, vararg vFns: VFun<X, E>): this(length, vFns.flatMap { it.sVars }.toSet(), vFns.flatMap { it.vVars }.toSet())

  companion object {
    operator fun <T: Fun<T>> invoke(t: Fun<T>): VFun<T, `1`> = VFun(`1`, arrayListOf(t))
    operator fun <T: Fun<T>> invoke(t0: Fun<T>, t1: Fun<T>): VFun<T, `2`> = VFun(`2`, arrayListOf(t0, t1))
    operator fun <T: Fun<T>> invoke(t0: Fun<T>, t1: Fun<T>, t2: Fun<T>): VFun<T, `3`> = VFun(`3`, arrayListOf(t0, t1, t2))
    operator fun <T: Fun<T>> invoke(t0: Fun<T>, t1: Fun<T>, t2: Fun<T>, t3: Fun<T>): VFun<T, `4`> = VFun(`4`, arrayListOf(t0, t1, t2, t3))
    operator fun <T: Fun<T>> invoke(t0: Fun<T>, t1: Fun<T>, t2: Fun<T>, t3: Fun<T>, t4: Fun<T>): VFun<T, `5`> = VFun(`5`, arrayListOf(t0, t1, t2, t3, t4))
    operator fun <T: Fun<T>> invoke(t0: Fun<T>, t1: Fun<T>, t2: Fun<T>, t3: Fun<T>, t4: Fun<T>, t5: Fun<T>): VFun<T, `6`> = VFun(`6`, arrayListOf(t0, t1, t2, t3, t4, t5))
    operator fun <T: Fun<T>> invoke(t0: Fun<T>, t1: Fun<T>, t2: Fun<T>, t3: Fun<T>, t4: Fun<T>, t5: Fun<T>, t6: Fun<T>): VFun<T, `7`> = VFun(`7`, arrayListOf(t0, t1, t2, t3, t4, t5, t6))
    operator fun <T: Fun<T>> invoke(t0: Fun<T>, t1: Fun<T>, t2: Fun<T>, t3: Fun<T>, t4: Fun<T>, t5: Fun<T>, t6: Fun<T>, t7: Fun<T>): VFun<T, `8`> = VFun(`8`, arrayListOf(t0, t1, t2, t3, t4, t5, t6, t7))
    operator fun <T: Fun<T>> invoke(t0: Fun<T>, t1: Fun<T>, t2: Fun<T>, t3: Fun<T>, t4: Fun<T>, t5: Fun<T>, t6: Fun<T>, t7: Fun<T>, t8: Fun<T>): VFun<T, `9`> = VFun(`9`, arrayListOf(t0, t1, t2, t3, t4, t5, t6, t7, t8))
  }

  init {
    if (length.i != contents.size && contents.isNotEmpty()) throw IllegalArgumentException("Declared length, $length != ${contents.size}")
  }

  val expand: MFun<X, `1`, E> by lazy { MFun(`1`, length, this) }

  operator fun invoke(sMap: Map<Var<X>, X> = emptyMap(), vMap: Map<VVar<X, E>, VConst<X, E>> = emptyMap()): VFun<X, E> =
    when (this) {
      is VNegative<X, E> -> VFun(length, value(sMap, vMap).contents.map { -it })
      is VSum<X, E> -> left(sMap, vMap) + right(sMap, vMap)
      is VVProd<X, E> -> left(sMap, vMap) * right(sMap, vMap)
//    is VDot<X, E> -> VFun(`1`, contents.reduceIndexed { index, acc, element -> acc + element * right[index] })
      is VVar<X, E> -> vMap.getOrElse(this) { this }
      else -> this
    }

  open fun diff(variable: Var<X>): VFun<X, E> =
    when (this) {
      is VConst -> VConst(length, *Array(length.i) { contents.first().zero })
      is VSum -> left.diff(variable) + right.diff(variable)
      is VVProd -> left.diff(variable) * right + right.diff(variable) * left
//    is SVProd -> left.diff(variable) * right + right.diff(variable) * left
      else -> TODO()
    }

  open operator fun unaryMinus(): VFun<X, E> = VNegative(this)
  open operator fun plus(addend: VFun<X, E>): VFun<X, E> = VSum(this, addend)
  open operator fun times(multiplicand: VFun<X, E>): VFun<X, E> = VVProd(this, multiplicand)
  open operator fun times(multiplicand: Fun<X>): VFun<X, E> = VFun(length, contents.map { it * multiplicand })
  open operator fun <Q: `1`> times(multiplicand: MFun<X, E, Q>): VFun<X, Q> = (expand * multiplicand).rows.first()

  infix fun dot(multiplicand: VFun<X, E>): Fun<X> =
    contents.reduceIndexed { index, acc, element -> acc + element * multiplicand[index] }

  override fun toString() = contents.joinToString(", ")
}

class VNegative<X: Fun<X>, E: `1`>(val value: VFun<X, E>): VFun<X, E>(value.length, value)
class VSum<X: Fun<X>, E: `1`>(val left: VFun<X, E>, val right: VFun<X, E>): VFun<X, E>(left.length, left, right)
//class VDot<X: Fun<X>, E: `1`>(val left: VFun<X, E>, val right: VFun<X, E>): Fun<X>(left.vars + right.vars)

class VVProd<X: Fun<X>, E: `1`>(val left: VFun<X, E>, val right: VFun<X, E>): VFun<X, E>(left.length, left, right)
class SVProd<X: Fun<X>, E: `1`>(val left: Fun<X>, val right: VFun<X, E>): VFun<X, E>(right.length, left.vars + right.sVars, right.vVars, *right.contents)

class VVar<X: Fun<X>, E: `1`>(override val name: String, override val length: Nat<E>, vararg val value: X): Variable, VFun<X, E>(length, *value) { override val vVars: Set<VVar<X, *>> = setOf(this) }
open class VConst<X: Fun<X>, E: `1`>(length: Nat<E>, override vararg val contents: SConst<X>): VFun<X, E>(length, *contents)
abstract class RealVector<X: Fun<X>, E: `1`>(length: Nat<E>, override vararg val contents: SConst<X>): VConst<X, E>(length, *contents)
//class VDoubleReal<E: `1`>(length: Nat<E>, override vararg val contents: DoubleReal): RealVector<DoubleReal, E>(length, *contents) {
//  override fun plus(addend: VFun<DoubleReal, E>): VFun<DoubleReal, E> = VDoubleReal(length, *contents.zip(addend.contents).map { (it.first + it.second) }.toTypedArray())
//}

/**
 * Type level integers.
 */
interface Nat<T: `0`> { val i: Int }
sealed class `0`(open val i: Int = 0) {
  companion object: `0`(), Nat<`0`>

  override fun toString() = "$i"
}

sealed class `1`(override val i: Int = 1): `0`(i) { companion object: `1`(), Nat<`1`> }
sealed class `2`(override val i: Int = 2): `1`(i) { companion object: `2`(), Nat<`2`> }
sealed class `3`(override val i: Int = 3): `2`(i) { companion object: `3`(), Nat<`3`> }
sealed class `4`(override val i: Int = 4): `3`(i) { companion object: `4`(), Nat<`4`> }
sealed class `5`(override val i: Int = 5): `4`(i) { companion object: `5`(), Nat<`5`> }
sealed class `6`(override val i: Int = 6): `5`(i) { companion object: `6`(), Nat<`6`> }
sealed class `7`(override val i: Int = 7): `6`(i) { companion object: `7`(), Nat<`7`> }
sealed class `8`(override val i: Int = 8): `7`(i) { companion object: `8`(), Nat<`8`> }
sealed class `9`(override val i: Int = 9): `8`(i) { companion object: `9`(), Nat<`9`> }