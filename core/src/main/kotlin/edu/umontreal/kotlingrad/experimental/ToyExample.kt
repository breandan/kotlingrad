@file:Suppress("FunctionName", "LocalVariableName", "unused")

package edu.umontreal.kotlingrad.experimental

import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.minus
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.MutableNode
import kotlin.math.*

@Suppress("DuplicatedCode")
fun main() {
  with(DoublePrecision) {
    val f = x pow 3
    println(f(x to 3.0))
    println("f(x) = $f")
    val df_dx = f.d(x)
    println("f'(x) = $df_dx")
    println("f'(3) = ${df_dx.invoke(x to 3.0)}") // Should be 27
    println("f''(2) = ${df_dx.d(x)(x to 2.0)}")  // Should be 12

    val g = x pow x
    println("g(x) = $g")
    val dg_dx = g.d(x)
    println("g'(x) = $dg_dx")

    val q = y + z
    val h = x + q / x
    println("h(x) = $h")
    val j = h(q to (x pow 2))
    println("h(q = x^2) = j = $j")
    val k = j(x to 3)
    println("j(x = 2) = $k")
    val dh_dx = h.d(x)
    println("h'(x) = $dh_dx")
    println("h'(1, 2, 3) = ${dh_dx(x to 1, y to 2, z to 3)}")

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

sealed class Fun<X : Fun<X>>(open val sVars: Set<Var<X>> = emptySet()) : Field<Fun<X>>, (Bindings<X>) -> Fun<X> {
  constructor(fn: Fun<X>) : this(fn.sVars)
  constructor(vararg fns: Fun<X>) : this(fns.flatMap { it.sVars }.toSet())

  override operator fun plus(addend: Fun<X>): Fun<X> = Sum(this, addend)
  override operator fun times(multiplicand: Fun<X>): Fun<X> = Prod(this, multiplicand)
  override operator fun div(divisor: Fun<X>): Fun<X> = this * divisor.pow(-One<X>())
  open operator fun <E : D1> times(multiplicand: VFun<X, E>): VFun<X, E> = SVProd(this, multiplicand)
  open operator fun <R : D1, C : D1> times(multiplicand: MFun<X, R, C>): MFun<X, R, C> = SMProd(this, multiplicand)

  override operator fun invoke(bnds: Bindings<X>): Fun<X> =
      Composition(this, bnds).run { if (bnds.isReassignmentFree) evaluate else this }

  open operator fun invoke(): Fun<X> = invoke(Bindings())

  open fun d(v1: Var<X>): Fun<X> = Derivative(this, v1)
  open fun d(v1: Var<X>, v2: Var<X>): Vec<X, D2> = Vec(Derivative(this, v1), Derivative(this, v2))
  open fun d(v1: Var<X>, v2: Var<X>, v3: Var<X>): Vec<X, D3> = Vec(Derivative(this, v1), Derivative(this, v2), Derivative(this, v3))
  open fun d(vararg vars: Var<X>): Map<Var<X>, Fun<X>> = vars.map { it to Derivative(this, it) }.toMap()
  open fun <R : D1, C : D1> d(mv: Mat<X, R, C>) = d(*mv.sVars.toTypedArray()).values.foldIndexed(mutableListOf()) { index, acc: MutableList<MutableList<Fun<X>>>, p ->
    if (index % mv.numCols.i == 0) acc.add(mutableListOf(p)) else acc.last().add(p)
    acc
  }.map { Vec(mv.numCols, it) }.let { Mat(mv.numRows, mv.numCols, it) }

  open fun grad(): Map<Var<X>, Fun<X>> = sVars.map { it to Derivative(this, it) }.toMap()

  override fun ln(): Fun<X> = Log(this)
  override fun pow(exp: Fun<X>): Fun<X> = Power(this, exp)
  override fun unaryMinus(): Fun<X> = Negative(this)
  open fun sqrt(): Fun<X> = this pow (One<X>() / (Two<X>()))

  override fun toString(): String = when {
    this is Log -> "ln($logarithmand)"
    this is Negative -> "-($value)"
    this is Power -> "($base) pow ($exponent)"
//    this is Prod && right is Sum -> "$left * ($right)"
//    this is Prod && left is Sum -> "($left) * $right"
    this is Prod -> "($left) * ($right)"
    this is Sum && right is Negative -> "$left - ${right.value}"
    this is Sum -> "$left + $right"
    this is Var -> name
    this is Derivative -> "d($fn) / d($vrb)"
    this is Zero -> "0" //"\uD835\uDFD8" // 𝟘
    this is One -> "1" //"\uD835\uDFD9"  // 𝟙
    this is Two -> "2" //"\uD835\uDFDA"  // 𝟚
    this is E -> "\u2147" // ⅇ
    this is VMagnitude -> "|$value|"
    this is DProd -> "($left) dot ($right)"
    this is Composition -> "($fn) comp $bindings"
    else -> super.toString()
  }

  val opStr: String = when (this) {
    is Log -> "ln"
    is Negative -> "-"
    is Power -> "pow"
    is Prod -> "*"
    is Sum -> "+"
    is Derivative -> "d"
    else -> super.toString()
  }

  fun toGraph(): MutableNode = mutNode(if (this is Var) "$this" else "${hashCode()}").apply {
    when (this@Fun) {
      is Var -> name
      is Negative -> { value.toGraph() - this; add(Label.of("neg")) }
      is Derivative -> { fn.toGraph() - this; mutNode("$this").apply { add(Label.of(vrb.toString())) } - this; add(Label.of("d")) }
      is Power -> { base.toGraph() - this; exponent.toGraph() - this; add(Label.of("pow")) }
      is Prod -> { left.toGraph() - this; right.toGraph() - this; add(Label.of("×")) }
      is Sum -> { left.toGraph() - this; right.toGraph() - this; add(Label.of("+")) }
      is RealNumber -> add(Label.of("$value"))
      is One -> add(Label.of("one"))
      is Zero -> add(Label.of("zero"))
      is Composition -> { bindings.sMap.entries.map { entry -> mutNode(entry.hashCode().toString()).also { compNode -> entry.key.toGraph() - compNode; entry.value.toGraph() - compNode; compNode.add(Label.of("comp")) } }.map { it - this; add(Label.of("bindings")) } }
      else -> TODO(this@Fun.javaClass.toString())
    }
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
class Derivative<X : Fun<X>>(val fn: Fun<X>, val vrb: Var<X>) : Fun<X>(fn, vrb) {
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
    is Composition -> bindings.curried().fold(One()) { acc: Fun<X>, binding ->
      acc * fn.df()(binding) * binding.sMap.entries.first().value.df()
    }
  }
}

class Composition<X : Fun<X>>(val fn: Fun<X>, val bindings: Bindings<X>) : Fun<X>() {
  val evaluate by lazy { apply() }
  override val sVars: Set<Var<X>> by lazy { evaluate.sVars }

//  private fun calculateFixpoint(): Fun<X> {
//    var result = apply()
//    var previous = result.toString()
//    while(true) {
//      result = result.apply()
//      val q1 = result.toString()
//      if(previous == q1) break else previous = q1
//    }
//    return result
//  }

  fun Fun<X>.apply(): Fun<X> =
    bindings.sMap.getOrElse(this) {
      when (this) {
        is Zero -> bindings.zero
        is One -> bindings.one
        is Two -> bindings.two
        is E -> bindings.e
        is Var -> this
        is SConst -> this
        is Prod -> left.apply() * right.apply()
        is Sum -> left.apply() + right.apply()
        is Power -> base.apply() pow exponent.apply()
        is Negative -> -value.apply()
        is Log -> logarithmand.apply().ln()
        is Derivative -> df().apply()
        is DProd -> left(bindings) as Vec<X, D1> dot right(bindings) as Vec<X, D1>
        is VMagnitude -> value(bindings).magnitude()
        is Composition -> fn.apply().apply()
      }
    }
}

data class Bindings<X : Fun<X>>(
    val sMap: Map<Fun<X>, Fun<X>> = mapOf(),
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
  val isReassignmentFree = sMap.values.all { it.sVars.isEmpty() }
  fun determines(fn: Fun<X>) = fn.sVars.all { it in sMap }
  override fun toString() = sMap.toString()
  operator fun contains(v: Var<X>) = v in sMap
  fun curried() = sMap.entries.map { Bindings(mapOf(it.key to it.value), zero, one, two, e) }
}

class DProd<X: Fun<X>>(val left: VFun<X, *>, val right: VFun<X, *>): Fun<X>(left.sVars + right.sVars)//, left.vVars + right.vVars)

class VMagnitude<X: Fun<X>>(val value: VFun<X, *>): Fun<X>(value.sVars)//, value.vVars)

interface Variable { val name: String }

class Var<X : Fun<X>>(override val name: String = "") : Variable, Fun<X>() {
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

  override fun <E : D1> times(multiplicand: VFun<DoubleReal, E>) =
    when (multiplicand) {
      is Vec -> Vec(multiplicand.length, multiplicand.contents.map { this * it })
      else -> super.times(multiplicand)
    }

  override fun <R : D1, C: D1> times(multiplicand: MFun<DoubleReal, R, C>) =
    when (multiplicand) {
      is Mat -> Mat(multiplicand.numRows, multiplicand.numCols, multiplicand.rows.map { this * it } as List<Vec<DoubleReal, C>>)
      else -> super.times(multiplicand)
    }
}

/**
 * Numerical context.
 */

sealed class Protocol<X : RealNumber<X>> {
  class IndVar<X: Fun<X>> constructor(val fn: Fun<X>)

  class Differential<X: Fun<X>>(private val fx: Fun<X>) {
    // TODO: make sure this notation works for arbitrary nested functions using the Chain rule
    infix operator fun div(arg: Differential<X>) = fx.d(arg.fx.sVars.first())
  }

  fun <X: Fun<X>> d(fn: Fun<X>) = Differential(fn)
  abstract fun wrap(default: Number): X

  operator fun Number.times(multiplicand: Fun<X>) = multiplicand * wrap(this)
  operator fun Fun<X>.times(multiplicand: Number) = wrap(multiplicand) * this

  operator fun Number.plus(addend: Fun<X>) = addend + wrap(this)
  operator fun Fun<X>.plus(addend: Number) = wrap(addend) + this

  operator fun Number.minus(subtrahend: Fun<X>) = -subtrahend + wrap(this)
  operator fun Fun<X>.minus(subtrahend: Number) = -wrap(subtrahend) + this

  fun Number.pow(exp: Fun<X>) = wrap(this) pow exp
  infix fun Fun<X>.pow(exp: Number) = this pow wrap(exp)
}

object DoublePrecision : Protocol<DoubleReal>() {
  override fun wrap(default: Number): DoubleReal = DoubleReal(default.toDouble())

  val one = wrap(1.0)
  val zero = wrap(0.0)
  val two = wrap(2.0)
  val e = wrap(E)

  fun vrb(name: String) = Var<DoubleReal>(name)

  @JvmName("ValBnd") operator fun Fun<DoubleReal>.invoke(vararg pairs: Pair<Var<DoubleReal>, Number>) =
    this(Bindings(pairs.map { (it.first to wrap(it.second)) }.toMap(), zero, one, two, e))
  @JvmName("FunBnd") operator fun Fun<DoubleReal>.invoke(vararg pairs: Pair<Fun<DoubleReal>, Fun<DoubleReal>>) =
    this(Bindings(pairs.map { (it.first to it.second) }.toMap(), zero, one, two, e))

  operator fun <Y : D1> VFun<DoubleReal, Y>.invoke(vararg sPairs: Pair<Var<DoubleReal>, Number>) =
    this(Bindings(sPairs.map { (it.first to wrap(it.second)) }.toMap(), zero, one, two, e))

  operator fun <Rows : D1, Cols: D1> MFun<DoubleReal, Rows, Cols>.invoke(vararg sPairs: Pair<Var<DoubleReal>, Number>) =
    this(Bindings(sPairs.map { (it.first to wrap(it.second)) }.toMap(), zero, one, two, e))

  fun Fun<DoubleReal>.asDouble() = (this as DoubleReal).value

  val x = vrb("X")
  val y = vrb("Y")
  val z = vrb("Z")

  fun Vec(d0: Double) = Vec(DoubleReal(d0))
  fun Vec(d0: Double, d1: Double) = Vec(DoubleReal(d0), DoubleReal(d1))
  fun Vec(d0: Double, d1: Double, d2: Double) = Vec(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double) = Vec(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2), DoubleReal(d3))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double) = Vec(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2), DoubleReal(d3), DoubleReal(d4))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double) = Vec(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2), DoubleReal(d3), DoubleReal(d4), DoubleReal(d5))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double, d6: Double) = Vec(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2), DoubleReal(d3), DoubleReal(d4), DoubleReal(d5), DoubleReal(d6))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double, d6: Double, d7: Double) = Vec(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2), DoubleReal(d3), DoubleReal(d4), DoubleReal(d5), DoubleReal(d6), DoubleReal(d7))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double, d6: Double, d7: Double, d8: Double) = Vec(DoubleReal(d0), DoubleReal(d1), DoubleReal(d2), DoubleReal(d3), DoubleReal(d4), DoubleReal(d5), DoubleReal(d6), DoubleReal(d7), DoubleReal(d8))

  fun Mat1x1(d0: Double) = Mat(D1, D1, Vec(d0))
  fun Mat1x2(d0: Double, d1: Double) = Mat(D1, D2, Vec(d0, d1))
  fun Mat1x3(d0: Double, d1: Double, d2: Double) = Mat(D1, D3, Vec(d0, d1, d2))
  fun Mat2x1(d0: Double, d1: Double) = Mat(D2, D1, Vec(d0), Vec(d1))
  fun Mat2x2(d0: Double, d1: Double, d2: Double, d3: Double) = Mat(D2, D2, Vec(d0, d1), Vec(d2, d3))
  fun Mat2x3(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double) = Mat(D2, D3, Vec(d0, d1, d2), Vec(d3, d4, d5))
  fun Mat3x1(d0: Double, d1: Double, d2: Double) = Mat(D3, D1, Vec(d0), Vec(d1), Vec(d2))
  fun Mat3x2(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double) = Mat(D3, D2, Vec(d0, d1), Vec(d2, d3), Vec(d4, d5))
  fun Mat3x3(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double, d6: Double, d7: Double, d8: Double) = Mat(D3, D3, Vec(d0, d1, d2), Vec(d3, d4, d5), Vec(d6, d7, d8))

  fun Var2() = Vec(Var<DoubleReal>(), Var())
  fun Var3() = Vec(Var<DoubleReal>(), Var(), Var())

  fun Var2x1() = Mat(D2, D1, Vec(Var<DoubleReal>()), Vec(Var()))
  fun Var2x2() = Mat(D2, D2, Vec(Var<DoubleReal>(), Var()), Vec(Var(), Var()))
  fun Var2x3() = Mat(D2, D3, Vec(Var<DoubleReal>(), Var(), Var()), Vec(Var(), Var(), Var()))
  fun Var3x1() = Mat(D3, D1, Vec(Var<DoubleReal>()), Vec(Var()), Vec(Var()))
  fun Var3x2() = Mat(D3, D2, Vec(Var<DoubleReal>(), Var()), Vec(Var(), Var()), Vec(Var(), Var()))
  fun Var3x3() = Mat(D3, D3, Vec(Var<DoubleReal>(), Var(), Var()), Vec(Var(), Var(), Var()), Vec(Var(), Var(), Var()))
}