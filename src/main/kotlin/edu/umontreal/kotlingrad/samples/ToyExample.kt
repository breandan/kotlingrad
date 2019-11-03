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
    val df_dx = f.d(x)
    println("f'(x) = $df_dx")
    println("f'(3) = ${df_dx(x to 3.0)}")

    val g = x pow x
    println("g(x) = $g")
    val dg_dx = g.d(x)
    println("g'(x) = $dg_dx")

    val q = y + z
    val h = x + q
    println("h(x) = $h")
    println("h(q = x^2) = ${h(q to (x pow 2))}")
    val dh_dx = h.d(x)
    println("h'(x) = $dh_dx")

    val t = g.d(x, y, z)
  }
}

/**
 * Algebraic primitives.
 */

interface Group<X : Group<X>> {
  operator fun unaryMinus(): X
  operator fun plus(addend: X): X
  operator fun minus(subtrahend: X): X = this + -subtrahend
}

interface Field<X : Field<X>> : Group<X> {
  operator fun div(divisor: X): X
  operator fun times(multiplicand: X): X

  infix fun pow(exp: X): X
  fun ln(): X
}

/**
 * Scalar function.
 */

sealed class Fun<X : Fun<X>>(open val sVars: Set<Var<X>> = emptySet()
                             //,open val vVars: Set<VVar<X, *>> = emptySet()
): Field<Fun<X>>, (Bindings<X>) -> Fun<X> {
  constructor(fn: Fun<X>) : this(fn.sVars)//, fn.vVars)
  constructor(vararg fns: Fun<X>) : this(fns.flatMap { it.sVars }.toSet()) //fns.flatMap { it.vVars }.toSet())

  override operator fun plus(addend: Fun<X>): Fun<X> = Sum(this, addend)
  override operator fun times(multiplicand: Fun<X>): Fun<X> = Prod(this, multiplicand)
  override operator fun div(divisor: Fun<X>): Fun<X> = this * divisor.pow(-One<X>())
  open operator fun <E : `1`> times(multiplicand: VFun<X, E>): VFun<X, E> = SVProd(this, multiplicand)
  open operator fun <R : `1`, C: `1`> times(multiplicand: MFun<X, R, C>): MFun<X, R, C> = SMProd(this, multiplicand)

  override operator fun invoke(bnds: Bindings<X>): Fun<X> =
    bnds.sMap.getOrElse(this) {
      when (this) {
        is Zero -> bnds.zero
        is One -> bnds.one
        is Two -> bnds.two
        is E -> bnds.e
        is Var -> this
        is SConst -> this
        is Prod -> left(bnds) * right(bnds)
        is Sum -> left(bnds) + right(bnds)
        is Power -> base(bnds) pow exponent(bnds)
        is Negative -> -value(bnds)
        is Log -> logarithmand(bnds).ln()
        is Derivative -> df()(bnds)
        is DProd -> DProd(left(bnds), right(bnds))
        is VMagnitude -> VMagnitude(value(bnds))
      }
    }

  operator fun invoke(): Fun<X> = invoke(Bindings())

  open fun d(v1: Var<X>): Fun<X> = Derivative(this, v1)
  open fun d(v1: Var<X>, v2: Var<X>): Vec<X, `2`> = Vec(Derivative(this, v1), Derivative(this, v2))
  open fun d(v1: Var<X>, v2: Var<X>, v3: Var<X>): Vec<X, `3`> = Vec(Derivative(this, v1), Derivative(this, v2), Derivative(this, v3))
  open fun d(vararg vars: Var<X>): Map<Var<X>, Fun<X>> = vars.map { it to Derivative(this, it) }.toMap()

  override fun ln(): Fun<X> = Log(this)

  override fun pow(exp: Fun<X>): Fun<X> = Power(this, exp)

  override fun unaryMinus(): Fun<X> = Negative(this)

  open fun sqrt(): Fun<X> = this pow (One<X>() / (Two<X>()))

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
    this is Derivative -> "d($fn) / d($vrb)"
    this is Zero -> "\uD835\uDFD8"
    this is One -> "\uD835\uDFD9"
    this is Two -> "\uD835\uDFDA"
    this is E -> "ⅇ"
    this is VMagnitude -> "|$value|"
    this is DProd -> "($left) dot ($right)"
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
class Derivative<X : Fun<X>> internal constructor(val fn: Fun<X>, val vrb: Var<X>) : Fun<X>(fn, vrb) {
  fun Fun<X>.df(): Fun<X> = when (this) {
    is Var -> if (this == vrb) One() else Zero()
    is SConst -> Zero()
    is Sum -> left.df() + right.df()
    is Prod -> left.df() * right + left * right.df()
    is Power -> this * (exponent * Log(base)).df()
    is Negative -> -value.df()
    is Log -> (logarithmand pow -One<X>()) * logarithmand.df()
    is Derivative -> fn.df()
    is DProd -> this().df()
    is VMagnitude -> this().df()
  }
}

data class Bindings<X: Fun<X>>(
  val sMap: Map<Fun<X>, Fun<X>> = mapOf(),
//  val vMap: Map<VFun<X, *>, VFun<X, *>> = mapOf(),
  val zero: Fun<X> = Zero(),
  val one: Fun<X> = One(),
  val two: Fun<X> = Two(),
  val e: Fun<X> = E()) {
//  constructor(sMap: Map<Fun<X>, Fun<X>>,
//              vMap: Map<VFun<X, *>, VFun<X, *>>,
//              zero: Fun<X>,
//              one: Fun<X>,
//              two: Fun<X>,
//              E: Fun<X>): this(sMap, zero, one, two, E)
}

class DProd<X: Fun<X>>(val left: VFun<X, *>, val right: VFun<X, *>): Fun<X>(left.sVars + right.sVars)//, left.vVars + right.vVars)

class VMagnitude<X: Fun<X>>(val value: VFun<X, *>): Fun<X>(value.sVars)//, value.vVars)

interface Variable { val name: String }

class Var<X : Fun<X>>(override val name: String) : Variable, Fun<X>() {
  override val sVars: Set<Var<X>> = setOf(this)
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

  override fun plus(addend: Fun<DoubleReal>) = when (addend) {
    is DoubleReal -> DoubleReal(value + addend.value)
    else -> super.plus(addend)
  }

  override fun times(multiplicand: Fun<DoubleReal>) = when (multiplicand) {
    is DoubleReal -> DoubleReal(value * multiplicand.value)
    else -> super.times(multiplicand)
  }

  override fun pow(exp: Fun<DoubleReal>) = when (exp) {
    is DoubleReal -> DoubleReal(value.pow(exp.value))
    else -> super.pow(exp)
  }

  override fun sqrt() = DoubleReal(kotlin.math.sqrt(value))
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

  val one = wrap(1.0)
  val zero = wrap(0.0)
  val two = wrap(2.0)
  val e = wrap(kotlin.math.E)

  fun vrb(name: String) = Var<DoubleReal>(name)

  @JvmName("ValBnd") operator fun Fun<DoubleReal>.invoke(vararg pairs: Pair<Var<DoubleReal>, Number>) =
    this(Bindings(pairs.map { (it.first to wrap(it.second)) }.toMap(), zero, one, two, e))
  @JvmName("FunBnd") operator fun Fun<DoubleReal>.invoke(vararg pairs: Pair<Fun<DoubleReal>, Fun<DoubleReal>>) =
    this(Bindings(pairs.map { (it.first to it.second) }.toMap(), zero, one, two, e))

  operator fun <Y : `1`> VFun<DoubleReal, Y>.invoke(vararg sPairs: Pair<Var<DoubleReal>, Number>) =
    this(Bindings(sPairs.map { (it.first to wrap(it.second)) }.toMap(), zero, one, two, e))

  fun Fun<DoubleReal>.asDouble() = (this as DoubleReal).value

  val x = vrb("x")
  val y = vrb("y")
  val z = vrb("z")

  fun Vec(d0: Double) = Vec(DoubleReal(d0))
  fun Vec(d0: Double, d1: Double) = Vec(DoubleReal(d0), DoubleReal(d1))
  fun Vec(d0: Double, d1: Double, d2: Double) = Vec(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double) = Vec(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2), DoubleReal(d3))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double) = Vec(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2), DoubleReal(d3), DoubleReal(d4))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double) = Vec(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2), DoubleReal(d3), DoubleReal(d4), DoubleReal(d5))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double, d6: Double) = Vec(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2), DoubleReal(d3), DoubleReal(d4), DoubleReal(d5), DoubleReal(d6))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double, d6: Double, d7: Double) = Vec(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2), DoubleReal(d3), DoubleReal(d4), DoubleReal(d5), DoubleReal(d6), DoubleReal(d7))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double, d6: Double, d7: Double, d8: Double) = Vec(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2), DoubleReal(d3), DoubleReal(d4), DoubleReal(d5), DoubleReal(d6), DoubleReal(d7), DoubleReal(d8))
}