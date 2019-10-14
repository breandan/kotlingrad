@file:Suppress("FunctionName", "LocalVariableName", "unused")

package edu.umontreal.kotlingrad.samples

import kotlin.math.ln
import kotlin.math.pow

@Suppress("DuplicatedCode")
fun main() {
  with(DoublePrecision) {
    val f = x pow 2
    println(f(x to 3.0))
    println("f(x) = $f")
    val df_dx = f.df(x)
    println("f'(x) = $df_dx")
    println("f'(3) = ${df_dx(x to 3.0)}")

    val g = x pow x
    println("g(x) = $g")
    val dg_dx = g.df(x)
    println("g'(x) = $dg_dx")

    val q = y + z
    val h = x + q
    println("h(x) = $h")
    println("h(q = x^2) = ${h(q to (x pow 2))}")
    val dh_dx = h.df(x)
    println("h'(x) = $dh_dx")

    try {
      g.df(x, y, z)
      assert(false) { "An exception should have been thrown but wasn't!" }
    } catch (e: IllegalArgumentException) {
      println(e.localizedMessage)
    }
  }
}

/**
 * Algebraic primitives.
 */

interface Group<X : Group<X>> {
  operator fun unaryMinus(): X
  operator fun plus(addend: X): X
  operator fun minus(subtrahend: X): X = this + -subtrahend
  operator fun times(multiplicand: X): X
}

interface Field<X : Field<X>> : Group<X> {
  operator fun div(divisor: X): X
  infix fun pow(exp: X): X
  fun ln(): X
}

/**
 * Scalar function.
 */

sealed class Fun<X : Fun<X>>(open val vars: Set<Var<X>> = emptySet()) :
  Field<Fun<X>>,
  (Bindings<X>) -> Fun<X> {
  constructor(fn: Fun<X>) : this(fn.vars)
  constructor(vararg fns: Fun<X>) : this(fns.flatMap { it.vars }.toSet())

  override operator fun plus(addend: Fun<X>): Fun<X> = Sum(this, addend)
  override operator fun times(multiplicand: Fun<X>): Fun<X> = Prod(this, multiplicand)
  override operator fun div(divisor: Fun<X>): Fun<X> = this * divisor.pow(-One<X>())
  operator fun <E : `1`> times(multiplicand: VFun<X, E>): VFun<X, E> = SVProd(this, multiplicand)

  override operator fun invoke(bnds: Bindings<X>): Fun<X> =
    bnds.map.getOrElse(this) {
      when (this) {
        is Zero -> bnds.zero
        is One -> bnds.one
        is E -> bnds.E
        is Var -> this
        is SConst -> this
        is Prod -> left(bnds) * right(bnds)
        is Sum -> left(bnds) + right(bnds)
        is Power -> base(bnds) pow exponent(bnds)
        is Negative -> -value(bnds)
        is Log -> logarithmand(bnds).ln()
        is Df -> ad()(bnds)
      }
    }

  open fun df(vararg variable: Var<X>): Fun<X> = Df(this, *variable).apply()

  override fun ln(): Fun<X> = Log(this)

  override fun pow(exp: Fun<X>): Fun<X> = Power(this, exp)

  override fun unaryMinus(): Fun<X> = Negative(this)

  fun sqrt(): Fun<X> = this pow (One<X>() / (Two<X>()))

  override fun toString(): String = when {
    this is Log -> "ln($logarithmand)"
    this is Negative -> "-($value)"
    this is Power -> "($base) pow ($exponent)"
    this is Prod && right is Sum -> "$left * ($right)"
    this is Prod && left is Sum -> "($left) * $right"
    this is Prod -> "($left) * ($right)"
    this is Sum && right is Negative -> "$left - ${right.value}"
    this is Sum -> "$left + $right"
    this is Var -> name
    this is Df -> "d($fn) / d(${vrbs.joinToString(", ")})"
    this is Zero -> "\uD835\uDFD8"
    this is One -> "\uD835\uDFD9"
    this is E -> "ⅇ"
    else -> super.toString()
  }
}

/**
 * Symbolic operators.
 */

class Sum<X : Fun<X>>(val left: Fun<X>, val right: Fun<X>) : Fun<X>(left, right)
class Negative<X : Fun<X>>(val value: Fun<X>) : Fun<X>(value)
class Prod<X : Fun<X>>(val left: Fun<X>, val right: Fun<X>) : Fun<X>(left, right)
class Power<X : Fun<X>> internal constructor(val base: Fun<X>, val exponent: Fun<X>) : Fun<X>(base, exponent)
class Log<X : Fun<X>> internal constructor(val logarithmand: Fun<X>) : Fun<X>(logarithmand)
class Df<X : Fun<X>> internal constructor(val fn: Fun<X>, vararg val vrbs: Var<X>) : Fun<X>(fn, *vrbs) {
  init {
    vrbs.filter { it !in fn.vars }.let { require(it.isEmpty()) { "Variables: $it not in function!" } }
  }

  fun apply() = fn.ad()

  fun Fun<X>.ad(): Fun<X> = when (this) {
    is Var -> if (this in vrbs) One() else Zero()
    is SConst -> Zero()
    is Sum -> left.ad() + right.ad()
    is Prod -> left.ad() * right + left * right.ad()
    is Power -> this * (exponent * Log(base)).ad()
    is Negative -> -value.ad()
    is Log -> (logarithmand pow -One<X>()) * logarithmand.ad()
    is Df -> fn.ad()
  }
}

data class Bindings<X: Fun<X>> (val map: Map<Fun<X>, Fun<X>>, 
                           val zero: Fun<X>,
                           val one: Fun<X>,
                           val two: Fun<X>,
                           val E: Fun<X>)

interface Variable { val name: String }

class Var<X : Fun<X>>(override val name: String) : Variable, Fun<X>() {
  override val vars: Set<Var<X>> = setOf(this)
}

open class SConst<X : Fun<X>> : Fun<X>()
class Zero<X: Fun<X>> : SConst<X>()
class One<X: Fun<X>> : SConst<X>()
class Two<X: Fun<X>> : SConst<X>()
class E<X: Fun<X>> : SConst<X>()

abstract class RealNumber<X : Fun<X>>(open val value: Number) : SConst<X>()

class DoubleReal(override val value: Double) : RealNumber<DoubleReal>(value) {
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

sealed class Protocol<X : RealNumber<X>> {
  abstract fun wrap(default: Number): X

  operator fun Number.times(multiplicand: Fun<X>) = multiplicand * wrap(this)
  operator fun Fun<X>.times(multiplicand: Number) = wrap(multiplicand) * this

  operator fun Number.plus(addend: Fun<X>) = addend + wrap(this)
  operator fun Fun<X>.plus(addend: Number) = wrap(addend) + this

  fun Number.pow(exp: Fun<X>) = wrap(this) pow exp
  infix fun Fun<X>.pow(exp: Number) = this pow wrap(exp)
}

object DoublePrecision : Protocol<DoubleReal>() {
  override fun wrap(default: Number): DoubleReal = DoubleReal(default.toDouble())

  fun vrb(name: String) = Var<DoubleReal>(name)

  @JvmName("ValBnd") operator fun Fun<DoubleReal>.invoke(vararg pairs: Pair<Var<DoubleReal>, Number>) =
    this(Bindings(pairs.map { (it.first to wrap(it.second)) }.toMap(), wrap(0.0), wrap(1.0), wrap(2.0), wrap(kotlin.math.E)))
  @JvmName("FunBnd") operator fun Fun<DoubleReal>.invoke(vararg pairs: Pair<Fun<DoubleReal>, Fun<DoubleReal>>) =
    this(Bindings(pairs.map { (it.first to it.second) }.toMap(), wrap(0.0), wrap(1.0), wrap(2.0), wrap(kotlin.math.E)))

  operator fun <Y : `1`> VFun<DoubleReal, Y>.invoke(vararg sPairs: Pair<Var<DoubleReal>, Number>) =
    this(sPairs.map { (it.first to wrap(it.second)) }.toMap())

  val x = vrb("x")
  val y = vrb("y")
  val z = vrb("z")

  fun Vec(d0: Double) = VFun(DoubleReal(d0))
  fun Vec(d0: Double, d1: Double): VFun<DoubleReal, `2`> = VFun(DoubleReal(d0), DoubleReal(d1))
  fun Vec(d0: Double, d1: Double, d2: Double): VFun<DoubleReal, `3`> = VFun(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double) = VFun(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2), DoubleReal(d3))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double) = VFun(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2), DoubleReal(d3), DoubleReal(d4))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double) = VFun(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2), DoubleReal(d3), DoubleReal(d4), DoubleReal(d5))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double, d6: Double) = VFun(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2), DoubleReal(d3), DoubleReal(d4), DoubleReal(d5), DoubleReal(d6))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double, d6: Double, d7: Double) = VFun(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2), DoubleReal(d3), DoubleReal(d4), DoubleReal(d5), DoubleReal(d6), DoubleReal(d7))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double, d6: Double, d7: Double, d8: Double) = VFun(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2), DoubleReal(d3), DoubleReal(d4), DoubleReal(d5), DoubleReal(d6), DoubleReal(d7), DoubleReal(d8))
}