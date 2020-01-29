@file:Suppress("FunctionName", "LocalVariableName", "unused", "UNUSED_VARIABLE")

package edu.umontreal.kotlingrad.experimental

import guru.nidi.graphviz.*
import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Renderer
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.MutableNode
import java.io.File
import java.lang.NumberFormatException
import kotlin.math.*

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

  infix fun pow(exponent: X): X
  fun ln(): X
}

interface Fun<X: SFun<X>> {
  val bindings: Bindings<X>
  fun opCode() = javaClass.simpleName
  fun toGraph(): MutableNode = mutNode(toString()).apply {
    when (this@Fun) {
      is BiFun<*> -> { (left.toGraph() - this).add(Color.BLUE); (right.toGraph() - this).add(Color.RED); add(Label.of(opCode())) }
      is UnFun<*> -> { input.toGraph() - this; add(Label.of(opCode())) }
      is SFun<*> -> (this@Fun as SFun).toGraph()
      is VFun<*, *> -> (this@Fun as VFun<*, *>).toGraph()
      else -> TODO(this@Fun.javaClass.toString())
    }
  }
}

interface Variable<X: SFun<X>>: Fun<X> { val name: String }

// Supports arbitrary subgraph reassignment but usually just holds variable-to-value bindings
@Suppress("UNCHECKED_CAST")
data class Bindings<X: SFun<X>>(val fMap: Map<Fun<X>, Fun<X>> = mapOf()): Map<Fun<X>, Fun<X>> by fMap {
  constructor(inputs: List<Bindings<X>>): this(
    mapOf(*inputs.flatMap { it.fMap.entries }.map { Pair(it.key, it.value) }.toTypedArray())
  )
  constructor(vararg bindings: Bindings<X>): this(bindings.toList())
  constructor(vararg funs: Fun<X>): this(funs.map { it.bindings })

  // Scalar, vector, and matrix "views" on untyped collection map
  val sMap = filterInstancesOf<SFun<X>>()
  val vMap = filterInstancesOf<VFun<X, *>>()
  val mMap = filterInstancesOf<MFun<X, *, *>>()

  private inline fun <reified T: Fun<X>> filterInstancesOf(): Map<T, T> =
    mapOf(*fMap.keys.filterIsInstance(T::class.java).map { it to fMap[it]!! as T }.toTypedArray())

  // Merges two variable bindings
  // TODO: Add support for change of variables, i.e. x = y, y = 2z, z = x + y...
  operator fun plus(other: Bindings<X>) = Bindings(fMap + other.fMap)

  // Scalar, vector, and matrix variables
  val sVars: Set<Var<X>> = sMap.keys.filterIsInstance<Var<X>>().toSet()
  val vVars: Set<VVar<X, *>> = vMap.keys.filterIsInstance<VVar<X, *>>().toSet()
  val mVars: Set<MVar<X, *, *>> = mMap.keys.filterIsInstance<MVar<X, *, *>>().toSet()

  val isReassignmentFree = fMap.values.none { it is Variable<*> }
  fun fullyDetermines(fn: SFun<X>) = fn.bindings.sVars.all { it in this }
  override fun toString() = fMap.toString()
  operator fun contains(v: Variable<X>) = v in fMap
  fun curried() = fMap.entries.map { Bindings(mapOf(it.key to it.value)) }

  operator fun get(fn: SFun<X>): SFun<X>? = sMap[fn]
  operator fun <L: D1> get(fn: VFun<X, L>): VFun<X, L>? = vMap[fn] as VFun<X, L>?
  operator fun <R: D1, C: D1> get(fn: MFun<X, R, C>): MFun<X, R, C>? = mMap[fn] as MFun<X, R, C>?

  override fun equals(other: Any?) = other is Bindings<*> && fMap == other.fMap
  override fun hashCode() = fMap.hashCode()
}

/**
 * Scalar function.
 */

sealed class SFun<X: SFun<X>>(override val bindings: Bindings<X>): Fun<X>, Field<SFun<X>>, (Bindings<X>) -> SFun<X> {
  constructor(vararg funs: Fun<X>) : this(Bindings(*funs))

  protected open val ZERO: SFun<X> by lazy { Zero() }
  protected open val ONE: SFun<X> by lazy { One() }
  protected open val TWO: SFun<X> by lazy { Two() }
  protected open val E: SFun<X> by lazy { E<X>() }

  override operator fun plus(addend: SFun<X>): SFun<X> = Sum(this, addend)
  override operator fun times(multiplicand: SFun<X>): SFun<X> = Prod(this, multiplicand)
  override operator fun div(divisor: SFun<X>): SFun<X> = this * divisor.pow(-ONE)
  open operator fun <E : D1> times(multiplicand: VFun<X, E>): VFun<X, E> = SVProd(this, multiplicand)
  open operator fun <R : D1, C : D1> times(multiplicand: MFun<X, R, C>): MFun<X, R, C> = SMProd(this, multiplicand)

  override operator fun invoke(bnds: Bindings<X>): SFun<X> =
      Composition(this, bnds).run { if (bnds.isReassignmentFree) evaluate else this }

  @JvmName("sFunReassign")
  operator fun invoke(vararg ps: Pair<SFun<X>, SFun<X>>): SFun<X> = invoke(Bindings(mapOf(*ps)))

  @JvmName("vFunReassign")
  operator fun <L: D1> invoke(pair: Pair<VFun<X, L>, VFun<X, L>>): SFun<X> =
    invoke(*pair.first().contents.zip(pair.second().contents).toTypedArray())

  @JvmName("mFunReassign")
  operator fun <R: D1, C: D1> invoke(pair: Pair<MFun<X, R, C>, MFun<X, R, C>>): SFun<X> =
    invoke(*pair.first().flatContents.zip(pair.second().flatContents).toTypedArray())

  operator fun invoke(vararg fns: SFun<X>): SFun<X> = invoke(*bindings.sVars.zip(fns).toTypedArray())

  operator fun invoke(): SFun<X> = invoke(Bindings())

  open fun d(v1: Var<X>): SFun<X> = Derivative(this, v1)
  open fun d(v1: Var<X>, v2: Var<X>): Vec<X, D2> = Vec(d(v1), d(v2))
  open fun d(v1: Var<X>, v2: Var<X>, v3: Var<X>): Vec<X, D3> = Vec(d(v1), d(v2), d(v3))
  open fun d(vararg vars: Var<X>): Map<Var<X>, SFun<X>> = vars.map { it to d(it) }.toMap()

  open fun sin(): SFun<X> = Sine(this)
  open fun cos(): SFun<X> = Cosine(this)
  open fun tan(): SFun<X> = Tangent(this)
  fun exp(): SFun<X> = Power(E, this)

  open fun <L: D1> d(vVar: VVar<X, L>): VFun<X, L> = Gradient(this, vVar)
  open fun <R: D1, C: D1> d(mVar: MVar<X, R, C>): MFun<X, R, C> = MGradient(this, mVar)

  open fun <R : D1, C : D1> d(mv: Mat<X, R, C>) =
    d(*mv.bindings.sVars.toTypedArray()).values
      .foldIndexed(mutableListOf()) { index, acc: MutableList<MutableList<SFun<X>>>, p ->
        if (index % mv.numCols == 0) acc.add(mutableListOf(p)) else acc.last().add(p)
        acc
      }.map { Vec<X, C>(it) }.let { Mat<X, R, C>(it) }

  open fun grad(): Map<Var<X>, SFun<X>> = bindings.sVars.map { it to Derivative(this, it) }.toMap()

  override fun ln(): SFun<X> = Log(this)
  override fun pow(exponent: SFun<X>): SFun<X> = Power(this, exponent)
  override fun unaryMinus(): SFun<X> = Negative(this)
  open fun sqrt(): SFun<X> = this pow (ONE / TWO)

  override fun toString(): String = when (this) {
    is Log -> "ln($left)"
    is Negative -> "- ($input)"
    is Power -> "($left).pow($right)"
    is Var -> name
    is Derivative -> "d($fn) / d($vrb)"
    is Special -> javaClass.simpleName
    is BiFun<*> -> "($left) ${opCode()} ($right)"
    is UnFun<*> -> "${opCode()}($input)"
    is VMagnitude -> "|$value|"
    is Composition -> "($fn)$inputs"
    else -> super.toString()
  }

  override fun opCode() = when (this) {
    is Log -> "ln"
    is Negative -> "-"
    is Power -> "pow"
    is Prod -> "*"
    is Sum -> "+"
    is Derivative -> "d"
    is Sine -> "sin"
    is Cosine -> "cos"
    is Tangent -> "tan"
    is DProd -> "dot"
    else -> super.toString()
  }

  override fun toGraph(): MutableNode = mutNode(if (this is Var) "$this" else "${hashCode()}").apply {
    when (this@SFun) {
      is Var -> name
      is Derivative -> { fn.toGraph() - this; mutNode("$this").apply { add(Label.of(vrb.toString())) } - this; add(Label.of("d")) }
      is BiFun<*> -> { (left.toGraph() - this).add(Color.BLUE); (right.toGraph() - this).add(Color.RED); add(Label.of(opCode())) }
      is UnFun<*> -> { input.toGraph() - this; add(Label.of(opCode())) }
      is RealNumber<*, *> -> add(Label.of(value.toString().take(5)))
      is Special -> add(Label.of(this@SFun.toString()))
      is Composition -> { bindings.sMap.entries.map { entry -> mutNode(entry.hashCode().toString()).also { compNode -> entry.key.toGraph() - compNode; entry.value.toGraph() - compNode; compNode.add(Label.of("comp")) } }.map { it - this; add(Label.of("bindings")) } }
      else -> TODO(this@SFun.javaClass.toString())
    }
  }
}

/**
 * Symbolic operators.
 */

interface BiFun<X: SFun<X>>: Fun<X> {
  val left: Fun<X>
  val right: Fun<X>
}

interface UnFun<X: SFun<X>>: Fun<X> {
  val input: Fun<X>
}

class Sine<X: SFun<X>>(override val input: SFun<X>): SFun<X>(input), UnFun<X>
class Cosine<X: SFun<X>>(override val input: SFun<X>): SFun<X>(input), UnFun<X>
class Tangent<X: SFun<X>>(override val input: SFun<X>): SFun<X>(input), UnFun<X>
class Negative<X : SFun<X>>(override val input: SFun<X>) : SFun<X>(input), UnFun<X>
class Sum<X : SFun<X>>(override val left: SFun<X>, override val right: SFun<X>): SFun<X>(left, right), BiFun<X>
class Prod<X : SFun<X>>(override val left: SFun<X>, override val right: SFun<X>): SFun<X>(left, right), BiFun<X>
class Power<X : SFun<X>>(override val left: SFun<X>, override val right: SFun<X>) : SFun<X>(left, right), BiFun<X>
class Log<X : SFun<X>>(override val left: SFun<X>, override val right: SFun<X> = E()) : SFun<X>(left, right), BiFun<X>

class Derivative<X : SFun<X>>(val fn: SFun<X>, val vrb: Var<X>) : SFun<X>(fn, vrb) {
  fun SFun<X>.df(): SFun<X> = when (this@df) {
    is Var -> if (this == vrb) ONE else ZERO
    is SConst -> ZERO
    is Sum -> left.df() + right.df()
    is Prod -> left.df() * right + left * right.df()
    is Power ->
      if (right.bindings.sVars.isEmpty()) right * left.pow(right - ONE) * left.df()
      else this * (left.df() * right / left + right.df() * left.ln())
    is Negative -> -input.df()
    is Log -> (left pow -ONE) * left.df()
    is Sine -> input.cos() * input.df()
    is Cosine -> -input.sin() * input.df()
    is Tangent -> (input.cos() pow -TWO) * input.df()
    is Derivative -> fn.df()
    is DProd -> this().df()
    is VMagnitude -> this().df()
    is Composition -> evaluate.df()
  }
}

// TODO: Unit test this data structure
class Composition<X : SFun<X>>(val fn: SFun<X>, val inputs: Bindings<X>) : SFun<X>(Bindings(fn.bindings, inputs)) {
  val evaluate: SFun<X> by lazy { bind(inputs) }
  override val bindings: Bindings<X> by lazy { evaluate.bindings }

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

  @Suppress("UNCHECKED_CAST")
  fun SFun<X>.bind(bindings: Bindings<X>): SFun<X> =
    bindings[this@bind] ?: when (this@bind) {
      is Var -> this@bind
      is SConst -> this@bind
      is Prod -> left.bind(bindings) * right.bind(bindings)
      is Sum -> left.bind(bindings) + right.bind(bindings)
      is Power -> left.bind(bindings) pow right.bind(bindings)
      is Negative -> -input.bind(bindings)
      is Sine -> input.bind(bindings).sin()
      is Cosine -> input.bind(bindings).cos()
      is Tangent -> input.bind(bindings).tan()
      is Log -> left.bind(bindings).ln()
      is Derivative -> df().bind(bindings)
      is DProd -> left(bindings) as Vec<X, D1> dot right(bindings) as Vec<X, D1>
      is VMagnitude -> value(bindings).magnitude()
      is Composition -> fn.bind(bindings + inputs)
    }
}

class DProd<X: SFun<X>>(val left: VFun<X, *>, val right: VFun<X, *>): SFun<X>(left, right)

class VMagnitude<X: SFun<X>>(val value: VFun<X, *>): SFun<X>(value)

class Var<X : SFun<X>>(override val name: String = "") : Variable<X>, SFun<X>() {
  override val bindings: Bindings<X> = Bindings(mapOf(this to this))
  override fun equals(other: Any?) = other is Var<*> &&  name == other.name
}

open class SConst<X : SFun<X>> : SFun<X>() {
  open val doubleValue: Double = when(this) {
    is Zero -> 0.0
    is One -> 1.0
    is Two -> 2.0
    is E -> kotlin.math.E
    else -> Double.NaN
  }
}
sealed class Special<X: SFun<X>> : SConst<X>() {
  override fun toString() = javaClass.simpleName
  override fun equals(other: Any?) =
    if (this === other) true else javaClass == other?.javaClass

  override fun hashCode(): Int = when(this) {
    is Zero<*> -> 0
    is One<*> -> 1
    is Two<*> -> 2
    is E<*> -> 3
  }
}
class Zero<X: SFun<X>> : Special<X>()
class One<X: SFun<X>> : Special<X>()
class Two<X: SFun<X>> : Special<X>()
class E<X: SFun<X>> : Special<X>()

abstract class RealNumber<X: SFun<X>, Y>(open val value: Y): SConst<X>() {
  override fun toString() = value.toString()
  open fun wrap(number: Any): SFun<X> = when(number) {
    is SFun<*> -> number as SFun<X>
    else -> throw NumberFormatException(number.toString())
  }

  override val doubleValue: Double by lazy {
    when (this) {
      is BDReal -> value.toDouble()
      is DReal -> value
      else -> super.doubleValue
    }
  }

  override val ZERO by lazy { wrap(0) }
  override val ONE by lazy { wrap(1) }
  override val TWO by lazy { wrap(2) }
  override val E by lazy { wrap(Math.E) }

  override fun <E : D1> times(multiplicand: VFun<X, E>): VFun<X, E> =
    when (multiplicand) {
      is Vec -> Vec(multiplicand.contents.map { this * it })
      else -> super.times(multiplicand)
    }

  @Suppress("UNCHECKED_CAST")
  override fun <R : D1, C: D1> times(multiplicand: MFun<X, R, C>) =
    when (multiplicand) {
      is Mat -> Mat(multiplicand.rows.map { this * it } as List<Vec<X, C>>)
      else -> super.times(multiplicand)
    }

  override fun sin() = wrap(sin(doubleValue))
  override fun cos() = wrap(cos(doubleValue))
  override fun tan() = wrap(tan(doubleValue))
  override fun sqrt() = wrap(sqrt(doubleValue))
  override fun unaryMinus() = wrap(-doubleValue)
  override fun ln() = wrap(ln(doubleValue))

  /**
   * Constant propagation.
   */

  override fun plus(addend: SFun<X>) = when (addend) {
    is RealNumber<X, *> -> wrap(doubleValue + addend.doubleValue)
    else -> super.plus(addend)
  }

  override fun times(multiplicand: SFun<X>) = when (multiplicand) {
    is RealNumber<X, *> -> wrap(doubleValue * multiplicand.doubleValue)
    else -> super.times(multiplicand)
  }

  override fun pow(exponent: SFun<X>) = when (exponent) {
    is RealNumber<X, *> -> wrap(doubleValue.pow(exponent.doubleValue))
    else -> super.pow(exponent)
  }
}

open class DReal(override val value: Double) : RealNumber<DReal, Double>(value) {
  override fun wrap(number: Any) = when(number) {
    is Number -> DReal(number.toDouble())
    is SConst<*> -> DReal(number.doubleValue)
    is SFun<*> -> super.wrap(number)
    else -> DReal(number.toString().toDouble())
  }

//  fun Double.clipped() = when {
//    isNaN() -> throw NumberFormatException("Is NaN")
//    3 < log10(absoluteValue).absoluteValue -> sign * 10.0.pow(log10(absoluteValue))
//    else -> this
//  }

  companion object: DReal(0.0)
}

/**
 * Numerical context. Converts numerical types from host language to eDSL.
 */

sealed class Protocol<X : SFun<X>>(val prototype: RealNumber<X, *>) {
  val x = Var<X>("x")
  val y = Var<X>("y")
  val z = Var<X>("z")

  val variables = listOf(x, y, z)

  val zero = Zero<X>()
  val one = One<X>()
  val two = Two<X>()
  val e = E<X>()

  val constants = mapOf(zero to 0, one to 1, two to 2, e to E).wrap()

  private fun <T> Map<T, Any>.wrap() = map { it.key to wrap(it.value) }.toTypedArray()

  private fun <T: Map<Var<X>, Number>> T.bind() = Bindings(constants.toMap() + this@bind.wrap())

  fun wrap(number: Any): SFun<X> = prototype.wrap(number)
  fun <X: RealNumber<X, Y>, Y: Number> SFun<X>.unwrap() = (this as X).value
  fun <X: RealNumber<X, Y>, Y: Number> SFun<X>.toDouble() = unwrap().toDouble()

  fun <T: SFun<T>> sin(angle: SFun<T>) = angle.sin()
  fun <T: SFun<T>> cos(angle: SFun<T>) = angle.cos()
  fun <T: SFun<T>> tan(angle: SFun<T>) = angle.tan()
  fun <T: SFun<T>> exp(exponent: SFun<T>) = exponent.exp()
  fun <T: SFun<T>> sqrt(radicand: SFun<T>) = radicand.sqrt()

  class IndVar<X: SFun<X>> constructor(val fn: SFun<X>)

  class Differential<X: SFun<X>>(private val fx: SFun<X>) {
    // TODO: ensure correctness for arbitrary nested functions using the Chain rule
    infix operator fun div(arg: Differential<X>) = fx.d(arg.fx.bindings.sVars.first())
  }

  operator fun Number.invoke(n: Number) = this

  operator fun SFun<X>.invoke(number: Number): SFun<X> = invoke(wrap(number))

  operator fun SFun<X>.invoke(vararg pairs: Pair<Var<X>, Number>) = invoke(pairs.toMap().bind())

  operator fun <E : D1> VFun<X, E>.invoke(vararg pairs: Pair<Var<X>, Number>) = invoke(pairs.toMap().bind())

  operator fun <R : D1, C: D1> MFun<X, R, C>.invoke(vararg pairs: Pair<Var<X>, Number>) = invoke(pairs.toMap().bind())

  fun d(fn: SFun<X>) = Differential(fn)

  operator fun Number.times(multiplicand: SFun<X>) = wrap(this) * multiplicand
  operator fun SFun<X>.times(multiplicand: Number) = this * wrap(multiplicand)

  operator fun Number.div(divisor: SFun<X>) = wrap(this) / divisor
  operator fun SFun<X>.div(divisor: Number) = this / wrap(divisor)

  operator fun Number.plus(addend: SFun<X>) = wrap(this) + addend
  operator fun SFun<X>.plus(addend: Number) = this + wrap(addend)

  operator fun Number.minus(subtrahend: SFun<X>) = wrap(this) - subtrahend
  operator fun SFun<X>.minus(subtrahend: Number) = this - wrap(subtrahend)

  fun Number.pow(exp: SFun<X>) = wrap(this) pow exp
  infix fun SFun<X>.pow(exp: Number) = this pow wrap(exp)
  @JvmName("prefixPowNum") fun pow(base: SFun<X>, exp: Number) = base pow wrap(exp)
  @JvmName("prefixPowFun") fun pow(base: Number, exp: SFun<X>) = wrap(base) pow exp

  fun <Y: Number> Vec(d0: Y) = Vec(wrap(d0))
  fun <Y: Number> Vec(d0: Y, d1: Y) = Vec(wrap(d0), wrap(d1))
  fun <Y: Number> Vec(d0: Y, d1: Y, d2: Y) = Vec(wrap(d0), wrap(d1), wrap(d2))
  fun <Y: Number> Vec(d0: Y, d1: Y, d2: Y, d3: Y) = Vec(wrap(d0), wrap(d1), wrap(d2), wrap(d3))
  fun <Y: Number> Vec(d0: Y, d1: Y, d2: Y, d3: Y, d4: Y) = Vec(wrap(d0), wrap(d1), wrap(d2), wrap(d3), wrap(d4))
  fun <Y: Number> Vec(d0: Y, d1: Y, d2: Y, d3: Y, d4: Y, d5: Y) = Vec(wrap(d0), wrap(d1), wrap(d2), wrap(d3), wrap(d4), wrap(d5))
  fun <Y: Number> Vec(d0: Y, d1: Y, d2: Y, d3: Y, d4: Y, d5: Y, d6: Y) = Vec(wrap(d0), wrap(d1), wrap(d2), wrap(d3), wrap(d4), wrap(d5), wrap(d6))
  fun <Y: Number> Vec(d0: Y, d1: Y, d2: Y, d3: Y, d4: Y, d5: Y, d6: Y, d7: Y) = Vec(wrap(d0), wrap(d1), wrap(d2), wrap(d3), wrap(d4), wrap(d5), wrap(d6), wrap(d7))
  fun <Y: Number> Vec(d0: Y, d1: Y, d2: Y, d3: Y, d4: Y, d5: Y, d6: Y, d7: Y, d8: Y) = Vec(wrap(d0), wrap(d1), wrap(d2), wrap(d3), wrap(d4), wrap(d5), wrap(d6), wrap(d7), wrap(d8))

  fun <Y: Number> Mat1x1(d0: Y) = Mat<X, D1, D1>(Vec(d0))
  fun <Y: Number> Mat1x2(d0: Y, d1: Y) = Mat<X, D1, D2>(Vec(d0, d1))
  fun <Y: Number> Mat1x3(d0: Y, d1: Y, d2: Y) = Mat<X, D1, D3>(Vec(d0, d1, d2))
  fun <Y: Number> Mat2x1(d0: Y, d1: Y) = Mat<X, D2, D1>(Vec(d0), Vec(d1))
  fun <Y: Number> Mat2x2(d0: Y, d1: Y, d2: Y, d3: Y) = Mat<X, D2, D2>(Vec(d0, d1), Vec(d2, d3))
  fun <Y: Number> Mat2x3(d0: Y, d1: Y, d2: Y, d3: Y, d4: Y, d5: Y) = Mat<X, D2, D3>(Vec(d0, d1, d2), Vec(d3, d4, d5))
  fun <Y: Number> Mat3x1(d0: Y, d1: Y, d2: Y) = Mat<X, D3, D1>(listOf(Vec(d0), Vec(d1), Vec(d2)) )
  fun <Y: Number> Mat3x2(d0: Y, d1: Y, d2: Y, d3: Y, d4: Y, d5: Y) = Mat<X, D3, D2>(Vec(d0, d1), Vec(d2, d3), Vec(d4, d5))
  fun <Y: Number> Mat3x3(d0: Y, d1: Y, d2: Y, d3: Y, d4: Y, d5: Y, d6: Y, d7: Y, d8: Y) = Mat<X, D3, D3>(Vec(d0, d1, d2), Vec(d3, d4, d5), Vec(d6, d7, d8))

  inline fun <R: D1, C: D1, Y: Any> Mat(r: Nat<R>, c: Nat<C>, gen: (Int, Int) -> Y): Mat<X, R, C> =
    Mat(List(r.i) { row -> Vec(List(c.i) { col -> wrap(gen(row, col)) }) })
  inline fun <E: D1, Y: Any> Vec(e: Nat<E>, gen: (Int) -> Y): Vec<X, E> =
    Vec(List(e.i) { wrap(gen(it)) })

  fun Var(name: String) = Var<X>(name)
  fun Var2(name: String) = VVar<X, D2>(name, D2)
  fun Var3(name: String) = VVar<X, D3>(name, D3)

  fun Var2x1(name: String) = MVar<X, D2, D1>(name, D2, D1)
  fun Var2x2(name: String) = MVar<X, D2, D2>(name, D2, D2)
  fun Var2x3(name: String) = MVar<X, D2, D3>(name, D2, D3)
  fun Var3x1(name: String) = MVar<X, D3, D1>(name, D3, D1)
  fun Var3x2(name: String) = MVar<X, D3, D2>(name, D3, D2)
  fun Var3x3(name: String) = MVar<X, D3, D3>(name, D3, D3)

  val DARKMODE = false
  val THICKNESS = 2

  fun Fun<*>.show() = renderAsSVG { toGraph() }.show()
  fun SFun<*>.renderAsSVG() = renderAsSVG { toGraph() }

  inline fun renderAsSVG(crossinline op: () -> MutableNode) =
    graph(directed = true) {
      val color = if (DARKMODE) Color.WHITE else Color.BLACK

      edge[color, Arrow.NORMAL, Style.lineWidth(THICKNESS)]

      graph[Rank.dir(Rank.RankDir.LEFT_TO_RIGHT), Color.TRANSPARENT.background()]

      node[color, color.font(), Font.config("Helvetica", 20),
        Style.lineWidth(THICKNESS)]

      op()
    }.toGraphviz().render(Format.SVG)

  fun Renderer.saveToFile(filename: String) = toFile(File(filename))

  fun Renderer.show() = toFile(File.createTempFile("temp", ".svg")).show()
  fun File.show() = ProcessBuilder("x-www-browser", path).start()
}

object DoublePrecision : Protocol<DReal>(DReal)
object BigDecimalPrecision : Protocol<BDReal>(BDReal)