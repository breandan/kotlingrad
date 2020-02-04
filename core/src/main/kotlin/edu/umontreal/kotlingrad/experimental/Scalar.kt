@file:Suppress("FunctionName", "LocalVariableName", "unused", "UNUSED_VARIABLE")

package edu.umontreal.kotlingrad.experimental

import edu.umontreal.kotlingrad.functions.Var
import guru.nidi.graphviz.*
import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.attribute.Color.*
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Renderer
import guru.nidi.graphviz.model.Factory
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.MutableNode
import java.io.File
import kotlin.NumberFormatException
import kotlin.math.*

/**
 * Algebraic primitives.
 */

interface Group<X: Group<X>> {
  operator fun unaryMinus(): X
  operator fun plus(addend: X): X
  operator fun minus(subtrahend: X): X = this + -subtrahend
}

interface Field<X: Field<X>>: Group<X> {
  operator fun div(divisor: X): X
  operator fun times(multiplicand: X): X

  infix fun pow(exponent: X): X
  fun ln(): X
}

interface Fun<X: SFun<X>>: (Bindings<X>) -> Fun<X> {
  val bindings: Bindings<X>
  fun opCode() = javaClass.simpleName

  override operator fun invoke(newBindings: Bindings<X>): Fun<X>

//  operator fun invoke(sFun: SFun<X>): Fun<X> = invoke(bindings.zip(fns).bind() + constants) as bindings.z
  operator fun <A: Fun<X>> A.invoke(vararg fns: Fun<X>): A = invoke(bindings.zip(fns.toList())) as A

  fun toGraph(): MutableNode = mutNode(toString()).apply {
    when (this@Fun) {
      is BiFun<*> -> { (left.toGraph() - this).add(BLUE); (right.toGraph() - this).add(RED); add(Label.of(opCode())) }
      is UnFun<*> -> { input.toGraph() - this; add(Label.of(opCode())) }
      is SFun<*> -> (this@Fun as SFun).toGraph()
      is VFun<*, *> -> (this@Fun as VFun<*, *>).toGraph()
      is MFun<*, *, *> -> (this@Fun as MFun<*, *, *>).toGraph()
      else -> TODO(this@Fun.javaClass.toString())
    }
  }
}

interface Variable<X: SFun<X>>: Fun<X> {
  val name: String
}

interface Constant<X: SFun<X>>: Fun<X>

// Supports arbitrary subgraph reassignment but usually just holds variable-to-value bindings
@Suppress("UNCHECKED_CAST")
data class Bindings<X: SFun<X>>(val fMap: Map<Fun<X>, Fun<X>> = mapOf()) {
  constructor(inputs: List<Bindings<X>>): this(inputs.map { it.fMap }.fold(mapOf()) { acc, fMap -> fMap + acc })
  constructor(vararg bindings: Bindings<X>): this(bindings.toList())
  constructor(vararg funs: Fun<X>): this(funs.map { it.bindings })
  constructor(vararg pairs: Pair<Fun<X>, Fun<X>>): this(pairs.toMap())

  companion object {
    var throws = false
  }

  fun zip(fns: List<Fun<X>>): Bindings<X> =
    sVars.filter { it.name == "mapInput" }.zip(fns.filterIsInstance<SFun<X>>()).let { mapped ->
      mapped + sVars.filter { it.name != "mapInput" }.zip(fns.toList() - mapped.map { it.second }) +
        vVars.zip(fns.filterIsInstance<VFun<X, *>>()) +
        mVars.zip(fns.filterIsInstance<MFun<X, *, *>>())
    }.let { Bindings(*it.toTypedArray()) }

  // Scalar, vector, and matrix "views" on untyped function map
  val sFunMap = filterInstancesOf<SFun<X>>()
  val vFunMap = filterInstancesOf<VFun<X, *>>()
  val mFunMap = filterInstancesOf<MFun<X, *, *>>()

  val mVarMap = mFunMap.filterKeys { it is MVar<X, *, *> } as Map<MVar<X, *, *>, MFun<X, *, *>>
  val vVarMap = mVarMap.filterValues { it is Mat<X, *, *> }
    .flatMap { it.key.vVars.zip((it.value as Mat<X, *, *>).rows) }.toMap() +
  vFunMap.filterKeys { it is VVar<X, *> } as Map<VVar<X, *>, VFun<X, *>>
  val sVarMap = (vVarMap.filterValues { it is Vec<X, *> }
    .flatMap { it.key.sVars.zip((it.value as Vec<X, *>).contents) } .toMap() +
    sFunMap.filterKeys { it is SVar<X> }) as Map<SVar<X>, SFun<X>>

  val allVarMap = mVarMap + vVarMap + sVarMap

  init {
    if(mVarMap.values.any { it is MVar } && throws) throw Exception("$mVarMap")
  }

  private inline fun <reified T> filterInstancesOf(): Map<T, T> = fMap.filterKeys { it is T } as Map<T, T>

  // Merges two variable bindings
  // TODO: Add support for change of variables, i.e. x = y, y = 2z, z = x + y...
  operator fun plus(other: Bindings<X>) =
    Bindings(
      fMap.filterValues { containsFreeVariable(it) } +
      other.fMap.filterValues { containsFreeVariable(it) } +
      fMap.filterValues { !containsFreeVariable(it) } +
      other.fMap.filterValues { !containsFreeVariable(it) }
    )

  // Scalar, vector, and matrix variables
  val sVars: Set<SVar<X>> = sVarMap.keys
  val vVars: Set<VVar<X, *>> = vVarMap.keys
  val mVars: Set<MVar<X, *, *>> = mVarMap.keys
  val allVars: Set<Variable<X>> = sVars + vVars + mVars

  private fun Vec<X, *>.allFreeSVars() = contents.filter { it !is Constant<*> }
  private fun Mat<X, *, *>.allFreeSVars() = rows.flatMap { it.bindings.sVars }
  fun allFreeVariables() = allVarMap.filterValues { containsFreeVariable(it) }
  fun allBoundVariables() = allVarMap.filterValues { !containsFreeVariable(it) }

  private fun containsFreeVariable(it: Fun<X>): Boolean =
    (it is Mat<X, *, *> && it.bindings.allFreeVariables().isNotEmpty()) ||
    (it is MFun<X, *, *> && it !is Mat<X, *, *> && it !is MConst<X, *, *>) ||
    (it is Vec<X, *> && it.bindings.allFreeVariables().isNotEmpty()) ||
    (it is VFun<X, *> && it !is Vec<X, *> && it !is VConst<X, *>) ||
    (it is SFun<X> && it !is Constant)

  val areReassignmentFree = allFreeVariables().isEmpty()

  fun fullyDetermines(fn: SFun<X>) = fn.bindings.allVars.all { it in this }
  operator fun contains(v: Fun<X>) = v in allVars
  fun curried() = fMap.map { Bindings(mapOf(it.key to it.value)) }

  operator fun <T> get(t: T): T? = (allVarMap[t as? Variable<X>] ?: fMap[t as? Fun<X>]) as? T?
  override fun equals(other: Any?) = other is Bindings<*> && fMap == other.fMap
  override fun hashCode() = fMap.hashCode()
  override fun toString() = fMap.toString()
}

/**
 * Scalar function.
 */

sealed class SFun<X: SFun<X>>(override val bindings: Bindings<X>): Fun<X>, Field<SFun<X>> {
  constructor(vararg funs: Fun<X>): this(Bindings(*funs))
  protected open val ZERO: SFun<X> by lazy { Zero() }
  protected open val ONE: SFun<X> by lazy { One() }
  protected open val TWO: SFun<X> by lazy { Two() }
  protected open val E: SFun<X> by lazy { E<X>() }

  override fun plus(addend: SFun<X>): SFun<X> = Sum(this, addend)
  override fun times(multiplicand: SFun<X>): SFun<X> = Prod(this, multiplicand)
  override fun div(divisor: SFun<X>): SFun<X> = this * divisor.pow(-ONE)
  open operator fun <E: D1> times(multiplicand: VFun<X, E>): VFun<X, E> = SVProd(this, multiplicand)
  open operator fun <R: D1, C: D1> times(multiplicand: MFun<X, R, C>): MFun<X, R, C> = SMProd(this, multiplicand)

  override fun invoke(newBindings: Bindings<X>): SFun<X> =
    Composition(this, newBindings).run { if (newBindings.areReassignmentFree || EAGER) evaluate else this }

  operator fun invoke() = invoke(Bindings())

  open fun d(v1: SVar<X>) = Derivative(this, v1)//.let { if (EAGER) it.df() else it }
  open fun d(v1: SVar<X>, v2: SVar<X>): Vec<X, D2> = Vec(d(v1), d(v2))
  open fun d(v1: SVar<X>, v2: SVar<X>, v3: SVar<X>): Vec<X, D3> = Vec(d(v1), d(v2), d(v3))
  open fun d(vararg vars: SVar<X>): Map<SVar<X>, SFun<X>> = vars.map { it to d(it) }.toMap()

  open fun sin(): SFun<X> = Sine(this)
  open fun cos(): SFun<X> = Cosine(this)
  open fun tan(): SFun<X> = Tangent(this)
  fun exp(): SFun<X> = Power(E, this)

  open fun <L: D1> d(vVar: VVar<X, L>): VFun<X, L> = Gradient(this, vVar).let { if(EAGER) it.df() else it }
  open fun <R: D1, C: D1> d(mVar: MVar<X, R, C>): MFun<X, R, C> = MGradient(this, mVar).let { if(EAGER) it.df() else it }

  open fun grad(): Map<SVar<X>, SFun<X>> = bindings.sVars.map { it to Derivative(this, it) }.toMap()

  override fun ln(): SFun<X> = Log(this)
  override fun pow(exponent: SFun<X>): SFun<X> = Power(this, exponent)
  override fun unaryMinus(): SFun<X> = Negative(this)
  open fun sqrt(): SFun<X> = this pow (ONE / TWO)

  override fun toString(): String = when (this) {
    is Log -> "ln($left)"
    is Negative -> "- ($input)"
    is Power -> "($left).pow($right)"
    is SVar -> name
    is Derivative -> "d($fn) / d($vrb)"
    is Special -> javaClass.simpleName
    is BiFun<*> -> "($left) ${opCode()} ($right)"
    is UnFun<*> -> "${opCode()}($input)"
    is Composition -> "($fn)$bindings"
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

  override fun toGraph(): MutableNode = mutNode(if (this is SVar) "$this" else "${hashCode()}").apply {
    when (this@SFun) {
      is SVar -> name
      is Derivative -> { fn.toGraph() - this; mutNode("$this").apply { add(Label.of(vrb.toString())) } - this; add(Label.of("d")) }
      is BiFun<*> -> { (left.toGraph() - this).add(BLUE); (right.toGraph() - this).add(RED); add(Label.of(opCode())) }
      is UnFun<*> -> { input.toGraph() - this; add(Label.of(opCode())) }
      is RealNumber<*, *> -> add(Label.of(value.toString().take(5)))
      is Special -> add(Label.of(this@SFun.toString()))
      is Composition -> { fn.toGraph() - this; Factory.mutNode("$this").apply { add(Label.of(bindings.allFreeVariables().keys.toString())) } - this; add(Label.of("Comp")) }
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
class Negative<X: SFun<X>>(override val input: SFun<X>): SFun<X>(input), UnFun<X>
class Sum<X: SFun<X>>(override val left: SFun<X>, override val right: SFun<X>): SFun<X>(left, right), BiFun<X>
class Prod<X: SFun<X>>(override val left: SFun<X>, override val right: SFun<X>): SFun<X>(left, right), BiFun<X>
class Power<X: SFun<X>>(override val left: SFun<X>, override val right: SFun<X>): SFun<X>(left, right), BiFun<X>
class Log<X: SFun<X>>(override val left: SFun<X>, override val right: SFun<X> = E<X>()): SFun<X>(left, right), BiFun<X>

class Derivative<X: SFun<X>>(val fn: SFun<X>, val vrb: SVar<X>): SFun<X>(fn, vrb) {
  fun df() = fn.df()
  fun SFun<X>.df(): SFun<X> = when (this@df) {
    is SVar -> if (this == vrb) ONE else ZERO
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
    is DProd -> (left.d(vrb) as VFun<X, D1> dot right as VFun<X, D1>) + (left as VFun<X, D1> dot right.d(vrb))
    is Composition -> evaluate.df()
    is VSumAll<X, *> -> input.d(vrb).sum()
  }
}

// TODO: Unit test this data structure
class Composition<X : SFun<X>>(val fn: SFun<X>, val inputs: Bindings<X>) : SFun<X>(fn.bindings + inputs) {
  val evaluate: SFun<X> by lazy { bind(bindings) }

  @Suppress("UNCHECKED_CAST")
  fun SFun<X>.bind(bnds: Bindings<X>): SFun<X> =
    bnds[this@bind] ?: when (this@bind) {
      is SVar -> TODO()
      is SConst -> this@bind
      is Prod -> left.bind(bnds) * right.bind(bnds)
      is Sum -> left.bind(bnds) + right.bind(bnds)
      is Power -> left.bind(bnds) pow right.bind(bnds)
      is Negative -> -input.bind(bnds)
      is Sine -> input.bind(bnds).sin()
      is Cosine -> input.bind(bnds).cos()
      is Tangent -> input.bind(bnds).tan()
      is Log -> left.bind(bnds).ln()
      is Derivative -> df().bind(bnds)
      is DProd -> left(bnds) as VFun<X, D1> dot right(bnds) as VFun<X, D1>
      is Composition -> fn.bind(bnds)
      is VSumAll<X, *> -> input(bnds).sum()
    }.also { result ->
      val freeVars = result.bindings.allFreeVariables().keys
      val boundVars = bnds.allBoundVariables()
      val unpropogated = freeVars.filter { it in boundVars }
      if (unpropogated.isNotEmpty()) {
        show("input"); result.show("result")
        println("Bindings were $bnds")
        throw Exception("Bindings included unpropogated variables: $unpropogated")
      }
    }
}

class DProd<X: SFun<X>>(override val left: VFun<X, *>, override val right: VFun<X, *>): SFun<X>(left, right), BiFun<X>
class VSumAll<X: SFun<X>, E: D1>(override val input: VFun<X, E>): SFun<X>(input), UnFun<X>

class SVar<X: SFun<X>>(override val name: String = ""): Variable<X>, SFun<X>() {
  override val bindings: Bindings<X> = Bindings(mapOf(this to this))
  override fun equals(other: Any?) = other is SVar<*> && name == other.name
}

open class SConst<X: SFun<X>>: SFun<X>(), Constant<X> {
  open val doubleValue: Double = when (this) {
    is Zero -> 0.0
    is One -> 1.0
    is Two -> 2.0
    is E -> kotlin.math.E
    else -> Double.NaN
  }
}

sealed class Special<X: SFun<X>>: SConst<X>() {
  override fun toString() = javaClass.simpleName
  override fun equals(other: Any?) =
    if (this === other) true else javaClass == other?.javaClass

  override fun hashCode(): Int = when (this) {
    is Zero<*> -> 0
    is One<*> -> 1
    is Two<*> -> 2
    is E<*> -> 3
  }
}

class Zero<X: SFun<X>>: Special<X>()
class One<X: SFun<X>>: Special<X>()
class Two<X: SFun<X>>: Special<X>()
class E<X: SFun<X>>: Special<X>()

abstract class RealNumber<X: SFun<X>, Y>(open val value: Y): SConst<X>() {
  override fun toString() = value.toString()
  abstract fun wrap(number: Number): SConst<X>

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

  override fun <E: D1> times(multiplicand: VFun<X, E>): VFun<X, E> =
    when (multiplicand) {
      is Vec -> Vec(multiplicand.contents.map { this * it })
      else -> super.times(multiplicand)
    }

  @Suppress("UNCHECKED_CAST")
  override fun <R: D1, C: D1> times(multiplicand: MFun<X, R, C>) =
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

open class DReal(override val value: Double): RealNumber<DReal, Double>(value) {
  override fun wrap(number: Number) = DReal(number.toDouble())

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

sealed class Protocol<X: SFun<X>>(val prototype: RealNumber<X, *>) {
  val x = SVar<X>("x")
  val y = SVar<X>("y")
  val z = SVar<X>("z")

  val variables = listOf(x, y, z)

  val zero: Fun<X> = Zero()
  val one: Fun<X> = One()
  val two: Fun<X> = Two()
  val e: Fun<X> = E()

  val constants = listOf(zero to 0, one to 1, two to 2, e to E).bind()

  private fun wrapOrError(any: Any): Fun<X> = when (any) {
    is Fun<*> -> any as Fun<X>
    is Number -> wrap(any)
    else -> throw NumberFormatException("Invoke expects a number or function but got: $any")
  }

  fun List<Pair<Fun<X>, Any>>.bind(): Bindings<X> =
    Bindings(map { it.first to wrapOrError(it.second) }.toMap())

  fun wrap(number: Number): SConst<X> = prototype.wrap(number)

  inline fun <reified X: RealNumber<X, Y>, Y: Number> SFun<X>.toDouble() =
    try {
      (this as X).value.toDouble()
    } catch(e: ClassCastException) {
      show("before")
      e.printStackTrace()
      throw NumberFormatException("Scalar function has unbound free variables: ${bindings.allFreeVariables().keys}")
    }

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
  operator fun <T: Fun<X>> T.invoke(vararg numbers: Number): T =
    invoke(bindings.zip(numbers.map { wrap(it) }) + constants) as T

  operator fun <T: Fun<X>> T.invoke(vararg ps: Pair<Fun<X>, Any>): T =
    invoke(ps.toList().bind() + constants) as T

  fun <T> T.test(): T = this

  fun d(fn: SFun<X>) = Differential(fn)

  operator fun Number.times(multiplicand: SFun<X>) = wrap(this) * multiplicand
  operator fun SFun<X>.times(multiplicand: Number) = this * wrap(multiplicand)
  operator fun <E: D1> Number.times(multiplicand: VFun<X, E>): VFun<X, E> = wrap(this) * multiplicand
  operator fun <E: D1> VFun<X, E>.times(multiplicand: Number) = this * wrap(multiplicand)
  operator fun <R: D1, C: D1> Number.times(multiplicand: MFun<X, R, C>): MFun<X, R, C> = wrap(this) * multiplicand
  operator fun <R: D1, C: D1> MFun<X, R, C>.times(multiplicand: Number) = this * wrap(multiplicand)

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

  inline fun <reified E: D1> VFun<X, E>.magnitude() = (this ʘ this).sum().sqrt()

  fun <Y: Number> Vec(d0: Y) = VConst<X, D1>(wrap(d0))
  fun <Y: Number> Vec(d0: Y, d1: Y) = VConst<X, D2>(wrap(d0), wrap(d1))
  fun <Y: Number> Vec(d0: Y, d1: Y, d2: Y) = VConst<X, D3>(wrap(d0), wrap(d1), wrap(d2))
  fun <Y: Number> Vec(d0: Y, d1: Y, d2: Y, d3: Y) = VConst<X, D4>(wrap(d0), wrap(d1), wrap(d2), wrap(d3))
  fun <Y: Number> Vec(d0: Y, d1: Y, d2: Y, d3: Y, d4: Y) = VConst<X, D5>(wrap(d0), wrap(d1), wrap(d2), wrap(d3), wrap(d4))
  fun <Y: Number> Vec(d0: Y, d1: Y, d2: Y, d3: Y, d4: Y, d5: Y) = VConst<X, D6>(wrap(d0), wrap(d1), wrap(d2), wrap(d3), wrap(d4), wrap(d5))
  fun <Y: Number> Vec(d0: Y, d1: Y, d2: Y, d3: Y, d4: Y, d5: Y, d6: Y) = VConst<X, D7>(wrap(d0), wrap(d1), wrap(d2), wrap(d3), wrap(d4), wrap(d5), wrap(d6))
  fun <Y: Number> Vec(d0: Y, d1: Y, d2: Y, d3: Y, d4: Y, d5: Y, d6: Y, d7: Y) = VConst<X, D8>(wrap(d0), wrap(d1), wrap(d2), wrap(d3), wrap(d4), wrap(d5), wrap(d6), wrap(d7))
  fun <Y: Number> Vec(d0: Y, d1: Y, d2: Y, d3: Y, d4: Y, d5: Y, d6: Y, d7: Y, d8: Y) = VConst<X, D9>(wrap(d0), wrap(d1), wrap(d2), wrap(d3), wrap(d4), wrap(d5), wrap(d6), wrap(d7), wrap(d8))

  fun <Y: Number> MConst1x1(d0: Y) = Mat<X, D1, D1>(Vec(d0))
  fun <Y: Number> MConst1x2(d0: Y, d1: Y) = Mat<X, D1, D2>(Vec(d0, d1))
  fun <Y: Number> MConst1x3(d0: Y, d1: Y, d2: Y) = Mat<X, D1, D3>(Vec(d0, d1, d2))
  fun <Y: Number> MConst2x1(d0: Y, d1: Y) = Mat<X, D2, D1>(Vec(d0), Vec(d1))
  fun <Y: Number> MConst2x2(d0: Y, d1: Y, d2: Y, d3: Y) = MConst<X, D2, D2>(Vec(d0, d1), Vec(d2, d3))
  fun <Y: Number> MConst2x3(d0: Y, d1: Y, d2: Y, d3: Y, d4: Y, d5: Y) = Mat<X, D2, D3>(Vec(d0, d1, d2), Vec(d3, d4, d5))
  fun <Y: Number> MConst3x1(d0: Y, d1: Y, d2: Y) = Mat<X, D3, D1>(Vec(d0), Vec(d1), Vec(d2))
  fun <Y: Number> MConst3x2(d0: Y, d1: Y, d2: Y, d3: Y, d4: Y, d5: Y) = Mat<X, D3, D2>(Vec(d0, d1), Vec(d2, d3), Vec(d4, d5))
  fun <Y: Number> MConst3x3(d0: Y, d1: Y, d2: Y, d3: Y, d4: Y, d5: Y, d6: Y, d7: Y, d8: Y) = Mat<X, D3, D3>(Vec(d0, d1, d2), Vec(d3, d4, d5), Vec(d6, d7, d8))

  inline fun <R: D1, C: D1, Y: Number> Mat(r: Nat<R>, c: Nat<C>, gen: (Int, Int) -> Y): Mat<X, R, C> =
    Mat(List(r.i) { row -> Vec(List(c.i) { col -> wrap(gen(row, col)) }) })

  inline fun <E: D1, Y: Number> Vec(e: Nat<E>, gen: (Int) -> Y): Vec<X, E> = Vec(List(e.i) { wrap(gen(it)) })

  fun Var(name: String) = SVar<X>(name)
  fun Var2(name: String) = VVar<X, D2>(name, D2)
  fun Var3(name: String) = VVar<X, D3>(name, D3)
  fun Var4(name: String) = VVar<X, D4>(name, D5)
  fun Var5(name: String) = VVar<X, D5>(name, D5)
  fun Var9(name: String) = VVar<X, D9>(name, D9)

  fun Var2x1(name: String) = MVar<X, D2, D1>(name, D2, D1)
  fun Var2x2(name: String) = MVar<X, D2, D2>(name, D2, D2)
  fun Var2x3(name: String) = MVar<X, D2, D3>(name, D2, D3)
  fun Var3x1(name: String) = MVar<X, D3, D1>(name, D3, D1)
  fun Var3x2(name: String) = MVar<X, D3, D2>(name, D3, D2)
  fun Var3x3(name: String) = MVar<X, D3, D3>(name, D3, D3)
  fun Var5x5(name: String) = MVar<X, D5, D5>(name, D5, D5)
  fun Var9x9(name: String) = MVar<X, D9, D9>(name, D9, D9)

  val DARKMODE = false
  val THICKNESS = 2

  fun Fun<*>.show() = renderAsSVG { toGraph() }.show()
  fun SFun<*>.renderAsSVG() = renderAsSVG { toGraph() }

  inline fun renderAsSVG(crossinline op: () -> MutableNode) =
    graph(directed = true) {
      val color = if (DARKMODE) WHITE else BLACK

      edge[color, Arrow.NORMAL, Style.lineWidth(THICKNESS)]

      graph[Rank.dir(Rank.RankDir.LEFT_TO_RIGHT), TRANSPARENT.background()]

      node[color, color.font(), Font.config("Helvetica", 20),
        Style.lineWidth(THICKNESS)]

      op()
    }.toGraphviz().render(Format.SVG)

  fun Renderer.saveToFile(filename: String) = toFile(File(filename))

  fun Renderer.show() = toFile(File.createTempFile("temp", ".svg")).show()
  fun File.show() = ProcessBuilder("x-www-browser", path).start()
}

object DoublePrecision: Protocol<DReal>(DReal)
object BigDecimalPrecision: Protocol<BDReal>(BDReal)