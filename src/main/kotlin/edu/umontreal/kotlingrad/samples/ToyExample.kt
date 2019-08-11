package edu.umontreal.kotlingrad.samples

import kotlin.math.*

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

sealed class Fun<X: Fun<X>>(open val vars: Set<Var<X>> = emptySet()): Field<Fun<X>>, (Map<Var<X>, X>) -> Fun<X> {
  constructor(fn: Fun<X>): this(fn.vars)
  constructor(vararg fns: Fun<X>): this(fns.flatMap { it.vars }.toSet())

  override operator fun plus(addend: Fun<X>): Fun<X> = Sum(this, addend)
  override operator fun times(multiplicand: Fun<X>): Fun<X> = Prod(this, multiplicand)

  operator fun <E: `1`> times(multiplicand: VFun<X, E>): VFun<X, E> = SVProd(this, multiplicand)

  override operator fun invoke(map: Map<Var<X>, X>): Fun<X> = when (this) {
    is SConst -> this
    is Var -> map.getOrElse(this) { this }
    is Prod -> left(map) * right(map)
    is Sum -> left(map) + right(map)
    is Power -> base(map) pow exponent(map)
    is Negative -> -value(map)
    is Log -> logarithmand(map).ln()
  }

  open fun diff(variable: Var<X>): Fun<X> = when (this) {
    is Var -> if (variable == this) one else zero
    is SConst -> zero
    is Sum -> left.diff(variable) + right.diff(variable)
    is Prod -> left.diff(variable) * right + left * right.diff(variable)
    is Power -> this * (exponent * Log(base)).diff(variable)
    is Negative -> -value.diff(variable)
    is Log -> logarithmand.pow(-one) * logarithmand.diff(variable)
  }

  override fun ln(): Fun<X> = Log(this)

  override fun pow(exp: Fun<X>): Fun<X> = Power(this, exp)

  override fun unaryMinus(): Fun<X> = Negative(this)

  override val e: SConst<X> by lazy { proto.e }
  override val one: SConst<X> by lazy { proto.one }
  override val zero: SConst<X> by lazy { proto.zero }
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
    this is Var -> name
    else -> super.toString()
  }
}

/**
 * Symbolic operators.
 */

class Sum<X: Fun<X>>(val left: Fun<X>, val right: Fun<X>): Fun<X>(left, right)
class Negative<X: Fun<X>>(val value: Fun<X>): Fun<X>(value)
class Prod<X: Fun<X>>(val left: Fun<X>, val right: Fun<X>): Fun<X>(left, right)
class Power<X: Fun<X>> internal constructor(val base: Fun<X>, val exponent: Fun<X>): Fun<X>(base, exponent)
class Log<X: Fun<X>> internal constructor(val logarithmand: Fun<X>): Fun<X>(logarithmand)
interface Variable { val name: String }
class Var<X: Fun<X>>(override val name: String, val value: X): Variable, Fun<X>() { override val vars: Set<Var<X>> = setOf(this) }

open class SConst<X: Fun<X>>: Fun<X>()
abstract class RealNumber<X: Fun<X>>(open val value: Number): SConst<X>()

class DoubleReal(override val value: Double): RealNumber<DoubleReal>(value) {
  override val e by lazy { DoubleReal(E) }
  override val one by lazy { DoubleReal(1.0) }
  override val zero by lazy { DoubleReal(0.0) }

  override fun unaryMinus() = DoubleReal(-value)
  override fun ln() = DoubleReal(ln(value))
  override fun toString() = value.toString()

  /**
   * Constant propagation.
   */

  override fun plus(addend: Fun<DoubleReal>): Fun<DoubleReal> = when (addend) {
    is DoubleReal -> DoubleReal(value + addend.value)
    else -> super.plus(addend)
  }

  override fun times(multiplicand: Fun<DoubleReal>): Fun<DoubleReal> = when (multiplicand) {
    is DoubleReal -> DoubleReal(value * multiplicand.value)
    else -> super.times(multiplicand)
  }

  override fun pow(exp: Fun<DoubleReal>) = when (exp) {
    is DoubleReal -> DoubleReal(value.pow(exp.value))
    else -> super.pow(exp)
  }
}

/**
 * Numerical context.
 */

sealed class Protocol<X: RealNumber<X>> {
  abstract fun wrap(default: Number): X

  operator fun Number.times(multiplicand: Fun<X>) = multiplicand * wrap(this)
  operator fun Fun<X>.times(multiplicand: Number) = wrap(multiplicand) * this

  operator fun Number.plus(addend: Fun<X>) = addend + wrap(this)
  operator fun Fun<X>.plus(addend: Number) = wrap(addend) + this

  fun Number.pow(exp: Fun<X>) = wrap(this) pow exp
  infix fun Fun<X>.pow(exp: Number) = this pow wrap(exp)
}

object DoublePrecision: Protocol<DoubleReal>() {
  override fun wrap(default: Number): DoubleReal = DoubleReal(default.toDouble())

  fun Var(name: String, default: Number) = Var(name, wrap(default))
  operator fun Fun<DoubleReal>.invoke(vararg pairs: Pair<Var<DoubleReal>, Number>) =
    this(pairs.map { (it.first to wrap(it.second)) }.toMap())
  operator fun VFun<DoubleReal, *>.invoke(vararg sPairs: Pair<Var<DoubleReal>, Number>) =
    this(sPairs.map { (it.first to wrap(it.second)) }.toMap())
}