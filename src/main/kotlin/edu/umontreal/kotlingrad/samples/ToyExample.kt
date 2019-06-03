package edu.umontreal.kotlingrad.samples

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
    val q = vf1 * vf2
    println(q)

//    val mf1 = MFun(`2`, `1`, VFun(`1`, y * y), VFun(`1`, x * y))
//    val mf2 = MFun(`1`, `2`, vf2)
//    val mf3 = MFun(`3`, `1`, VFun(`1`, x), VFun(`1`, y), VFun(`1`, x))
//    // TODO: Look into this * operator
//    println(mf1 * mf2) // 2*1 x 1*2
//    println(mf1 * vf1) // 2*1 x 2
//    println(mf2 * vf1) // 1*2 x 2
//    println(mf3 * vf1) // 1*3 x 2
//    println(mf2 * x)   // 1*2 x 1
  }
}

interface Group<X: Group<X>> {
  val one: X
  val zero: X
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
}

abstract class RealNumber<X: Fun<X>>(open val value: Number): Const<X>()

class DoubleReal(override val value: Double): RealNumber<DoubleReal>(value) {
  override val e by lazy { DoubleReal(Math.E) }
  override val one by lazy { DoubleReal(1.0) }
  override val zero by lazy { DoubleReal(0.0) }

  override fun plus(addend: Fun<DoubleReal>): Fun<DoubleReal> = when (addend) {
    is DoubleReal -> DoubleReal(value + addend.value)
    else -> super.plus(addend)
  }

  override fun unaryMinus() = DoubleReal(-value)

  override fun times(multiplicand: Fun<DoubleReal>): Fun<DoubleReal> = when (multiplicand) {
    is DoubleReal -> DoubleReal(value * multiplicand.value)
    else -> super.times(multiplicand)
  }

  override fun pow(exp: Fun<DoubleReal>) = when (exp) {
    is DoubleReal -> DoubleReal(Math.pow(value, exp.value))
    else -> super.pow(exp)
  }

  override fun ln() = DoubleReal(Math.log(value))
  override fun toString() = value.toString()
}

sealed class Fun<X: Fun<X>>(open val variables: Set<Var<X>> = emptySet()): Field<Fun<X>> {
  constructor(fn: Fun<X>): this(fn.variables)
  constructor(vararg fns: Fun<X>): this(fns.flatMap { it.variables }.toSet())

  override operator fun plus(addend: Fun<X>): Fun<X> = Sum(this, addend)
  override operator fun times(multiplicand: Fun<X>): Fun<X> = Prod(this, multiplicand)

  operator fun invoke(map: Map<Var<X>, X>): Fun<X> = when (this) {
    is Const -> this
    is Var -> map.getOrElse(this) { this }
    is Prod -> left(map) * right(map)
    is Sum -> left(map) + right(map)
    is Power -> base(map) pow exponent(map)
    is Negative -> -value(map)
    is Log -> logarithmand(map).ln()

    else -> throw ClassNotFoundException("Unknown class:$this")
  }

  open fun diff(variable: Var<X>): Fun<X> = when (this) {
    is Var -> if (variable == this) one else zero
    is Const -> zero
    is Sum -> left.diff(variable) + right.diff(variable)
    is Prod -> left.diff(variable) * right + left * right.diff(variable)
    is Power -> this * (exponent * Log(base)).diff(variable)
    is Negative -> -value.diff(variable)
    is Log -> logarithmand.pow(-one) * logarithmand.diff(variable)

    else -> throw ClassNotFoundException("Unknown class:$this")
  }

  override fun ln(): Fun<X> = Log(this)

  override fun pow(exp: Fun<X>): Fun<X> = Power(this, exp)

  override fun unaryMinus(): Fun<X> = Negative(this)

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

open class VFun<Y: Fun<Y>, X: VFun<Y, X, E>, E: `10`>(val length: Nat<E>, vararg val contents: Fun<Y>): Group<VFun<Y, X, E>> {
//  constructor(length: Nat<E>, contents: List<Fun<Y>>): this(length, *contents.toTypedArray())
  override val one: VFun<Y, X, E>
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
  override val zero: VFun<Y, X, E>
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

  override fun unaryMinus(): VFun<Y, X, E> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  //override fun plus(addend: VFun<Y, X, E>): VFun<Y, X, E> = this
  override fun plus(addend: VFun<Y, X, E>): VFun<Y, X, E> = VFun(length, *contents.mapIndexed { i, p -> p + addend[i] }.toTypedArray())

  override fun times(multiplicand: VFun<Y, X, E>): VFun<Y, X, E> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

//
//  init {
//    if (length.i != contents.size) throw IllegalArgumentException("Declared $length != ${contents.size}")
//  }
//
//  
  operator fun get(i: Int): Fun<Y> = contents[i]
//  fun plus(addend: VFun<X, E>): VFun<X, E> = VFun(length, contents.mapIndexed { i, p -> p + addend[i] })
//  override fun plus(addend: VFun<X, E>): VFun<X, E> = VFun(length, contents.map { it + addend })
//
//  override fun times(multiplicand: Fun<X>): VFun<X, E> = VFun(length, contents.map { it * multiplicand })
//  operator fun times(multiplicand: VFun<X, E>): Fun<X> =
//    contents.foldIndexed(zero as Fun<X>) { index, acc, t -> acc + t * multiplicand[index] }
//  operator fun <Q: `10`> times(multiplicand: MFun<X, E, Q>) = multiplicand * this
//
//  val upcast: MFun<X, `1`, E> by lazy { MFun(`1`, length, this) }
}

//class MFun<X: Fun<X>, R: `10`, C: `10`>(val numRows: Nat<R>, val numCols: Nat<C>, vararg val rows: VFun<X, C>): VFun<X, R>(numRows, *rows) {
//  constructor(numRows: Nat<R>, numCols: Nat<C>, contents: List<VFun<X, C>>): this(numRows, numCols, *contents.toTypedArray())
//  val cols: Array<VFun<X, R>> by lazy { (0 until numCols.i).map { i -> VFun(numRows, rows.map { it[i] }) }.toTypedArray() }
//
//  operator fun <Q: `10`> times(multiplicand: MFun<X, C, Q>): MFun<X, R, Q> =
//    MFun(numRows, multiplicand.numCols, (0 until numRows.i).map { i ->
//        VFun(multiplicand.numCols, (0 until multiplicand.numCols.i).map { j ->
//          rows[i] * multiplicand.cols[j] }) })
//
////  override fun times(multiplicand: Fun<X>): MFun<X, R, C> = MFun(numRows, numCols, rows.map { it * multiplicand })
//  operator fun times(multiplicand: VFun<X, C>): MFun<X, R, `1`> = this * multiplicand.upcast.transpose
//
//  val transpose by lazy { MFun(numCols, numRows, *cols) }
//
//  override fun toString() = "($numRows x $numCols)\n[${rows.joinToString("\n ") { it.contents.joinToString(", ") }}]"
//}

open class Const<X: Fun<X>>: Fun<X>()
class Sum<X: Fun<X>>(val left: Fun<X>, val right: Fun<X>): Fun<X>(left, right)
class Negative<X: Fun<X>>(val value: Fun<X>): Fun<X>(value)
class Prod<X: Fun<X>>(val left: Fun<X>, val right: Fun<X>): Fun<X>(left, right)
class Power<X: Fun<X>> internal constructor(val base: Fun<X>, val exponent: Fun<X>): Fun<X>(base, exponent)
class Log<X: Fun<X>> internal constructor(val logarithmand: Fun<X>): Fun<X>(logarithmand)
class Var<X: Fun<X>>(val name: String, val value: X): Fun<X>() {
  override val variables: Set<Var<X>> = setOf(this)
}

sealed class Protocol<X: RealNumber<X>> {
  abstract fun wrap(default: Number): X

  operator fun Number.times(multiplicand: Fun<X>) = multiplicand * wrap(this)
  operator fun Fun<X>.times(multiplicand: Number) = wrap(multiplicand) * this

  operator fun Number.plus(addend: Fun<X>) = addend * wrap(this)
  operator fun Fun<X>.plus(addend: Number) = wrap(addend) * this

  fun Number.pow(exp: Fun<X>) = wrap(this) pow exp
  infix fun Fun<X>.pow(exp: Number) = this pow wrap(exp)
}

object DoublePrecision: Protocol<DoubleReal>() {
  override fun wrap(default: Number): DoubleReal = DoubleReal(default.toDouble())
}

open class `0`(override val i: Int = 0): `1`(i)  { companion object: `0`(), Nat<`0`> }
open class `1`(override val i: Int = 1): `2`(i)  { companion object: `1`(), Nat<`1`> }
open class `2`(override val i: Int = 2): `3`(i)  { companion object: `2`(), Nat<`2`> }
open class `3`(override val i: Int = 3): `4`(i)  { companion object: `3`(), Nat<`3`> }
open class `4`(override val i: Int = 4): `5`(i)  { companion object: `4`(), Nat<`4`> }
open class `5`(override val i: Int = 5): `6`(i)  { companion object: `5`(), Nat<`5`> }
open class `6`(override val i: Int = 6): `7`(i)  { companion object: `6`(), Nat<`6`> }
open class `7`(override val i: Int = 7): `8`(i)  { companion object: `7`(), Nat<`7`> }
open class `8`(override val i: Int = 8): `9`(i)  { companion object: `8`(), Nat<`8`> }
open class `9`(override val i: Int = 9): `10`(i) { companion object: `9`(), Nat<`9`> }

sealed class `10`(open val i: Int = 10) {
  companion object: `10`(), Nat<`10`>

  override fun toString() = "$i"
}

interface Nat<T: `10`> { val i: Int }
