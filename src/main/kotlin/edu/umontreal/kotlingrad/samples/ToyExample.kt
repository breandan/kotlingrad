@file:Suppress("ClassName")

package edu.umontreal.kotlingrad.samples

import kotlin.math.E
import kotlin.math.ln
import kotlin.math.pow

fun main() {
  with(DoublePrecision) {
    val x = SVar("x", DoubleReal(0.0))
    val y = SVar("y", DoubleReal(0.0))

    val f = x pow 2
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
    val z = q(mapOf(x to DoubleReal(1.0), y to DoubleReal(2.0)))
    println("z: $z")

    val mf1 = MFun(`2`, `1`, VFun(`1`, y * y), VFun(`1`, x * y))
    val mf2 = MFun(`1`, `2`, vf2)
    val mf3 = MFun(`3`, `2`, VFun(`2`, x, x), VFun(`2`, y, x), VFun(`2`, x, x))
    println(mf1 * mf2) // 2*1 x 1*2
//  println(mf1 * vf1) // 2*1 x 2
    println(mf2 * vf1) // 1*2 x 2
    println(mf3 * vf1) // 3*2 x 2
//  println(mf3 * mf3)   // 3*2 x 3*2
  }
}

/**
 * Algebraic primitives.
 */

interface Group<X: Group<X>> {
  operator fun unaryMinus(): X
  operator fun plus(addend: X): X
  operator fun minus(subtrahend: X): X = this + -subtrahend
  operator fun times(multiplicand: X): X
}

interface Field<X: Field<X>>: Group<X> {
  operator fun div(dividend: X): X = this * dividend.pow(-one)
  infix fun pow(exp: X): X
  fun ln(): X
  val e: X
  val one: X
  val zero: X
}

/**
 * Scalar function.
 */

sealed class SFun<X: SFun<X>>(open val vars: Set<SVar<X>> = emptySet()): Field<SFun<X>> {
  constructor(fn: SFun<X>): this(fn.vars)
  constructor(vararg fns: SFun<X>): this(fns.flatMap { it.vars }.toSet())

  override operator fun plus(addend: SFun<X>): SFun<X> = Sum(this, addend)
  override operator fun times(multiplicand: SFun<X>): SFun<X> = Prod(this, multiplicand)

  operator fun <E: `1`> times(multiplicand: VFun<X, E>): VFun<X, E> = SVProd(this, multiplicand)

  operator fun invoke(map: Map<SVar<X>, X>): SFun<X> = when (this) {
    is Const -> this
    is SVar -> map.getOrElse(this) { this }
    is Prod -> left(map) * right(map)
    is Sum -> left(map) + right(map)
    is Power -> base(map) pow exponent(map)
    is Negative -> -value(map)
    is Log -> logarithmand(map).ln()
  }

  open fun diff(variable: SVar<X>): SFun<X> = when (this) {
    is SVar -> if (variable == this) one else zero
    is Const -> zero
    is Sum -> left.diff(variable) + right.diff(variable)
    is Prod -> left.diff(variable) * right + left * right.diff(variable)
    is Power -> this * (exponent * Log(base)).diff(variable)
    is Negative -> -value.diff(variable)
    is Log -> logarithmand.pow(-one) * logarithmand.diff(variable)
  }

  override fun ln(): SFun<X> = Log(this)

  override fun pow(exp: SFun<X>): SFun<X> = Power(this, exp)

  override fun unaryMinus(): SFun<X> = Negative(this)

  override val e: Const<X> by lazy { proto.e }
  override val one: Const<X> by lazy { proto.one }
  override val zero: Const<X> by lazy { proto.zero }
  private val proto: X by lazy { vars.first().value }

  override fun toString(): String = when {
    this is Log -> "ln($logarithmand)"
    this is Negative -> "-$value"
    this is Power -> "$base^($exponent)"
    this is Prod && right is Sum -> "$left⋅($right)"
    this is Prod && left is Sum -> "($left)⋅$right"
    this is Prod -> "$left⋅$right"
    this is Sum && right is Negative -> "$left - ${right.value}"
    this is Sum -> "$left + $right"
    this is SVar -> name
    else -> super.toString()
  }
}

/**
 * Symbolic operators.
 */

class Sum<X: SFun<X>>(val left: SFun<X>, val right: SFun<X>): SFun<X>(left, right)
class Negative<X: SFun<X>>(val value: SFun<X>): SFun<X>(value)
class Prod<X: SFun<X>>(val left: SFun<X>, val right: SFun<X>): SFun<X>(left, right)
class Power<X: SFun<X>> internal constructor(val base: SFun<X>, val exponent: SFun<X>): SFun<X>(base, exponent)
class Log<X: SFun<X>> internal constructor(val logarithmand: SFun<X>): SFun<X>(logarithmand)
interface Variable {
  val name: String
}
class SVar<X: SFun<X>>(override val name: String, val value: X): Variable, SFun<X>() { override val vars: Set<SVar<X>> = setOf(this) }

open class Const<X: SFun<X>>: SFun<X>()
abstract class RealNumber<X: SFun<X>>(open val value: Number): Const<X>()

/**
 * Constant propagation.
 */

class DoubleReal(override val value: Double): RealNumber<DoubleReal>(value) {
  override val e by lazy { DoubleReal(E) }
  override val one by lazy { DoubleReal(1.0) }
  override val zero by lazy { DoubleReal(0.0) }

  override fun plus(addend: SFun<DoubleReal>): SFun<DoubleReal> = when (addend) {
    is DoubleReal -> DoubleReal(value + addend.value)
    else -> super.plus(addend)
  }

  override fun unaryMinus() = DoubleReal(-value)

  override fun times(multiplicand: SFun<DoubleReal>): SFun<DoubleReal> = when (multiplicand) {
    is DoubleReal -> DoubleReal(value * multiplicand.value)
    else -> super.times(multiplicand)
  }

  override fun pow(exp: SFun<DoubleReal>) = when (exp) {
    is DoubleReal -> DoubleReal(value.pow(exp.value))
    else -> super.pow(exp)
  }

  override fun ln() = DoubleReal(ln(value))
  override fun toString() = value.toString()
}

/**
 * Vector function.
 */

open class VFun<X: SFun<X>, E: `1`>(
  open val length: Nat<E>,
  val sVars: Set<SVar<X>> = emptySet(),
  open val vVars: Set<VVar<X, *>> = emptySet(),
  open vararg val contents: SFun<X>
): List<SFun<X>> by contents.toList() {
  constructor(length: Nat<E>, contents: List<SFun<X>>): this(length, contents.flatMap { it.vars }.toSet(), emptySet(), *contents.toTypedArray())
  constructor(length: Nat<E>, vararg contents: SFun<X>): this(length, contents.flatMap { it.vars }.toSet(), emptySet(), *contents)
  constructor(length: Nat<E>, vararg vFns: VFun<X, E>): this(length, vFns.flatMap { it.sVars }.toSet(), vFns.flatMap { it.vVars }.toSet())

  companion object {
    operator fun <T: SFun<T>> invoke(t: SFun<T>): VFun<T, `1`> = VFun(`1`, arrayListOf(t))
    operator fun <T: SFun<T>> invoke(t0: SFun<T>, t1: SFun<T>): VFun<T, `2`> = VFun(`2`, arrayListOf(t0, t1))
    operator fun <T: SFun<T>> invoke(t0: SFun<T>, t1: SFun<T>, t2: SFun<T>): VFun<T, `3`> = VFun(`3`, arrayListOf(t0, t1, t2))
    operator fun <T: SFun<T>> invoke(t0: SFun<T>, t1: SFun<T>, t2: SFun<T>, t3: SFun<T>): VFun<T, `4`> = VFun(`4`, arrayListOf(t0, t1, t2, t3))
    operator fun <T: SFun<T>> invoke(t0: SFun<T>, t1: SFun<T>, t2: SFun<T>, t3: SFun<T>, t4: SFun<T>): VFun<T, `5`> = VFun(`5`, arrayListOf(t0, t1, t2, t3, t4))
    operator fun <T: SFun<T>> invoke(t0: SFun<T>, t1: SFun<T>, t2: SFun<T>, t3: SFun<T>, t4: SFun<T>, t5: SFun<T>): VFun<T, `6`> = VFun(`6`, arrayListOf(t0, t1, t2, t3, t4, t5))
    operator fun <T: SFun<T>> invoke(t0: SFun<T>, t1: SFun<T>, t2: SFun<T>, t3: SFun<T>, t4: SFun<T>, t5: SFun<T>, t6: SFun<T>): VFun<T, `7`> = VFun(`7`, arrayListOf(t0, t1, t2, t3, t4, t5, t6))
    operator fun <T: SFun<T>> invoke(t0: SFun<T>, t1: SFun<T>, t2: SFun<T>, t3: SFun<T>, t4: SFun<T>, t5: SFun<T>, t6: SFun<T>, t7: SFun<T>): VFun<T, `8`> = VFun(`8`, arrayListOf(t0, t1, t2, t3, t4, t5, t6, t7))
    operator fun <T: SFun<T>> invoke(t0: SFun<T>, t1: SFun<T>, t2: SFun<T>, t3: SFun<T>, t4: SFun<T>, t5: SFun<T>, t6: SFun<T>, t7: SFun<T>, t8: SFun<T>): VFun<T, `9`> = VFun(`9`, arrayListOf(t0, t1, t2, t3, t4, t5, t6, t7, t8))
  }

  init {
    if (length.i != contents.size && contents.isNotEmpty()) throw IllegalArgumentException("Declared length, $length != ${contents.size}")
  }

  val expand: MFun<X, `1`, E> by lazy { MFun(`1`, length, this) }

  operator fun invoke(map: Map<SVar<X>, X> = emptyMap(),
                      vMap: Map<VVar<X, *>, VConst<X, *>> = emptyMap()): VFun<X, E> =
    when (this) {
      is VNegative<X, E> -> VFun(length, value(map).contents.map { -it })
      is VSum<X, E> -> VFun(length, left(map).contents.zip(right(map)).map { it.first + it.second })
      is VVProd<X, E> -> VFun(length, left(map).contents.zip(right(map)).map { it.first * it.second })
//    is VDot<X, E> -> VFun(`1`, contents.reduceIndexed { index, acc, element -> acc + element * right[index] })
      else -> VFun(length, contents.map { it(map) })
    }

  open operator fun unaryMinus(): VFun<X, E> = VNegative(this)
  open operator fun plus(addend: VFun<X, E>): VFun<X, E> = VSum(this, addend)
  open operator fun times(multiplicand: VFun<X, E>): VFun<X, E> = VVProd(this, multiplicand)
  open operator fun times(multiplicand: SFun<X>): VFun<X, E> = VFun(length, contents.map { it * multiplicand })
  open operator fun <Q: `1`> times(multiplicand: MFun<X, E, Q>): VFun<X, Q> = (expand * multiplicand).rows.first()

  infix fun dot(multiplicand: VFun<X, E>): SFun<X> =
    contents.reduceIndexed { index, acc, element -> acc + element * multiplicand[index] }

  override fun toString() = contents.joinToString(", ")
}

class VNegative<X: SFun<X>, E: `1`>(val value: VFun<X, E>): VFun<X, E>(value.length, value)
class VSum<X: SFun<X>, E: `1`>(val left: VFun<X, E>, val right: VFun<X, E>): VFun<X, E>(left.length, left, right)
//class VDot<X: SFun<X>, E: `1`>(val left: VFun<X, E>, val right: VFun<X, E>): SFun<X>(left.vars + right.vars)

class VVProd<X: SFun<X>, E: `1`>(val left: VFun<X, E>, val right: VFun<X, E>): VFun<X, E>(left.length, left, right)
class SVProd<X: SFun<X>, E: `1`>(val left: SFun<X>, val right: VFun<X, E>): VFun<X, E>(right.length, left.vars + right.sVars, right.vVars, *right.contents)

class VVar<X: SFun<X>, E: `1`>(override val name: String, override val length: Nat<E>, vararg val value: X): Variable, VFun<X, E>(length, *value) { override val vVars: Set<VVar<X, *>> = setOf(this) }
open class VConst<X: SFun<X>, E: `1`>(length: Nat<E>, override vararg val contents: Const<X>): VFun<X, E>(length, *contents)
abstract class RealVector<X: SFun<X>, E: `1`>(length: Nat<E>, override vararg val contents: Const<X>): VConst<X, E>(length, *contents)
//class VDoubleReal<E: `1`>(length: Nat<E>, override vararg val contents: DoubleReal): RealVector<DoubleReal, E>(length, *contents) {
//  override fun plus(addend: VFun<DoubleReal, E>): VFun<DoubleReal, E> = VDoubleReal(length, *contents.zip(addend.contents).map { (it.first + it.second) }.toTypedArray())
//}

/**
 * Matrix function.
 */

open class MFun<X: SFun<X>, R: `1`, C: `1`>(
  val numRows: Nat<R>,
  val numCols: Nat<C>,
  val sVars: Set<SVar<X>> = emptySet(),
  val vVars: Set<VVar<X, *>> = emptySet(),
  open val mVars: Set<MVar<X, *, *>> = emptySet(),
  open vararg val rows: VFun<X, C>
): List<VFun<X, C>> by rows.toList() {
  constructor(numRows: Nat<R>, numCols: Nat<C>, contents: List<VFun<X, C>>):
    this(numRows, numCols, contents.flatMap { it.sVars }.toSet(), contents.flatMap { it.vVars }.toSet(), emptySet(), *contents.toTypedArray())
  constructor(numRows: Nat<R>, numCols: Nat<C>, vararg rows: VFun<X, C>):
    this(numRows, numCols, rows.flatMap { it.sVars }.toSet(), rows.flatMap { it.vVars }.toSet(), emptySet(), *rows)

  constructor(left: MFun<X, R, *>, right: MFun<X, *, C>): this(left.numRows, right.numCols, left.sVars + right.sVars, left.vVars + right.vVars, left.mVars + right.mVars)
  constructor(mFun: MFun<X, R, C>): this(mFun.numRows, mFun.numCols, mFun.sVars, mFun.vVars, mFun.mVars)

  init {
    if (numRows.i != rows.size) throw IllegalArgumentException("Declared rows, $numRows != ${rows.size}")
  }

  companion object {
    @JvmName("m1x1") private operator fun <T: SFun<T>, V: VFun<T, `1`>> invoke(t0: V): MFun<T, `1`, `1`> = MFun(`1`, `1`, t0)
    @JvmName("m1x2") private operator fun <T: SFun<T>, V: VFun<T, `2`>> invoke(t0: V): MFun<T, `1`, `2`> = MFun(`1`, `2`, t0)
    @JvmName("m1x3") private operator fun <T: SFun<T>, V: VFun<T, `3`>> invoke(t0: V): MFun<T, `1`, `3`> = MFun(`1`, `3`, t0)
    @JvmName("m1x4") private operator fun <T: SFun<T>, V: VFun<T, `4`>> invoke(t0: V): MFun<T, `1`, `4`> = MFun(`1`, `4`, t0)
    @JvmName("m1x5") private operator fun <T: SFun<T>, V: VFun<T, `5`>> invoke(t0: V): MFun<T, `1`, `5`> = MFun(`1`, `5`, t0)
    @JvmName("m1x6") private operator fun <T: SFun<T>, V: VFun<T, `6`>> invoke(t0: V): MFun<T, `1`, `6`> = MFun(`1`, `6`, t0)
    @JvmName("m1x7") private operator fun <T: SFun<T>, V: VFun<T, `7`>> invoke(t0: V): MFun<T, `1`, `7`> = MFun(`1`, `7`, t0)
    @JvmName("m1x8") private operator fun <T: SFun<T>, V: VFun<T, `8`>> invoke(t0: V): MFun<T, `1`, `8`> = MFun(`1`, `8`, t0)
    @JvmName("m1x9") private operator fun <T: SFun<T>, V: VFun<T, `9`>> invoke(t0: V): MFun<T, `1`, `9`> = MFun(`1`, `9`, t0)
    @JvmName("m2x1") private operator fun <T: SFun<T>, V: VFun<T, `1`>> invoke(t0: V, t1: V): MFun<T, `2`, `1`> = MFun(`2`, `1`, t0, t1)
    @JvmName("m2x2") private operator fun <T: SFun<T>, V: VFun<T, `2`>> invoke(t0: V, t1: V): MFun<T, `2`, `2`> = MFun(`2`, `2`, t0, t1)
    @JvmName("m2x3") private operator fun <T: SFun<T>, V: VFun<T, `3`>> invoke(t0: V, t1: V): MFun<T, `2`, `3`> = MFun(`2`, `3`, t0, t1)
    @JvmName("m2x4") private operator fun <T: SFun<T>, V: VFun<T, `4`>> invoke(t0: V, t1: V): MFun<T, `2`, `4`> = MFun(`2`, `4`, t0, t1)
    @JvmName("m2x5") private operator fun <T: SFun<T>, V: VFun<T, `5`>> invoke(t0: V, t1: V): MFun<T, `2`, `5`> = MFun(`2`, `5`, t0, t1)
    @JvmName("m2x6") private operator fun <T: SFun<T>, V: VFun<T, `6`>> invoke(t0: V, t1: V): MFun<T, `2`, `6`> = MFun(`2`, `6`, t0, t1)
    @JvmName("m2x7") private operator fun <T: SFun<T>, V: VFun<T, `7`>> invoke(t0: V, t1: V): MFun<T, `2`, `7`> = MFun(`2`, `7`, t0, t1)
    @JvmName("m2x8") private operator fun <T: SFun<T>, V: VFun<T, `8`>> invoke(t0: V, t1: V): MFun<T, `2`, `8`> = MFun(`2`, `8`, t0, t1)
    @JvmName("m2x9") private operator fun <T: SFun<T>, V: VFun<T, `9`>> invoke(t0: V, t1: V): MFun<T, `2`, `9`> = MFun(`2`, `9`, t0, t1)
    @JvmName("m3x1") private operator fun <T: SFun<T>, V: VFun<T, `1`>> invoke(t0: V, t1: V, t2: V): MFun<T, `3`, `1`> = MFun(`3`, `1`, t0, t1, t2)
    @JvmName("m3x2") private operator fun <T: SFun<T>, V: VFun<T, `2`>> invoke(t0: V, t1: V, t2: V): MFun<T, `3`, `2`> = MFun(`3`, `2`, t0, t1, t2)
    @JvmName("m3x3") private operator fun <T: SFun<T>, V: VFun<T, `3`>> invoke(t0: V, t1: V, t2: V): MFun<T, `3`, `3`> = MFun(`3`, `3`, t0, t1, t2)
    @JvmName("m3x4") private operator fun <T: SFun<T>, V: VFun<T, `4`>> invoke(t0: V, t1: V, t2: V): MFun<T, `3`, `4`> = MFun(`3`, `4`, t0, t1, t2)
    @JvmName("m3x5") private operator fun <T: SFun<T>, V: VFun<T, `5`>> invoke(t0: V, t1: V, t2: V): MFun<T, `3`, `5`> = MFun(`3`, `5`, t0, t1, t2)
    @JvmName("m3x6") private operator fun <T: SFun<T>, V: VFun<T, `6`>> invoke(t0: V, t1: V, t2: V): MFun<T, `3`, `6`> = MFun(`3`, `6`, t0, t1, t2)
    @JvmName("m3x7") private operator fun <T: SFun<T>, V: VFun<T, `7`>> invoke(t0: V, t1: V, t2: V): MFun<T, `3`, `7`> = MFun(`3`, `7`, t0, t1, t2)
    @JvmName("m3x8") private operator fun <T: SFun<T>, V: VFun<T, `8`>> invoke(t0: V, t1: V, t2: V): MFun<T, `3`, `8`> = MFun(`3`, `8`, t0, t1, t2)
    @JvmName("m3x9") private operator fun <T: SFun<T>, V: VFun<T, `9`>> invoke(t0: V, t1: V, t2: V): MFun<T, `3`, `9`> = MFun(`3`, `9`, t0, t1, t2)
    @JvmName("m4x1") private operator fun <T: SFun<T>, V: VFun<T, `1`>> invoke(t0: V, t1: V, t2: V, t3: V): MFun<T, `4`, `1`> = MFun(`4`, `1`, t0, t1, t2, t3)
    @JvmName("m4x2") private operator fun <T: SFun<T>, V: VFun<T, `2`>> invoke(t0: V, t1: V, t2: V, t3: V): MFun<T, `4`, `2`> = MFun(`4`, `2`, t0, t1, t2, t3)
    @JvmName("m4x3") private operator fun <T: SFun<T>, V: VFun<T, `3`>> invoke(t0: V, t1: V, t2: V, t3: V): MFun<T, `4`, `3`> = MFun(`4`, `3`, t0, t1, t2, t3)
    @JvmName("m4x4") private operator fun <T: SFun<T>, V: VFun<T, `4`>> invoke(t0: V, t1: V, t2: V, t3: V): MFun<T, `4`, `4`> = MFun(`4`, `4`, t0, t1, t2, t3)
    @JvmName("m4x5") private operator fun <T: SFun<T>, V: VFun<T, `5`>> invoke(t0: V, t1: V, t2: V, t3: V): MFun<T, `4`, `5`> = MFun(`4`, `5`, t0, t1, t2, t3)
    @JvmName("m4x6") private operator fun <T: SFun<T>, V: VFun<T, `6`>> invoke(t0: V, t1: V, t2: V, t3: V): MFun<T, `4`, `6`> = MFun(`4`, `6`, t0, t1, t2, t3)
    @JvmName("m4x7") private operator fun <T: SFun<T>, V: VFun<T, `7`>> invoke(t0: V, t1: V, t2: V, t3: V): MFun<T, `4`, `7`> = MFun(`4`, `7`, t0, t1, t2, t3)
    @JvmName("m4x8") private operator fun <T: SFun<T>, V: VFun<T, `8`>> invoke(t0: V, t1: V, t2: V, t3: V): MFun<T, `4`, `8`> = MFun(`4`, `8`, t0, t1, t2, t3)
    @JvmName("m4x9") private operator fun <T: SFun<T>, V: VFun<T, `9`>> invoke(t0: V, t1: V, t2: V, t3: V): MFun<T, `4`, `9`> = MFun(`4`, `9`, t0, t1, t2, t3)
    @JvmName("m5x1") private operator fun <T: SFun<T>, V: VFun<T, `1`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V): MFun<T, `5`, `1`> = MFun(`5`, `1`, t0, t1, t2, t3, t4)
    @JvmName("m5x2") private operator fun <T: SFun<T>, V: VFun<T, `2`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V): MFun<T, `5`, `2`> = MFun(`5`, `2`, t0, t1, t2, t3, t4)
    @JvmName("m5x3") private operator fun <T: SFun<T>, V: VFun<T, `3`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V): MFun<T, `5`, `3`> = MFun(`5`, `3`, t0, t1, t2, t3, t4)
    @JvmName("m5x4") private operator fun <T: SFun<T>, V: VFun<T, `4`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V): MFun<T, `5`, `4`> = MFun(`5`, `4`, t0, t1, t2, t3, t4)
    @JvmName("m5x5") private operator fun <T: SFun<T>, V: VFun<T, `5`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V): MFun<T, `5`, `5`> = MFun(`5`, `5`, t0, t1, t2, t3, t4)
    @JvmName("m5x6") private operator fun <T: SFun<T>, V: VFun<T, `6`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V): MFun<T, `5`, `6`> = MFun(`5`, `6`, t0, t1, t2, t3, t4)
    @JvmName("m5x7") private operator fun <T: SFun<T>, V: VFun<T, `7`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V): MFun<T, `5`, `7`> = MFun(`5`, `7`, t0, t1, t2, t3, t4)
    @JvmName("m5x8") private operator fun <T: SFun<T>, V: VFun<T, `8`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V): MFun<T, `5`, `8`> = MFun(`5`, `8`, t0, t1, t2, t3, t4)
    @JvmName("m5x9") private operator fun <T: SFun<T>, V: VFun<T, `9`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V): MFun<T, `5`, `9`> = MFun(`5`, `9`, t0, t1, t2, t3, t4)
    @JvmName("m6x1") private operator fun <T: SFun<T>, V: VFun<T, `1`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V): MFun<T, `6`, `1`> = MFun(`6`, `1`, t0, t1, t2, t3, t4, t5)
    @JvmName("m6x2") private operator fun <T: SFun<T>, V: VFun<T, `2`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V): MFun<T, `6`, `2`> = MFun(`6`, `2`, t0, t1, t2, t3, t4, t5)
    @JvmName("m6x3") private operator fun <T: SFun<T>, V: VFun<T, `3`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V): MFun<T, `6`, `3`> = MFun(`6`, `3`, t0, t1, t2, t3, t4, t5)
    @JvmName("m6x4") private operator fun <T: SFun<T>, V: VFun<T, `4`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V): MFun<T, `6`, `4`> = MFun(`6`, `4`, t0, t1, t2, t3, t4, t5)
    @JvmName("m6x5") private operator fun <T: SFun<T>, V: VFun<T, `5`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V): MFun<T, `6`, `5`> = MFun(`6`, `5`, t0, t1, t2, t3, t4, t5)
    @JvmName("m6x6") private operator fun <T: SFun<T>, V: VFun<T, `6`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V): MFun<T, `6`, `6`> = MFun(`6`, `6`, t0, t1, t2, t3, t4, t5)
    @JvmName("m6x7") private operator fun <T: SFun<T>, V: VFun<T, `7`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V): MFun<T, `6`, `7`> = MFun(`6`, `7`, t0, t1, t2, t3, t4, t5)
    @JvmName("m6x8") private operator fun <T: SFun<T>, V: VFun<T, `8`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V): MFun<T, `6`, `8`> = MFun(`6`, `8`, t0, t1, t2, t3, t4, t5)
    @JvmName("m6x9") private operator fun <T: SFun<T>, V: VFun<T, `9`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V): MFun<T, `6`, `9`> = MFun(`6`, `9`, t0, t1, t2, t3, t4, t5)
    @JvmName("m7x1") private operator fun <T: SFun<T>, V: VFun<T, `1`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V): MFun<T, `7`, `1`> = MFun(`7`, `1`, t0, t1, t2, t3, t4, t5, t6)
    @JvmName("m7x2") private operator fun <T: SFun<T>, V: VFun<T, `2`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V): MFun<T, `7`, `2`> = MFun(`7`, `2`, t0, t1, t2, t3, t4, t5, t6)
    @JvmName("m7x3") private operator fun <T: SFun<T>, V: VFun<T, `3`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V): MFun<T, `7`, `3`> = MFun(`7`, `3`, t0, t1, t2, t3, t4, t5, t6)
    @JvmName("m7x4") private operator fun <T: SFun<T>, V: VFun<T, `4`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V): MFun<T, `7`, `4`> = MFun(`7`, `4`, t0, t1, t2, t3, t4, t5, t6)
    @JvmName("m7x5") private operator fun <T: SFun<T>, V: VFun<T, `5`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V): MFun<T, `7`, `5`> = MFun(`7`, `5`, t0, t1, t2, t3, t4, t5, t6)
    @JvmName("m7x6") private operator fun <T: SFun<T>, V: VFun<T, `6`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V): MFun<T, `7`, `6`> = MFun(`7`, `6`, t0, t1, t2, t3, t4, t5, t6)
    @JvmName("m7x7") private operator fun <T: SFun<T>, V: VFun<T, `7`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V): MFun<T, `7`, `7`> = MFun(`7`, `7`, t0, t1, t2, t3, t4, t5, t6)
    @JvmName("m7x8") private operator fun <T: SFun<T>, V: VFun<T, `8`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V): MFun<T, `7`, `8`> = MFun(`7`, `8`, t0, t1, t2, t3, t4, t5, t6)
    @JvmName("m7x9") private operator fun <T: SFun<T>, V: VFun<T, `9`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V): MFun<T, `7`, `9`> = MFun(`7`, `9`, t0, t1, t2, t3, t4, t5, t6)
    @JvmName("m8x1") private operator fun <T: SFun<T>, V: VFun<T, `1`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V, t7: V): MFun<T, `8`, `1`> = MFun(`8`, `1`, t0, t1, t2, t3, t4, t5, t6, t7)
    @JvmName("m8x2") private operator fun <T: SFun<T>, V: VFun<T, `2`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V, t7: V): MFun<T, `8`, `2`> = MFun(`8`, `2`, t0, t1, t2, t3, t4, t5, t6, t7)
    @JvmName("m8x3") private operator fun <T: SFun<T>, V: VFun<T, `3`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V, t7: V): MFun<T, `8`, `3`> = MFun(`8`, `3`, t0, t1, t2, t3, t4, t5, t6, t7)
    @JvmName("m8x4") private operator fun <T: SFun<T>, V: VFun<T, `4`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V, t7: V): MFun<T, `8`, `4`> = MFun(`8`, `4`, t0, t1, t2, t3, t4, t5, t6, t7)
    @JvmName("m8x5") private operator fun <T: SFun<T>, V: VFun<T, `5`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V, t7: V): MFun<T, `8`, `5`> = MFun(`8`, `5`, t0, t1, t2, t3, t4, t5, t6, t7)
    @JvmName("m8x6") private operator fun <T: SFun<T>, V: VFun<T, `6`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V, t7: V): MFun<T, `8`, `6`> = MFun(`8`, `6`, t0, t1, t2, t3, t4, t5, t6, t7)
    @JvmName("m8x7") private operator fun <T: SFun<T>, V: VFun<T, `7`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V, t7: V): MFun<T, `8`, `7`> = MFun(`8`, `7`, t0, t1, t2, t3, t4, t5, t6, t7)
    @JvmName("m8x8") private operator fun <T: SFun<T>, V: VFun<T, `8`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V, t7: V): MFun<T, `8`, `8`> = MFun(`8`, `8`, t0, t1, t2, t3, t4, t5, t6, t7)
    @JvmName("m8x9") private operator fun <T: SFun<T>, V: VFun<T, `9`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V, t7: V): MFun<T, `8`, `9`> = MFun(`8`, `9`, t0, t1, t2, t3, t4, t5, t6, t7)
    @JvmName("m9x1") private operator fun <T: SFun<T>, V: VFun<T, `1`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V, t7: V, t8: V): MFun<T, `9`, `1`> = MFun(`9`, `1`, t0, t1, t2, t3, t4, t5, t6, t7, t8)
    @JvmName("m9x2") private operator fun <T: SFun<T>, V: VFun<T, `2`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V, t7: V, t8: V): MFun<T, `9`, `2`> = MFun(`9`, `2`, t0, t1, t2, t3, t4, t5, t6, t7, t8)
    @JvmName("m9x3") private operator fun <T: SFun<T>, V: VFun<T, `3`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V, t7: V, t8: V): MFun<T, `9`, `3`> = MFun(`9`, `3`, t0, t1, t2, t3, t4, t5, t6, t7, t8)
    @JvmName("m9x4") private operator fun <T: SFun<T>, V: VFun<T, `4`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V, t7: V, t8: V): MFun<T, `9`, `4`> = MFun(`9`, `4`, t0, t1, t2, t3, t4, t5, t6, t7, t8)
    @JvmName("m9x5") private operator fun <T: SFun<T>, V: VFun<T, `5`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V, t7: V, t8: V): MFun<T, `9`, `5`> = MFun(`9`, `5`, t0, t1, t2, t3, t4, t5, t6, t7, t8)
    @JvmName("m9x6") private operator fun <T: SFun<T>, V: VFun<T, `6`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V, t7: V, t8: V): MFun<T, `9`, `6`> = MFun(`9`, `6`, t0, t1, t2, t3, t4, t5, t6, t7, t8)
    @JvmName("m9x7") private operator fun <T: SFun<T>, V: VFun<T, `7`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V, t7: V, t8: V): MFun<T, `9`, `7`> = MFun(`9`, `7`, t0, t1, t2, t3, t4, t5, t6, t7, t8)
    @JvmName("m9x8") private operator fun <T: SFun<T>, V: VFun<T, `8`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V, t7: V, t8: V): MFun<T, `9`, `8`> = MFun(`9`, `8`, t0, t1, t2, t3, t4, t5, t6, t7, t8)
    @JvmName("m9x9") private operator fun <T: SFun<T>, V: VFun<T, `9`>> invoke(t0: V, t1: V, t2: V, t3: V, t4: V, t5: V, t6: V, t7: V, t8: V): MFun<T, `9`, `9`> = MFun(`9`, `9`, t0, t1, t2, t3, t4, t5, t6, t7, t8)

    @JvmName("m1x2f") operator fun <T: SFun<T>> invoke(m: Nat<`1`>, n: Nat<`2`>, t0: T, t1: T) = MFun(VFun(t0, t1))
    @JvmName("m1x3f") operator fun <T: SFun<T>> invoke(m: Nat<`1`>, n: Nat<`3`>, t0: T, t1: T, t2: T) = MFun(VFun(t0, t1, t2))
    @JvmName("m1x4f") operator fun <T: SFun<T>> invoke(m: Nat<`1`>, n: Nat<`4`>, t0: T, t1: T, t2: T, t3: T) = MFun(VFun(t0, t1, t2, t3))
    @JvmName("m1x5f") operator fun <T: SFun<T>> invoke(m: Nat<`1`>, n: Nat<`5`>, t0: T, t1: T, t2: T, t3: T, t4: T) = MFun(VFun(t0, t1, t2, t3, t4))
    @JvmName("m1x6f") operator fun <T: SFun<T>> invoke(m: Nat<`1`>, n: Nat<`6`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T) = MFun(VFun(t0, t1, t2, t3, t4, t5))
    @JvmName("m1x7f") operator fun <T: SFun<T>> invoke(m: Nat<`1`>, n: Nat<`7`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6))
    @JvmName("m1x8f") operator fun <T: SFun<T>> invoke(m: Nat<`1`>, n: Nat<`8`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6, t7))
    @JvmName("m1x9f") operator fun <T: SFun<T>> invoke(m: Nat<`1`>, n: Nat<`9`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6, t7, t8))
    @JvmName("m2x1f") operator fun <T: SFun<T>> invoke(m: Nat<`2`>, n: Nat<`1`>, t0: T, t1: T) = MFun(VFun(t0), VFun(t1))
    @JvmName("m2x2f") operator fun <T: SFun<T>> invoke(m: Nat<`2`>, n: Nat<`2`>, t0: T, t1: T, t2: T, t3: T) = MFun(VFun(t0, t1), VFun(t2, t3))
    @JvmName("m2x3f") operator fun <T: SFun<T>> invoke(m: Nat<`2`>, n: Nat<`3`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T) = MFun(VFun(t0, t1, t2), VFun(t3, t4, t5))
    @JvmName("m2x4f") operator fun <T: SFun<T>> invoke(m: Nat<`2`>, n: Nat<`4`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T) = MFun(VFun(t0, t1, t2, t3), VFun(t4, t5, t6, t7))
    @JvmName("m2x5f") operator fun <T: SFun<T>> invoke(m: Nat<`2`>, n: Nat<`5`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T) = MFun(VFun(t0, t1, t2, t3, t4), VFun(t5, t6, t7, t8, t9))
    @JvmName("m2x6f") operator fun <T: SFun<T>> invoke(m: Nat<`2`>, n: Nat<`6`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T) = MFun(VFun(t0, t1, t2, t3, t4, t5), VFun(t6, t7, t8, t9, t10, t11))
    @JvmName("m2x7f") operator fun <T: SFun<T>> invoke(m: Nat<`2`>, n: Nat<`7`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6), VFun(t7, t8, t9, t10, t11, t12, t13))
    @JvmName("m2x8f") operator fun <T: SFun<T>> invoke(m: Nat<`2`>, n: Nat<`8`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6, t7), VFun(t8, t9, t10, t11, t12, t13, t14, t15))
    @JvmName("m2x9f") operator fun <T: SFun<T>> invoke(m: Nat<`2`>, n: Nat<`9`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6, t7, t8), VFun(t9, t10, t11, t12, t13, t14, t15, t16, t17))
    @JvmName("m3x1f") operator fun <T: SFun<T>> invoke(m: Nat<`3`>, n: Nat<`1`>, t0: T, t1: T, t2: T) = MFun(VFun(t0), VFun(t1), VFun(t2))
    @JvmName("m3x2f") operator fun <T: SFun<T>> invoke(m: Nat<`3`>, n: Nat<`2`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T) = MFun(VFun(t0, t1), VFun(t2, t3), VFun(t4, t5))
    @JvmName("m3x3f") operator fun <T: SFun<T>> invoke(m: Nat<`3`>, n: Nat<`3`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T) = MFun(VFun(t0, t1, t2), VFun(t3, t4, t5), VFun(t6, t7, t8))
    @JvmName("m3x4f") operator fun <T: SFun<T>> invoke(m: Nat<`3`>, n: Nat<`4`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T) = MFun(VFun(t0, t1, t2, t3), VFun(t4, t5, t6, t7), VFun(t8, t9, t10, t11))
    @JvmName("m3x5f") operator fun <T: SFun<T>> invoke(m: Nat<`3`>, n: Nat<`5`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T) = MFun(VFun(t0, t1, t2, t3, t4), VFun(t5, t6, t7, t8, t9), VFun(t10, t11, t12, t13, t14))
    @JvmName("m3x6f") operator fun <T: SFun<T>> invoke(m: Nat<`3`>, n: Nat<`6`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T) = MFun(VFun(t0, t1, t2, t3, t4, t5), VFun(t6, t7, t8, t9, t10, t11), VFun(t12, t13, t14, t15, t16, t17))
    @JvmName("m3x7f") operator fun <T: SFun<T>> invoke(m: Nat<`3`>, n: Nat<`7`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6), VFun(t7, t8, t9, t10, t11, t12, t13), VFun(t14, t15, t16, t17, t18, t19, t20))
    @JvmName("m3x8f") operator fun <T: SFun<T>> invoke(m: Nat<`3`>, n: Nat<`8`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6, t7), VFun(t8, t9, t10, t11, t12, t13, t14, t15), VFun(t16, t17, t18, t19, t20, t21, t22, t23))
    @JvmName("m3x9f") operator fun <T: SFun<T>> invoke(m: Nat<`3`>, n: Nat<`9`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6, t7, t8), VFun(t9, t10, t11, t12, t13, t14, t15, t16, t17), VFun(t18, t19, t20, t21, t22, t23, t24, t25, t26))
    @JvmName("m4x1f") operator fun <T: SFun<T>> invoke(m: Nat<`4`>, n: Nat<`1`>, t0: T, t1: T, t2: T, t3: T) = MFun(VFun(t0), VFun(t1), VFun(t2), VFun(t3))
    @JvmName("m4x2f") operator fun <T: SFun<T>> invoke(m: Nat<`4`>, n: Nat<`2`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T) = MFun(VFun(t0, t1), VFun(t2, t3), VFun(t4, t5), VFun(t6, t7))
    @JvmName("m4x3f") operator fun <T: SFun<T>> invoke(m: Nat<`4`>, n: Nat<`3`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T) = MFun(VFun(t0, t1, t2), VFun(t3, t4, t5), VFun(t6, t7, t8), VFun(t9, t10, t11))
    @JvmName("m4x4f") operator fun <T: SFun<T>> invoke(m: Nat<`4`>, n: Nat<`4`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T) = MFun(VFun(t0, t1, t2, t3), VFun(t4, t5, t6, t7), VFun(t8, t9, t10, t11), VFun(t12, t13, t14, t15))
    @JvmName("m4x5f") operator fun <T: SFun<T>> invoke(m: Nat<`4`>, n: Nat<`5`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T) = MFun(VFun(t0, t1, t2, t3, t4), VFun(t5, t6, t7, t8, t9), VFun(t10, t11, t12, t13, t14), VFun(t15, t16, t17, t18, t19))
    @JvmName("m4x6f") operator fun <T: SFun<T>> invoke(m: Nat<`4`>, n: Nat<`6`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T) = MFun(VFun(t0, t1, t2, t3, t4, t5), VFun(t6, t7, t8, t9, t10, t11), VFun(t12, t13, t14, t15, t16, t17), VFun(t18, t19, t20, t21, t22, t23))
    @JvmName("m4x7f") operator fun <T: SFun<T>> invoke(m: Nat<`4`>, n: Nat<`7`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6), VFun(t7, t8, t9, t10, t11, t12, t13), VFun(t14, t15, t16, t17, t18, t19, t20), VFun(t21, t22, t23, t24, t25, t26, t27))
    @JvmName("m4x8f") operator fun <T: SFun<T>> invoke(m: Nat<`4`>, n: Nat<`8`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6, t7), VFun(t8, t9, t10, t11, t12, t13, t14, t15), VFun(t16, t17, t18, t19, t20, t21, t22, t23), VFun(t24, t25, t26, t27, t28, t29, t30, t31))
    @JvmName("m4x9f") operator fun <T: SFun<T>> invoke(m: Nat<`4`>, n: Nat<`9`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6, t7, t8), VFun(t9, t10, t11, t12, t13, t14, t15, t16, t17), VFun(t18, t19, t20, t21, t22, t23, t24, t25, t26), VFun(t27, t28, t29, t30, t31, t32, t33, t34, t35))
    @JvmName("m5x1f") operator fun <T: SFun<T>> invoke(m: Nat<`5`>, n: Nat<`1`>, t0: T, t1: T, t2: T, t3: T, t4: T) = MFun(VFun(t0), VFun(t1), VFun(t2), VFun(t3), VFun(t4))
    @JvmName("m5x2f") operator fun <T: SFun<T>> invoke(m: Nat<`5`>, n: Nat<`2`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T) = MFun(VFun(t0, t1), VFun(t2, t3), VFun(t4, t5), VFun(t6, t7), VFun(t8, t9))
    @JvmName("m5x3f") operator fun <T: SFun<T>> invoke(m: Nat<`5`>, n: Nat<`3`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T) = MFun(VFun(t0, t1, t2), VFun(t3, t4, t5), VFun(t6, t7, t8), VFun(t9, t10, t11), VFun(t12, t13, t14))
    @JvmName("m5x4f") operator fun <T: SFun<T>> invoke(m: Nat<`5`>, n: Nat<`4`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T) = MFun(VFun(t0, t1, t2, t3), VFun(t4, t5, t6, t7), VFun(t8, t9, t10, t11), VFun(t12, t13, t14, t15), VFun(t16, t17, t18, t19))
    @JvmName("m5x5f") operator fun <T: SFun<T>> invoke(m: Nat<`5`>, n: Nat<`5`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T) = MFun(VFun(t0, t1, t2, t3, t4), VFun(t5, t6, t7, t8, t9), VFun(t10, t11, t12, t13, t14), VFun(t15, t16, t17, t18, t19), VFun(t20, t21, t22, t23, t24))
    @JvmName("m5x6f") operator fun <T: SFun<T>> invoke(m: Nat<`5`>, n: Nat<`6`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T) = MFun(VFun(t0, t1, t2, t3, t4, t5), VFun(t6, t7, t8, t9, t10, t11), VFun(t12, t13, t14, t15, t16, t17), VFun(t18, t19, t20, t21, t22, t23), VFun(t24, t25, t26, t27, t28, t29))
    @JvmName("m5x7f") operator fun <T: SFun<T>> invoke(m: Nat<`5`>, n: Nat<`7`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6), VFun(t7, t8, t9, t10, t11, t12, t13), VFun(t14, t15, t16, t17, t18, t19, t20), VFun(t21, t22, t23, t24, t25, t26, t27), VFun(t28, t29, t30, t31, t32, t33, t34))
    @JvmName("m5x8f") operator fun <T: SFun<T>> invoke(m: Nat<`5`>, n: Nat<`8`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6, t7), VFun(t8, t9, t10, t11, t12, t13, t14, t15), VFun(t16, t17, t18, t19, t20, t21, t22, t23), VFun(t24, t25, t26, t27, t28, t29, t30, t31), VFun(t32, t33, t34, t35, t36, t37, t38, t39))
    @JvmName("m5x9f") operator fun <T: SFun<T>> invoke(m: Nat<`5`>, n: Nat<`9`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T, t40: T, t41: T, t42: T, t43: T, t44: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6, t7, t8), VFun(t9, t10, t11, t12, t13, t14, t15, t16, t17), VFun(t18, t19, t20, t21, t22, t23, t24, t25, t26), VFun(t27, t28, t29, t30, t31, t32, t33, t34, t35), VFun(t36, t37, t38, t39, t40, t41, t42, t43, t44))
    @JvmName("m6x1f") operator fun <T: SFun<T>> invoke(m: Nat<`6`>, n: Nat<`1`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T) = MFun(VFun(t0), VFun(t1), VFun(t2), VFun(t3), VFun(t4), VFun(t5))
    @JvmName("m6x2f") operator fun <T: SFun<T>> invoke(m: Nat<`6`>, n: Nat<`2`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T) = MFun(VFun(t0, t1), VFun(t2, t3), VFun(t4, t5), VFun(t6, t7), VFun(t8, t9), VFun(t10, t11))
    @JvmName("m6x3f") operator fun <T: SFun<T>> invoke(m: Nat<`6`>, n: Nat<`3`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T) = MFun(VFun(t0, t1, t2), VFun(t3, t4, t5), VFun(t6, t7, t8), VFun(t9, t10, t11), VFun(t12, t13, t14), VFun(t15, t16, t17))
    @JvmName("m6x4f") operator fun <T: SFun<T>> invoke(m: Nat<`6`>, n: Nat<`4`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T) = MFun(VFun(t0, t1, t2, t3), VFun(t4, t5, t6, t7), VFun(t8, t9, t10, t11), VFun(t12, t13, t14, t15), VFun(t16, t17, t18, t19), VFun(t20, t21, t22, t23))
    @JvmName("m6x5f") operator fun <T: SFun<T>> invoke(m: Nat<`6`>, n: Nat<`5`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T) = MFun(VFun(t0, t1, t2, t3, t4), VFun(t5, t6, t7, t8, t9), VFun(t10, t11, t12, t13, t14), VFun(t15, t16, t17, t18, t19), VFun(t20, t21, t22, t23, t24), VFun(t25, t26, t27, t28, t29))
    @JvmName("m6x6f") operator fun <T: SFun<T>> invoke(m: Nat<`6`>, n: Nat<`6`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T) = MFun(VFun(t0, t1, t2, t3, t4, t5), VFun(t6, t7, t8, t9, t10, t11), VFun(t12, t13, t14, t15, t16, t17), VFun(t18, t19, t20, t21, t22, t23), VFun(t24, t25, t26, t27, t28, t29), VFun(t30, t31, t32, t33, t34, t35))
    @JvmName("m6x7f") operator fun <T: SFun<T>> invoke(m: Nat<`6`>, n: Nat<`7`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T, t40: T, t41: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6), VFun(t7, t8, t9, t10, t11, t12, t13), VFun(t14, t15, t16, t17, t18, t19, t20), VFun(t21, t22, t23, t24, t25, t26, t27), VFun(t28, t29, t30, t31, t32, t33, t34), VFun(t35, t36, t37, t38, t39, t40, t41))
    @JvmName("m6x8f") operator fun <T: SFun<T>> invoke(m: Nat<`6`>, n: Nat<`8`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T, t40: T, t41: T, t42: T, t43: T, t44: T, t45: T, t46: T, t47: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6, t7), VFun(t8, t9, t10, t11, t12, t13, t14, t15), VFun(t16, t17, t18, t19, t20, t21, t22, t23), VFun(t24, t25, t26, t27, t28, t29, t30, t31), VFun(t32, t33, t34, t35, t36, t37, t38, t39), VFun(t40, t41, t42, t43, t44, t45, t46, t47))
    @JvmName("m6x9f") operator fun <T: SFun<T>> invoke(m: Nat<`6`>, n: Nat<`9`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T, t40: T, t41: T, t42: T, t43: T, t44: T, t45: T, t46: T, t47: T, t48: T, t49: T, t50: T, t51: T, t52: T, t53: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6, t7, t8), VFun(t9, t10, t11, t12, t13, t14, t15, t16, t17), VFun(t18, t19, t20, t21, t22, t23, t24, t25, t26), VFun(t27, t28, t29, t30, t31, t32, t33, t34, t35), VFun(t36, t37, t38, t39, t40, t41, t42, t43, t44), VFun(t45, t46, t47, t48, t49, t50, t51, t52, t53))
    @JvmName("m7x1f") operator fun <T: SFun<T>> invoke(m: Nat<`7`>, n: Nat<`1`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T) = MFun(VFun(t0), VFun(t1), VFun(t2), VFun(t3), VFun(t4), VFun(t5), VFun(t6))
    @JvmName("m7x2f") operator fun <T: SFun<T>> invoke(m: Nat<`7`>, n: Nat<`2`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T) = MFun(VFun(t0, t1), VFun(t2, t3), VFun(t4, t5), VFun(t6, t7), VFun(t8, t9), VFun(t10, t11), VFun(t12, t13))
    @JvmName("m7x3f") operator fun <T: SFun<T>> invoke(m: Nat<`7`>, n: Nat<`3`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T) = MFun(VFun(t0, t1, t2), VFun(t3, t4, t5), VFun(t6, t7, t8), VFun(t9, t10, t11), VFun(t12, t13, t14), VFun(t15, t16, t17), VFun(t18, t19, t20))
    @JvmName("m7x4f") operator fun <T: SFun<T>> invoke(m: Nat<`7`>, n: Nat<`4`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T) = MFun(VFun(t0, t1, t2, t3), VFun(t4, t5, t6, t7), VFun(t8, t9, t10, t11), VFun(t12, t13, t14, t15), VFun(t16, t17, t18, t19), VFun(t20, t21, t22, t23), VFun(t24, t25, t26, t27))
    @JvmName("m7x5f") operator fun <T: SFun<T>> invoke(m: Nat<`7`>, n: Nat<`5`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T) = MFun(VFun(t0, t1, t2, t3, t4), VFun(t5, t6, t7, t8, t9), VFun(t10, t11, t12, t13, t14), VFun(t15, t16, t17, t18, t19), VFun(t20, t21, t22, t23, t24), VFun(t25, t26, t27, t28, t29), VFun(t30, t31, t32, t33, t34))
    @JvmName("m7x6f") operator fun <T: SFun<T>> invoke(m: Nat<`7`>, n: Nat<`6`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T, t40: T, t41: T) = MFun(VFun(t0, t1, t2, t3, t4, t5), VFun(t6, t7, t8, t9, t10, t11), VFun(t12, t13, t14, t15, t16, t17), VFun(t18, t19, t20, t21, t22, t23), VFun(t24, t25, t26, t27, t28, t29), VFun(t30, t31, t32, t33, t34, t35), VFun(t36, t37, t38, t39, t40, t41))
    @JvmName("m7x7f") operator fun <T: SFun<T>> invoke(m: Nat<`7`>, n: Nat<`7`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T, t40: T, t41: T, t42: T, t43: T, t44: T, t45: T, t46: T, t47: T, t48: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6), VFun(t7, t8, t9, t10, t11, t12, t13), VFun(t14, t15, t16, t17, t18, t19, t20), VFun(t21, t22, t23, t24, t25, t26, t27), VFun(t28, t29, t30, t31, t32, t33, t34), VFun(t35, t36, t37, t38, t39, t40, t41), VFun(t42, t43, t44, t45, t46, t47, t48))
    @JvmName("m7x8f") operator fun <T: SFun<T>> invoke(m: Nat<`7`>, n: Nat<`8`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T, t40: T, t41: T, t42: T, t43: T, t44: T, t45: T, t46: T, t47: T, t48: T, t49: T, t50: T, t51: T, t52: T, t53: T, t54: T, t55: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6, t7), VFun(t8, t9, t10, t11, t12, t13, t14, t15), VFun(t16, t17, t18, t19, t20, t21, t22, t23), VFun(t24, t25, t26, t27, t28, t29, t30, t31), VFun(t32, t33, t34, t35, t36, t37, t38, t39), VFun(t40, t41, t42, t43, t44, t45, t46, t47), VFun(t48, t49, t50, t51, t52, t53, t54, t55))
    @JvmName("m7x9f") operator fun <T: SFun<T>> invoke(m: Nat<`7`>, n: Nat<`9`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T, t40: T, t41: T, t42: T, t43: T, t44: T, t45: T, t46: T, t47: T, t48: T, t49: T, t50: T, t51: T, t52: T, t53: T, t54: T, t55: T, t56: T, t57: T, t58: T, t59: T, t60: T, t61: T, t62: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6, t7, t8), VFun(t9, t10, t11, t12, t13, t14, t15, t16, t17), VFun(t18, t19, t20, t21, t22, t23, t24, t25, t26), VFun(t27, t28, t29, t30, t31, t32, t33, t34, t35), VFun(t36, t37, t38, t39, t40, t41, t42, t43, t44), VFun(t45, t46, t47, t48, t49, t50, t51, t52, t53), VFun(t54, t55, t56, t57, t58, t59, t60, t61, t62))
    @JvmName("m8x1f") operator fun <T: SFun<T>> invoke(m: Nat<`8`>, n: Nat<`1`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T) = MFun(VFun(t0), VFun(t1), VFun(t2), VFun(t3), VFun(t4), VFun(t5), VFun(t6), VFun(t7))
    @JvmName("m8x2f") operator fun <T: SFun<T>> invoke(m: Nat<`8`>, n: Nat<`2`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T) = MFun(VFun(t0, t1), VFun(t2, t3), VFun(t4, t5), VFun(t6, t7), VFun(t8, t9), VFun(t10, t11), VFun(t12, t13), VFun(t14, t15))
    @JvmName("m8x3f") operator fun <T: SFun<T>> invoke(m: Nat<`8`>, n: Nat<`3`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T) = MFun(VFun(t0, t1, t2), VFun(t3, t4, t5), VFun(t6, t7, t8), VFun(t9, t10, t11), VFun(t12, t13, t14), VFun(t15, t16, t17), VFun(t18, t19, t20), VFun(t21, t22, t23))
    @JvmName("m8x4f") operator fun <T: SFun<T>> invoke(m: Nat<`8`>, n: Nat<`4`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T) = MFun(VFun(t0, t1, t2, t3), VFun(t4, t5, t6, t7), VFun(t8, t9, t10, t11), VFun(t12, t13, t14, t15), VFun(t16, t17, t18, t19), VFun(t20, t21, t22, t23), VFun(t24, t25, t26, t27), VFun(t28, t29, t30, t31))
    @JvmName("m8x5f") operator fun <T: SFun<T>> invoke(m: Nat<`8`>, n: Nat<`5`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T) = MFun(VFun(t0, t1, t2, t3, t4), VFun(t5, t6, t7, t8, t9), VFun(t10, t11, t12, t13, t14), VFun(t15, t16, t17, t18, t19), VFun(t20, t21, t22, t23, t24), VFun(t25, t26, t27, t28, t29), VFun(t30, t31, t32, t33, t34), VFun(t35, t36, t37, t38, t39))
    @JvmName("m8x6f") operator fun <T: SFun<T>> invoke(m: Nat<`8`>, n: Nat<`6`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T, t40: T, t41: T, t42: T, t43: T, t44: T, t45: T, t46: T, t47: T) = MFun(VFun(t0, t1, t2, t3, t4, t5), VFun(t6, t7, t8, t9, t10, t11), VFun(t12, t13, t14, t15, t16, t17), VFun(t18, t19, t20, t21, t22, t23), VFun(t24, t25, t26, t27, t28, t29), VFun(t30, t31, t32, t33, t34, t35), VFun(t36, t37, t38, t39, t40, t41), VFun(t42, t43, t44, t45, t46, t47))
    @JvmName("m8x7f") operator fun <T: SFun<T>> invoke(m: Nat<`8`>, n: Nat<`7`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T, t40: T, t41: T, t42: T, t43: T, t44: T, t45: T, t46: T, t47: T, t48: T, t49: T, t50: T, t51: T, t52: T, t53: T, t54: T, t55: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6), VFun(t7, t8, t9, t10, t11, t12, t13), VFun(t14, t15, t16, t17, t18, t19, t20), VFun(t21, t22, t23, t24, t25, t26, t27), VFun(t28, t29, t30, t31, t32, t33, t34), VFun(t35, t36, t37, t38, t39, t40, t41), VFun(t42, t43, t44, t45, t46, t47, t48), VFun(t49, t50, t51, t52, t53, t54, t55))
    @JvmName("m8x8f") operator fun <T: SFun<T>> invoke(m: Nat<`8`>, n: Nat<`8`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T, t40: T, t41: T, t42: T, t43: T, t44: T, t45: T, t46: T, t47: T, t48: T, t49: T, t50: T, t51: T, t52: T, t53: T, t54: T, t55: T, t56: T, t57: T, t58: T, t59: T, t60: T, t61: T, t62: T, t63: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6, t7), VFun(t8, t9, t10, t11, t12, t13, t14, t15), VFun(t16, t17, t18, t19, t20, t21, t22, t23), VFun(t24, t25, t26, t27, t28, t29, t30, t31), VFun(t32, t33, t34, t35, t36, t37, t38, t39), VFun(t40, t41, t42, t43, t44, t45, t46, t47), VFun(t48, t49, t50, t51, t52, t53, t54, t55), VFun(t56, t57, t58, t59, t60, t61, t62, t63))
    @JvmName("m8x9f") operator fun <T: SFun<T>> invoke(m: Nat<`8`>, n: Nat<`9`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T, t40: T, t41: T, t42: T, t43: T, t44: T, t45: T, t46: T, t47: T, t48: T, t49: T, t50: T, t51: T, t52: T, t53: T, t54: T, t55: T, t56: T, t57: T, t58: T, t59: T, t60: T, t61: T, t62: T, t63: T, t64: T, t65: T, t66: T, t67: T, t68: T, t69: T, t70: T, t71: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6, t7, t8), VFun(t9, t10, t11, t12, t13, t14, t15, t16, t17), VFun(t18, t19, t20, t21, t22, t23, t24, t25, t26), VFun(t27, t28, t29, t30, t31, t32, t33, t34, t35), VFun(t36, t37, t38, t39, t40, t41, t42, t43, t44), VFun(t45, t46, t47, t48, t49, t50, t51, t52, t53), VFun(t54, t55, t56, t57, t58, t59, t60, t61, t62), VFun(t63, t64, t65, t66, t67, t68, t69, t70, t71))
    @JvmName("m9x1f") operator fun <T: SFun<T>> invoke(m: Nat<`9`>, n: Nat<`1`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T) = MFun(VFun(t0), VFun(t1), VFun(t2), VFun(t3), VFun(t4), VFun(t5), VFun(t6), VFun(t7), VFun(t8))
    @JvmName("m9x2f") operator fun <T: SFun<T>> invoke(m: Nat<`9`>, n: Nat<`2`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T) = MFun(VFun(t0, t1), VFun(t2, t3), VFun(t4, t5), VFun(t6, t7), VFun(t8, t9), VFun(t10, t11), VFun(t12, t13), VFun(t14, t15), VFun(t16, t17))
    @JvmName("m9x3f") operator fun <T: SFun<T>> invoke(m: Nat<`9`>, n: Nat<`3`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T) = MFun(VFun(t0, t1, t2), VFun(t3, t4, t5), VFun(t6, t7, t8), VFun(t9, t10, t11), VFun(t12, t13, t14), VFun(t15, t16, t17), VFun(t18, t19, t20), VFun(t21, t22, t23), VFun(t24, t25, t26))
    @JvmName("m9x4f") operator fun <T: SFun<T>> invoke(m: Nat<`9`>, n: Nat<`4`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T) = MFun(VFun(t0, t1, t2, t3), VFun(t4, t5, t6, t7), VFun(t8, t9, t10, t11), VFun(t12, t13, t14, t15), VFun(t16, t17, t18, t19), VFun(t20, t21, t22, t23), VFun(t24, t25, t26, t27), VFun(t28, t29, t30, t31), VFun(t32, t33, t34, t35))
    @JvmName("m9x5f") operator fun <T: SFun<T>> invoke(m: Nat<`9`>, n: Nat<`5`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T, t40: T, t41: T, t42: T, t43: T, t44: T) = MFun(VFun(t0, t1, t2, t3, t4), VFun(t5, t6, t7, t8, t9), VFun(t10, t11, t12, t13, t14), VFun(t15, t16, t17, t18, t19), VFun(t20, t21, t22, t23, t24), VFun(t25, t26, t27, t28, t29), VFun(t30, t31, t32, t33, t34), VFun(t35, t36, t37, t38, t39), VFun(t40, t41, t42, t43, t44))
    @JvmName("m9x6f") operator fun <T: SFun<T>> invoke(m: Nat<`9`>, n: Nat<`6`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T, t40: T, t41: T, t42: T, t43: T, t44: T, t45: T, t46: T, t47: T, t48: T, t49: T, t50: T, t51: T, t52: T, t53: T) = MFun(VFun(t0, t1, t2, t3, t4, t5), VFun(t6, t7, t8, t9, t10, t11), VFun(t12, t13, t14, t15, t16, t17), VFun(t18, t19, t20, t21, t22, t23), VFun(t24, t25, t26, t27, t28, t29), VFun(t30, t31, t32, t33, t34, t35), VFun(t36, t37, t38, t39, t40, t41), VFun(t42, t43, t44, t45, t46, t47), VFun(t48, t49, t50, t51, t52, t53))
    @JvmName("m9x7f") operator fun <T: SFun<T>> invoke(m: Nat<`9`>, n: Nat<`7`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T, t40: T, t41: T, t42: T, t43: T, t44: T, t45: T, t46: T, t47: T, t48: T, t49: T, t50: T, t51: T, t52: T, t53: T, t54: T, t55: T, t56: T, t57: T, t58: T, t59: T, t60: T, t61: T, t62: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6), VFun(t7, t8, t9, t10, t11, t12, t13), VFun(t14, t15, t16, t17, t18, t19, t20), VFun(t21, t22, t23, t24, t25, t26, t27), VFun(t28, t29, t30, t31, t32, t33, t34), VFun(t35, t36, t37, t38, t39, t40, t41), VFun(t42, t43, t44, t45, t46, t47, t48), VFun(t49, t50, t51, t52, t53, t54, t55), VFun(t56, t57, t58, t59, t60, t61, t62))
    @JvmName("m9x8f") operator fun <T: SFun<T>> invoke(m: Nat<`9`>, n: Nat<`8`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T, t40: T, t41: T, t42: T, t43: T, t44: T, t45: T, t46: T, t47: T, t48: T, t49: T, t50: T, t51: T, t52: T, t53: T, t54: T, t55: T, t56: T, t57: T, t58: T, t59: T, t60: T, t61: T, t62: T, t63: T, t64: T, t65: T, t66: T, t67: T, t68: T, t69: T, t70: T, t71: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6, t7), VFun(t8, t9, t10, t11, t12, t13, t14, t15), VFun(t16, t17, t18, t19, t20, t21, t22, t23), VFun(t24, t25, t26, t27, t28, t29, t30, t31), VFun(t32, t33, t34, t35, t36, t37, t38, t39), VFun(t40, t41, t42, t43, t44, t45, t46, t47), VFun(t48, t49, t50, t51, t52, t53, t54, t55), VFun(t56, t57, t58, t59, t60, t61, t62, t63), VFun(t64, t65, t66, t67, t68, t69, t70, t71))
    @JvmName("m9x9f") operator fun <T: SFun<T>> invoke(m: Nat<`9`>, n: Nat<`9`>, t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T, t9: T, t10: T, t11: T, t12: T, t13: T, t14: T, t15: T, t16: T, t17: T, t18: T, t19: T, t20: T, t21: T, t22: T, t23: T, t24: T, t25: T, t26: T, t27: T, t28: T, t29: T, t30: T, t31: T, t32: T, t33: T, t34: T, t35: T, t36: T, t37: T, t38: T, t39: T, t40: T, t41: T, t42: T, t43: T, t44: T, t45: T, t46: T, t47: T, t48: T, t49: T, t50: T, t51: T, t52: T, t53: T, t54: T, t55: T, t56: T, t57: T, t58: T, t59: T, t60: T, t61: T, t62: T, t63: T, t64: T, t65: T, t66: T, t67: T, t68: T, t69: T, t70: T, t71: T, t72: T, t73: T, t74: T, t75: T, t76: T, t77: T, t78: T, t79: T, t80: T) = MFun(VFun(t0, t1, t2, t3, t4, t5, t6, t7, t8), VFun(t9, t10, t11, t12, t13, t14, t15, t16, t17), VFun(t18, t19, t20, t21, t22, t23, t24, t25, t26), VFun(t27, t28, t29, t30, t31, t32, t33, t34, t35), VFun(t36, t37, t38, t39, t40, t41, t42, t43, t44), VFun(t45, t46, t47, t48, t49, t50, t51, t52, t53), VFun(t54, t55, t56, t57, t58, t59, t60, t61, t62), VFun(t63, t64, t65, t66, t67, t68, t69, t70, t71), VFun(t72, t73, t74, t75, t76, t77, t78, t79, t80))
  }

  val transpose by lazy { MFun(numCols, numRows, sVars, vVars, mVars, *cols) }
  val cols: Array<VFun<X, R>> by lazy { (0 until numCols.i).map { i -> VFun(numRows, rows.map { it[i] }) }.toTypedArray() }

  operator fun invoke(smap: Map<SVar<X>, Const<X>> = emptyMap(),
                      vMap: Map<VVar<X, *>, VConst<X, *>> = emptyMap(),
                      mMap: Map<MVar<X, *, *>, MConst<X, *, *>> = emptyMap()): MFun<X, R, C> = when (this) {
    else -> TODO()
  }

  operator fun unaryMinus() = MFun(numRows, numCols, rows.map { -it })
  operator fun plus(addend: MFun<X, R, C>): MFun<X, R, C> =
    MFun(numRows, numCols, rows.mapIndexed { i, r -> r + addend[i] })

  operator fun times(multiplicand: SFun<X>): MFun<X, R, C> =
    MFun(numRows, numCols, rows.map { it * multiplicand })

  operator fun times(multiplicand: VFun<X, C>): VFun<X, R> =
    (this * multiplicand.expand.transpose).cols.first()

  operator fun <Q: `1`> times(multiplicand: MFun<X, C, Q>): MFun<X, R, Q> =
    MFun(numRows, multiplicand.numCols, (0 until numRows.i).map { i ->
      VFun(multiplicand.numCols, (0 until multiplicand.numCols.i).map { j ->
        rows[i] dot multiplicand.cols[j]
      })
    })

  override fun toString() = "($numRows x $numCols)\n[${rows.joinToString("\n ") { it.contents.joinToString(", ") }}]"
}

class MNegative<X: SFun<X>, R: `1`, C: `1`>(val value: MFun<X, R, C>): MFun<X, R, C>(value)

class MMSum<X: SFun<X>, R: `1`, C: `1`>(val left: MFun<X, R, C>, val right: MFun<X, R, C>): MFun<X, R, C>(left, right)
class MMProd<X: SFun<X>, R: `1`, C1: `1`, C2: `1`>(val left: MFun<X, R, C1>, val right: MFun<X, C1, C2>): MFun<X, R, C2>(left, right)

class MVProd<X: SFun<X>, R: `1`, C: `1`>(val left: MFun<X, R, C>, val right: VFun<X, C>): VFun<X, C>(right.length, right)
class VMProd<X: SFun<X>, R: `1`, C: `1`>(val left: VFun<X, C>, val right: MFun<X, R, C>): VFun<X, C>(left.length, left)

class MSProd<X: SFun<X>, R: `1`, C: `1`>(val left: MFun<X, R, C>, val right: SFun<X>): MFun<X, R, C>(left)
class SMProd<X: SFun<X>, R: `1`, C: `1`>(val left: SFun<X>, val right: MFun<X, R, C>): MFun<X, R, C>(right)

class MVar<X: SFun<X>, R: `1`, C: `1`>(override val name: String, numRows: Nat<R>, numCols: Nat<C>, vararg val value: VVar<X, C>):
  Variable, MFun<X, R, C>(numRows, numCols) { override val mVars: Set<MVar<X, *, *>> = setOf(this) }
open class MConst<X: SFun<X>, R: `1`, C: `1`>(numRows: Nat<R>, numCols: Nat<C>, override vararg val rows: VConst<X, C>): MFun<X, R, C>(numRows, numCols, *rows)

/**
 * Numerical context.
 */

sealed class Protocol<X: RealNumber<X>> {
  abstract fun wrap(default: Number): X

  operator fun Number.times(multiplicand: SFun<X>) = multiplicand * wrap(this)
  operator fun SFun<X>.times(multiplicand: Number) = wrap(multiplicand) * this

  operator fun Number.plus(addend: SFun<X>) = addend * wrap(this)
  operator fun SFun<X>.plus(addend: Number) = wrap(addend) * this

  fun Number.pow(exp: SFun<X>) = wrap(this) pow exp
  infix fun SFun<X>.pow(exp: Number) = this pow wrap(exp)
}

object DoublePrecision: Protocol<DoubleReal>() {
  override fun wrap(default: Number): DoubleReal = DoubleReal(default.toDouble())
}

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