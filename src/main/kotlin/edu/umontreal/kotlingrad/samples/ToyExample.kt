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
  }
}

interface Field<X: Field<X>> {
  val e: X
  val one: X
  val zero: X
  operator fun unaryMinus(): X
  operator fun plus(addend: X): X
  operator fun minus(subtrahend: X): X = this + -subtrahend
  operator fun times(multiplicand: X): X
  operator fun div(dividend: X): X = this * dividend.pow(-one)
  infix fun pow(exp: X): X
  fun ln(): X
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
  }

  open fun diff(variable: Var<X>): Fun<X> = when (this) {
    is Var -> if (variable == this) one else zero
    is Const -> zero
    is Sum -> left.diff(variable) + right.diff(variable)
    is Prod -> left.diff(variable) * right + left * right.diff(variable)
    is Power -> this * (exponent * Log(base)).diff(variable)
    is Negative -> -value.diff(variable)
    is Log -> logarithmand.pow(-one) * logarithmand.diff(variable)
  }

  override fun ln(): Fun<X> = Log(this)

  override fun pow(exp: Fun<X>): Fun<X> = Power(this, exp)

  override fun unaryMinus(): Fun<X> = Negative(this)

  override val e: Const<X> by lazy { proto.e }
  override val one: Const<X> by lazy { proto.one }
  override val zero: Const<X> by lazy { proto.zero }
  val proto: X by lazy { variables.first().value }

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