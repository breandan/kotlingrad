package edu.umontreal.kotlingrad.samples

fun main() {
  with(DoubleProtocol) {
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

interface Group<X: Group<X>> {
  operator fun unaryMinus(): X
  infix operator fun minus(subtrahend: X): X = this + -subtrahend
  operator fun plus(addend: X): X
  operator fun times(multiplicand: X): X
  val one: X
  val zero: X
}

interface Field<X: Field<X>>: Group<X> {
  val e: X

  operator fun div(dividend: X): X = this * dividend.inverse()
  fun inverse(): X

  infix fun pow(exp: X): X
  fun log(): X
}

abstract class RealNumber<X: Field<X>>: Field<X>

class DoubleReal(val value: Double): RealNumber<DoubleReal>() {
  override val one by lazy { DoubleReal(1.0) }
  override val zero by lazy { DoubleReal(0.0) }
  override val e by lazy { DoubleReal(Math.E) }

  override fun plus(addend: DoubleReal) = DoubleReal(value + addend.value)
  override fun unaryMinus() = DoubleReal(-value)
  override fun times(multiplicand: DoubleReal) = DoubleReal(value * multiplicand.value)
  override fun inverse() = DoubleReal(1.0 / value)
  override fun pow(exp: DoubleReal) = DoubleReal(Math.pow(value, exp.value))
  override fun log() = DoubleReal(Math.log(value))
  override fun toString() = value.toString()
}

sealed class Fun<X: Field<X>>(open val variables: Set<Var<X>> = emptySet()): Field<Fun<X>> {
  constructor(fn: Fun<X>): this(fn.variables)
  constructor(vararg fns: Fun<X>): this(fns.flatMap { it.variables }.toSet())

  override operator fun plus(addend: Fun<X>) = Sum(this, addend)
  override operator fun times(multiplicand: Fun<X>) = Prod(this, multiplicand)

  operator fun invoke(map: Map<Var<X>, X>): X = when (this) {
    is Const -> value
    is Var -> map.getOrElse(this) { value }
    is Prod -> left(map) * right(map)
    is Sum -> left(map) + right(map)
    is Power -> base(map) pow exponent(map)
    is Negative -> -value(map)
    is Log -> logarithmand(map).log()
  }

  open fun diff(variable: Var<X>): Fun<X> = when (this) {
    is Var -> if (variable == this) one else zero
    is Const -> zero
    is Sum -> left.diff(variable) + right.diff(variable)
    is Prod -> left.diff(variable) * right + left * right.diff(variable)
    is Power -> this * (exponent * Log(base)).diff(variable)
    is Negative -> -value.diff(variable)
    is Log -> logarithmand.inverse() * logarithmand.diff(variable)
  }

  override fun inverse() = pow(-one)
  override fun log() = Log(this)

  override fun pow(exp: Fun<X>) = Power(this, exp)

  override fun unaryMinus(): Fun<X> = Negative(this)

  override val one: Const<X> by lazy { Const(this(emptyMap()).one) }
  override val zero: Const<X> by lazy { Const(this(emptyMap()).zero) }
  override val e: Const<X> by lazy { Const(this(emptyMap()).e) }

  override fun toString(): String = when {
    this is Log -> "ln($logarithmand)"
    this is Negative -> "-$value"
    this is Power -> "$base^($exponent)"
    this is Prod && right is Sum -> "$left⋅($right)"
    this is Prod && left is Sum -> "($left)⋅$right"
    this is Prod -> "$left⋅$right"
    this is Sum && right is Negative -> "$left - ${right.value}"
    this is Sum -> "$left + $right"
    this is Const -> "$value"
    this is Var -> name
    else -> super.toString()
  }
}

class Const<X: Field<X>>(val value: X): Fun<X>()
class Sum<X: Field<X>>(val left: Fun<X>, val right: Fun<X>): Fun<X>()
class Negative<X: Field<X>>(val value: Fun<X>): Fun<X>()
class Prod<X: Field<X>>(val left: Fun<X>, val right: Fun<X>): Fun<X>()
class Power<X: Field<X>> internal constructor(val base: Fun<X>, val exponent: Fun<X>): Fun<X>(base, exponent)
class Log<X: Field<X>> internal constructor(val logarithmand: Fun<X>): Fun<X>(logarithmand)
class Var<X: Field<X>>(val name: String, val value: X): Fun<X>() {
  override val variables: Set<Var<X>> = setOf(this)
}

sealed class Protocol<X: RealNumber<X>> {
  abstract fun wrap(default: Number): X

  operator fun Number.times(multiplicand: Fun<X>) = multiplicand * Const(wrap(this))
  operator fun Fun<X>.times(multiplicand: Number) = Const(wrap(multiplicand)) * this

  operator fun Number.plus(addend: Fun<X>) = addend * Const(wrap(this))
  operator fun Fun<X>.plus(addend: Number) = Const(wrap(addend)) * this

  fun Number.pow(exp: Fun<X>) = Const(wrap(this)) pow exp
  infix fun Fun<X>.pow(exp: Number) = this pow Const(wrap(exp))
}

object DoubleProtocol: Protocol<DoubleReal>() {
  override fun wrap(default: Number): DoubleReal = DoubleReal(default.toDouble())
}