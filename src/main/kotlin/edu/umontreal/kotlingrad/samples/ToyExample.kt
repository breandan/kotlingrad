package edu.umontreal.kotlingrad.samples

import kotlin.math.E
import kotlin.math.ln
import kotlin.math.pow

fun main() {
  with(DoublePrecision) {
    val x = Var("x", DoubleReal(0.0))
    val y = Var("y", DoubleReal(0.0))

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

    val vf1 = VFun(`2`, y + x, y * 2)
    val vf2 = VFun(`2`, x, y)
    val q = vf1 + vf2
    val z = q(mapOf(x to DoubleReal(1.0), y to DoubleReal(2.0)))
    println("z: $z")

    val mf1 = MFun(`2`, `1`, VFun(`1`, y * y), VFun(`1`, x * y))
    val mf2 = MFun(`1`, `2`, vf2)
    val mf3 = MFun(`3`, `2`, VFun(`2`, x, x), VFun(`2`, y, x), VFun(`2`, x, x))
    println(mf1 * mf2) // 2*1 x 1*2
//    println(mf1 * vf1) // 2*1 x 2
    println(mf2 * vf1) // 1*2 x 2
    println(mf3 * vf1) // 3*2 x 2
//    println(mf3 * mf3)   // 3*2 x 3*2
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

sealed class SFun<X: SFun<X>>(open val variables: Set<Var<X>> = emptySet()): Field<SFun<X>> {
  constructor(fn: SFun<X>): this(fn.variables)
  constructor(vararg fns: SFun<X>): this(fns.flatMap { it.variables }.toSet())

  override operator fun plus(addend: SFun<X>): SFun<X> = Sum(this, addend)
  override operator fun times(multiplicand: SFun<X>): SFun<X> = Prod(this, multiplicand)

  operator fun invoke(map: Map<Var<X>, X>): SFun<X> = when (this) {
    is Const -> this
    is Var -> map.getOrElse(this) { this }
    is Prod -> left(map) * right(map)
    is Sum -> left(map) + right(map)
    is Power -> base(map) pow exponent(map)
    is Negative -> -value(map)
    is Log -> logarithmand(map).ln()
  }

  open fun diff(variable: Var<X>): SFun<X> = when (this) {
    is Var -> if (variable == this) one else zero
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
  private val proto: X by lazy { variables.first().value }

  override fun toString(): String = when {
    this is Log -> "ln($logarithmand)"
    this is Negative -> "-$value"
    this is Power -> "$base^($exponent)"
    this is Prod && right is Sum -> "$left⋅($right)"
    this is Prod && left is Sum -> "($left)⋅$right"
    this is Prod -> "$left⋅$right"
    this is Sum && right is Negative -> "$left - ${right.value}"
    this is Sum -> "$left + $right"
    this is Var -> name
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
interface Variable
class Var<X: SFun<X>>(val name: String, val value: X): Variable, SFun<X>() { override val variables: Set<Var<X>> = setOf(this) }

open class Const<X: SFun<X>>: SFun<X>()
abstract class RealNumber<X: SFun<X>>(open val value: Number): Const<X>()

/**
 * Constant propogation.
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
  val length: Nat<E>,
  val sVars: Set<Var<X>> = emptySet(),
  open val vVars: Set<VVar<X, *>> = emptySet(),
  open vararg val contents: SFun<X>
): List<SFun<X>> by contents.toList() {
  constructor(length: Nat<E>, contents: List<SFun<X>>): this(length, contents.flatMap { it.variables }.toSet(), emptySet(), *contents.toTypedArray())
  constructor(length: Nat<E>, vararg contents: SFun<X>): this(length, contents.flatMap { it.variables }.toSet(), emptySet(), *contents)
  constructor(vararg fns: VFun<X, E>): this(fns.first().length)

  init {
    if (length.i != contents.size && contents.isNotEmpty()) throw IllegalArgumentException("Declared length, $length != ${contents.size}")
  }

  val expand: MFun<X, `1`, E> by lazy { MFun(`1`, length, this) }

  operator fun invoke(map: Map<Var<X>, X> = emptyMap(), vMap: Map<VVar<X, *>, VConst<X, *>> = emptyMap()): VFun<X, E> = when (this) {
    is VNegative<X, E> -> VFun(length, value(map, vMap).contents.map { -it })
    is VSum<X, E> -> VFun(length, left(map, vMap).contents.zip(right(map, vMap)).map { it.first + it.second })
    is VProd<X, E> -> VFun(length, left(map, vMap).contents.zip(right(map, vMap)).map { it.first * it.second })
    else -> VFun(length, contents.map { it(map) })
  }

  open operator fun unaryMinus(): VFun<X, E> = VNegative(this)
  open operator fun plus(addend: VFun<X, E>): VFun<X, E> = VSum(this, addend)
  open operator fun times(multiplicand: VFun<X, E>): VFun<X, E> = VProd(this, multiplicand)
  open operator fun times(multiplicand: SFun<X>): VFun<X, E> = VFun(length, contents.map { it * multiplicand })
  open operator fun <Q: `1`> times(multiplicand: MFun<X, E, Q>): VFun<X, Q> = (expand * multiplicand).rows.first()

  infix fun dot(multiplicand: VFun<X, E>): SFun<X> =
    contents.reduceIndexed { index, acc, element -> acc + element * multiplicand[index] }

  override fun toString() = contents.joinToString(", ")
}

class VNegative<X: SFun<X>, E: `1`>(val value: VFun<X, E>): VFun<X, E>(value)
class VSum<X: SFun<X>, E: `1`>(val left: VFun<X, E>, val right: VFun<X, E>): VFun<X, E>(left, right)
class VProd<X: SFun<X>, E: `1`>(val left: VFun<X, E>, val right: VFun<X, E>): VFun<X, E>(left, right)
class VVar<X: SFun<X>, E: `1`>(val name: String, vararg val value: X): Variable, VFun<X, E>() { override val vVars: Set<VVar<X, *>> = setOf(this) }
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
  vararg val rows: VFun<X, C>
): List<VFun<X, C>> by rows.toList() {
  constructor(numRows: Nat<R>, numCols: Nat<C>, contents: List<VFun<X, C>>): this(numRows, numCols, *contents.toTypedArray())
  constructor(left: MFun<X, R, *>, right: MFun<X, *, C>): this(left.numRows, right.numCols)

  init {
    if (numRows.i != rows.size) throw IllegalArgumentException("Declared rows, $numRows != ${rows.size}")
  }

  val transpose by lazy { MFun(numCols, numRows, *cols) }
  val cols: Array<VFun<X, R>> by lazy { (0 until numCols.i).map { i -> VFun(numRows, rows.map { it[i] }) }.toTypedArray() }

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

class MSum<X: SFun<X>, R: `1`, C: `1`>(val left: MFun<X, R, C>, val right: MFun<X, R, C>): MFun<X, R, C>(left, right)
class MProd<X: SFun<X>, R: `1`, C1: `1`, C2: `1`>(val left: MFun<X, R, C1>, val right: MFun<X, C1, C2>): MFun<X, R, C2>(left, right)

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