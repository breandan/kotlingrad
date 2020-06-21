@file:Suppress("FunctionName", "LocalVariableName", "unused", "UNUSED_VARIABLE")

package edu.umontreal.kotlingrad.experimental

import guru.nidi.graphviz.*
import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.attribute.Color.*
import guru.nidi.graphviz.attribute.GraphAttr.*
import guru.nidi.graphviz.attribute.Rank.RankDir.*
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Renderer
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.MutableNode
import java.io.File
import java.io.Serializable
import kotlin.NumberFormatException
import kotlin.math.*
import kotlin.reflect.KProperty

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

interface Fun<X: SFun<X>>: (Bindings<X>) -> Fun<X>, Serializable {
  val bindings: Bindings<X>
  fun opCode() = javaClass.simpleName

  fun isConstant() = bindings.allVars.isEmpty()
  fun wrap(number: Number) = SConst<X>(number.toDouble())

  override operator fun invoke(newBindings: Bindings<X>): Fun<X>

//  operator fun invoke(sFun: SFun<X>): Fun<X> = invoke(bindings.zip(fns).bind() + constants) as bindings.z
  operator fun <A: Fun<X>> A.invoke(vararg fns: Fun<X>): A = invoke(bindings.zip(fns.toList())) as A
  operator fun <A: Fun<X>> A.invoke(vararg pairs: Pair<Fun<X>, Fun<X>>): A = invoke(Bindings(*pairs)) as A

  fun toGraph(): MutableNode
}

interface BiFun<X: SFun<X>>: Fun<X> {
  val left: Fun<X>
  val right: Fun<X>
}

interface UnFun<X: SFun<X>>: Fun<X> { val input: Fun<X> }
interface Variable<X: SFun<X>>: Fun<X> { val name: String }
interface Constant<X: SFun<X>>: Fun<X>

// Supports arbitrary subgraph reassignment but usually just holds variable-to-value bindings
@Suppress("UNCHECKED_CAST")
data class Bindings<X: SFun<X>>(val fMap: Map<Fun<X>, Fun<X>> = mapOf()) {
  constructor(inputs: List<Bindings<X>>): this(inputs.map { it.fMap }
    .fold(mapOf<Fun<X>, Fun<X>>()) { acc, fMap -> fMap + acc })
  constructor(vararg bindings: Bindings<X>): this(bindings.toList())
  constructor(vararg funs: Fun<X>): this(funs.map { it.bindings })
  constructor(vararg pairs: Pair<Fun<X>, Fun<X>>): this(pairs.toMap())

  // TODO: Take shape into consideration when binding variables
  fun zip(fns: List<Fun<X>>): Bindings<X> =
    (sVars.zip(fns.filterIsInstance<SFun<X>>())+
        vVars.zip(fns.filterIsInstance<VFun<X, *>>()) +
        mVars.zip(fns.filterIsInstance<MFun<X, *, *>>())
    ).let { Bindings(*it.toTypedArray()) }

  // Scalar, vector, and matrix "views" on untyped function map
  val sFunMap = filterInstancesOf<SFun<X>>()
  val vFunMap = filterInstancesOf<VFun<X, *>>()
  val mFunMap = filterInstancesOf<MFun<X, *, *>>()

  val mVarMap = mFunMap.filterKeys { it is MVar<X, *, *> } as Map<MVar<X, *, *>, MFun<X, *, *>>
  val vVarMap = mVarMap.filterValues { it is Mat<X, *, *> }
    .flatMap { (k, v) -> k.vVars.zip((v as Mat<X, *, *>).rows) }.toMap() +
  vFunMap.filterKeys { it is VVar<X, *> } as Map<VVar<X, *>, VFun<X, *>>
  val sVarMap = (vVarMap.filterValues { it is Vec<X, *> }
    .flatMap { (k, v) -> k.sVars.zip((v as Vec<X, *>).contents) }.toMap() +
    sFunMap.filterKeys { it is SVar<X> && it.name != "mapInput"}) as Map<SVar<X>, SFun<X>>

  val allVarMap = mVarMap + vVarMap + sVarMap

  private inline fun <reified T> filterInstancesOf(): Map<T, T> =
    fMap.filterKeys { it is T } as Map<T, T>

  // Merges two variable bindings
  // TODO: Add support for change of variables, i.e. x = y, y = 2z, z = x + y...
  operator fun plus(other: Bindings<X>) =
    Bindings(
      fMap + other.fMap +
      allVarMap.filterValues { containsFreeVariable(it) } +
      other.allVarMap.filterValues { containsFreeVariable(it) } +
      allVarMap.filterValues { !containsFreeVariable(it) } +
      other.allVarMap.filterValues { !containsFreeVariable(it) }
    )

  operator fun plus(pair: Pair<Fun<X>, Fun<X>>) = plus(Bindings(pair))

  operator fun minus(func: Fun<X>) = Bindings(fMap.filterNot { it.key == func })

  // Scalar, vector, and matrix variables
  val sVars: Set<SVar<X>> = sVarMap.keys
  val vVars: Set<VVar<X, *>> = vVarMap.keys
  val mVars: Set<MVar<X, *, *>> = mVarMap.keys
  val allVars: Set<Variable<X>> = sVars + vVars + mVars
  val allFreeVariables by lazy { allVarMap.filterValues { containsFreeVariable(it) } }
  val allBoundVariables: Map<Variable<X>, Fun<X>> by lazy { allVarMap.filterValues { !containsFreeVariable(it) } }

  private fun containsFreeVariable(it: Fun<X>): Boolean =
    (it is Mat<X, *, *> && it.bindings.allFreeVariables.isNotEmpty()) ||
    (it is MFun<X, *, *> && it !is Mat<X, *, *> && it !is MConst<X, *, *>) ||
    (it is Vec<X, *> && it.bindings.allFreeVariables.isNotEmpty()) ||
    (it is VFun<X, *> && it !is Vec<X, *> && it !is VConst<X, *>) ||
    (it is SFun<X> && it !is Constant)

  val complete = allFreeVariables.isEmpty()
  val readyToBind = allBoundVariables.isNotEmpty()

  fun fullyDetermines(fn: SFun<X>) = fn.bindings.allVars.all { it in this }
  operator fun contains(v: Fun<X>) = v in allVars
  fun curried() = fMap.map { (k, v) -> Bindings(k to v) }

  // Intended for debugging purposes, should be removed eventually
  fun checkForUnpropagatedVariables(before: Fun<X>, after: Fun<X>) {
    val freeVars = after.bindings.allFreeVariables.keys
    val boundVars = allBoundVariables.keys
    val unpropagated = (freeVars intersect boundVars).map { it to this[it] }
    if (unpropagated.isNotEmpty()) {
      before.show("input"); after.show("result")
      println("Free vars: $freeVars")
      println("Bindings were $this")
      throw Exception("Result included unpropagated variables: $unpropagated")
    }
  }

  operator fun <T: Fun<X>> get(t: T): T? = (allVarMap[t as? Variable<X>] ?: fMap[t]) as? T?
  override fun equals(other: Any?) = other is Bindings<*> && fMap == other.fMap
  override fun hashCode() = fMap.hashCode()
  override fun toString() = fMap.toString()
}

/**
 * Scalar function.
 */

sealed class SFun<X: SFun<X>>(override val bindings: Bindings<X>): Fun<X>, Field<SFun<X>> {
  constructor(vararg funs: Fun<X>): this(Bindings(*funs))
  open val ZERO: Special<X> by lazy { Zero() }
  open val ONE: Special<X> by lazy { One() }
  open val TWO: Special<X> by lazy { Two() }
  open val E: Special<X> by lazy { E<X>() }
  val x by lazy { SVar<X>("x") }
  val y by lazy { SVar<X>("y") }
  val z by lazy { SVar<X>("z") }

  override fun plus(addend: SFun<X>): SFun<X> = Sum(this, addend)
  override fun times(multiplicand: SFun<X>): SFun<X> = Prod(this, multiplicand)
  override fun div(divisor: SFun<X>): SFun<X> = this * divisor.pow(-ONE)
  open operator fun <E: D1> times(multiplicand: VFun<X, E>): VFun<X, E> = SVProd(this, multiplicand)
  open operator fun <R: D1, C: D1> times(multiplicand: MFun<X, R, C>): MFun<X, R, C> = SMProd(this, multiplicand)

  override fun invoke(newBindings: Bindings<X>): SFun<X> =
    SComposition(this, newBindings)
      .run { if (bindings.complete || newBindings.readyToBind || EAGER) evaluate else this }

  operator fun invoke() = invoke(Bindings())

  open fun d(v1: SVar<X>): SFun<X> = Derivative(this, v1)//.let { if (EAGER) it.df() else it }
  open fun d(v1: SVar<X>, v2: SVar<X>): Vec<X, D2> = Vec(d(v1), d(v2))
  open fun d(v1: SVar<X>, v2: SVar<X>, v3: SVar<X>): Vec<X, D3> = Vec(d(v1), d(v2), d(v3))
  open fun d(vararg vars: SVar<X>): Map<SVar<X>, SFun<X>> = vars.map { it to d(it) }.toMap()

  open fun sin(): SFun<X> = Sine(this)
  open fun cos(): SFun<X> = Cosine(this)
  open fun tan(): SFun<X> = Tangent(this)
  fun exp(): SFun<X> = Power(E, this)

  open fun <L: D1> d(vVar: VVar<X, L>): VFun<X, L> = Gradient(this, vVar)//.let { if(EAGER) it.df() else it }
  open fun <R: D1, C: D1> d(mVar: MVar<X, R, C>): MFun<X, R, C> = MGradient(this, mVar)//.let { if(EAGER) it.df() else it }

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
    is SComposition -> "($fn)$bindings"
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
    else -> javaClass.simpleName
  }

  override fun toGraph(): MutableNode = mutNode(if (this is SVar) "$this" else "${hashCode()}").apply {
    when (this@SFun) {
      is SVar -> name
      is Derivative -> { fn.toGraph() - this; mutNode("$this").apply { add(Label.of(vrb.toString())) } - this; add(Label.of("d")) }
      is RealNumber<*, *> -> add(Label.of(value.toString().take(5)))
      is Special -> add(Label.of(this@SFun.toString()))
      is SComposition -> { fn.toGraph() - this; mutNode("$this").apply { add(Label.of(bindings.allFreeVariables.keys.toString())) } - this; add(Label.of("SComp")) }
      is BiFun<*> -> { (left.toGraph() - this).add(BLUE); (right.toGraph() - this).add(RED); add(Label.of(opCode())) } // add(Label.of("{{<In0>|<In1>}|${opCode()}|{<Out0>}}")) }
      is UnFun<*> -> { input.toGraph() - this; add(Label.of(opCode())) }
      is SConst<*> -> add(Label.of(this@SFun.toString()))
      else -> TODO(this@SFun.javaClass.toString())
    }
  }
}

/**
 * Symbolic operators.
 */

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
      if (right.isConstant()) right * left.pow(right - ONE) * left.df() //https://en.wikipedia.org/wiki/Differentiation_rules#The_polynomial_or_elementary_power_rule
      else this * (left.df() * right / left + right.df() * left.ln()) //https://en.wikipedia.org/wiki/Differentiation_rules#Generalized_power_rule
    is Negative -> -input.df()
    is Log -> (left pow -ONE) * left.df()
    is Sine -> input.cos() * input.df()
    is Cosine -> -input.sin() * input.df()
    is Tangent -> (input.cos() pow -TWO) * input.df()
    is Derivative -> fn.df()
    is DProd -> (left.d(vrb) as VFun<X, D1> dot right as VFun<X, D1>) + (left as VFun<X, D1> dot right.d(vrb))
    is SComposition -> evaluate.df()
    is VSumAll<X, *> -> input.d(vrb).sum()
//    is Custom<X> -> fn.df()
  }
}

// TODO: Unit test this data structure
class SComposition<X : SFun<X>>(val fn: SFun<X>, inputs: Bindings<X>) : SFun<X>(fn.bindings + inputs) {
  val evaluate: SFun<X> by lazy { bind(bindings) }

  @Suppress("UNCHECKED_CAST")
  fun SFun<X>.bind(bnds: Bindings<X>): SFun<X> =
    bnds[this@bind] ?: when (this@bind) {
      is SVar -> this
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
      is SComposition -> fn.bind(bnds + bindings)
      is VSumAll<X, *> -> input(bnds).sum()
    }.also { result -> bnds.checkForUnpropagatedVariables(this@bind, result) }
}

class DProd<X: SFun<X>>(override val left: VFun<X, *>, override val right: VFun<X, *>): SFun<X>(left, right), BiFun<X>
class VSumAll<X: SFun<X>, E: D1>(override val input: VFun<X, E>): SFun<X>(input), UnFun<X>

class SVar<X: SFun<X>>(override val name: String = ""): Variable<X>, SFun<X>() {
  override val bindings: Bindings<X> = Bindings(mapOf(this to this))
  override fun equals(other: Any?) = other is SVar<*> && name == other.name
  override fun hashCode(): Int = name.hashCode()
  operator fun getValue(thisRef: Any?, property: KProperty<*>) =
    SVar<X>(if (name.isEmpty()) property.name else name)
}

open class SConst<X: SFun<X>>(open val value: Number? = null): SFun<X>(), Constant<X> {
  override fun toString() = doubleValue.toString()
  open val doubleValue: Double = value?.toDouble() ?: when (this) {
    is Zero -> 0.0
    is One -> 1.0
    is Two -> 2.0
    is E -> kotlin.math.E
    else -> Double.NaN
  }

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
    is SConst<X> -> wrap(doubleValue + addend.doubleValue)
    else -> super.plus(addend)
  }

  override fun times(multiplicand: SFun<X>) = when (multiplicand) {
    is SConst<X> -> wrap(doubleValue * multiplicand.doubleValue)
    else -> super.times(multiplicand)
  }

  override fun pow(exponent: SFun<X>) = when (exponent) {
    is SConst<X> -> wrap(doubleValue.pow(exponent.doubleValue))
    else -> super.pow(exponent)
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

abstract class RealNumber<X: RealNumber<X, Y>, Y: Number>(override val value: Y): SConst<X>() {
  override fun toString() = value.toString()
  abstract override fun wrap(number: Number): SConst<X>

  override val doubleValue: Double by lazy {
    when (this) {
      is BDReal -> value.toDouble()
      is DReal -> value
      else -> super.doubleValue
    }
  }
}

open class DReal(override val value: Double): RealNumber<DReal, Double>(value) {
  override fun wrap(number: Number) = DReal(number.toDouble())
  companion object: DReal(Double.NaN)
}

/**
 * Numerical context. Converts numerical types from host language to eDSL.
 */

abstract class Protocol<X: RealNumber<X, *>>(val prototype: X) {
  val constants = prototype.run { listOf(ZERO to 0, ONE to 1, TWO to 2, E to Math.E).bind() }
  val x = prototype.x
  val y = prototype.y
  val z = prototype.z
  open val variables = listOf(x, y, z)

  fun wrapOrError(any: Any): Fun<X> = when (any) {
    is Fun<*> -> any as Fun<X>
    is Number -> prototype.wrap(any)
    else -> throw NumberFormatException("Invoke expects a number or function but got: $any")
  }

  fun List<Pair<Fun<X>, Any>>.bind(): Bindings<X> =
    Bindings(map { it.first to wrapOrError(it.second) }.toMap())

  fun wrap(number: Number): SConst<X> = prototype.wrap(number)

  fun SFun<X>.toDouble() =
    try {
      (this as SConst<X>).doubleValue
    } catch(e: ClassCastException) {
      show("before")
      e.printStackTrace()
      throw NumberFormatException("Scalar function ${javaClass.simpleName} has unbound free variables: ${bindings.allFreeVariables.keys}")
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

  /**
   * Invocation is cast as T, to keep track of the output type and type check
   * subsequent operations. T must never be more specific or more abstract than
   * [SFun], [VFun] or [MFun]. If the receiver is a superclass (i.e. [Fun]),
   * type checking subsequent composition will be impossible. If the receiver
   * is a subclass, the cast will fail after T has been evaluated to [Constant].
   */

  operator fun <T: Fun<X>> T.invoke(vararg numbers: Number): T =
    invoke(bindings.zip(numbers.map { wrap(it) }) + constants) as T

  operator fun <T: Fun<X>> T.invoke(vararg funs: Fun<X>): T =
    invoke(bindings.zip(funs.toList()) + constants) as T

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

  fun <Y: Number> Vec(y0: Y) = VConst<X, D1>(wrap(y0))
  fun <Y: Number> Vec(y0: Y, y1: Y) = VConst<X, D2>(wrap(y0), wrap(y1))
  fun <Y: Number> Vec(y0: Y, y1: Y, y2: Y) = VConst<X, D3>(wrap(y0), wrap(y1), wrap(y2))
  fun <Y: Number> Vec(y0: Y, y1: Y, y2: Y, y3: Y) = VConst<X, D4>(wrap(y0), wrap(y1), wrap(y2), wrap(y3))
  fun <Y: Number> Vec(y0: Y, y1: Y, y2: Y, y3: Y, y4: Y) = VConst<X, D5>(wrap(y0), wrap(y1), wrap(y2), wrap(y3), wrap(y4))
  fun <Y: Number> Vec(y0: Y, y1: Y, y2: Y, y3: Y, y4: Y, y5: Y) = VConst<X, D6>(wrap(y0), wrap(y1), wrap(y2), wrap(y3), wrap(y4), wrap(y5))
  fun <Y: Number> Vec(y0: Y, y1: Y, y2: Y, y3: Y, y4: Y, y5: Y, y6: Y) = VConst<X, D7>(wrap(y0), wrap(y1), wrap(y2), wrap(y3), wrap(y4), wrap(y5), wrap(y6))
  fun <Y: Number> Vec(y0: Y, y1: Y, y2: Y, y3: Y, y4: Y, y5: Y, y6: Y, y7: Y) = VConst<X, D8>(wrap(y0), wrap(y1), wrap(y2), wrap(y3), wrap(y4), wrap(y5), wrap(y6), wrap(y7))
  fun <Y: Number> Vec(y0: Y, y1: Y, y2: Y, y3: Y, y4: Y, y5: Y, y6: Y, y7: Y, y8: Y) = VConst<X, D9>(wrap(y0), wrap(y1), wrap(y2), wrap(y3), wrap(y4), wrap(y5), wrap(y6), wrap(y7), wrap(y8))

  fun <Y: Number> Mat1x1(y0: Y) = MConst<X, D1, D1>(Vec(y0))
  fun <Y: Number> Mat1x2(y0: Y, y1: Y) = MConst<X, D1, D2>(Vec(y0, y1))
  fun <Y: Number> Mat1x3(y0: Y, y1: Y, y2: Y) = MConst<X, D1, D3>(Vec(y0, y1, y2))
  fun <Y: Number> Mat2x1(y0: Y, y1: Y) = MConst<X, D2, D1>(Vec(y0), Vec(y1))
  fun <Y: Number> Mat2x2(y0: Y, y1: Y, y2: Y, y3: Y) = MConst<X, D2, D2>(Vec(y0, y1), Vec(y2, y3))
  fun <Y: Number> Mat2x3(y0: Y, y1: Y, y2: Y, y3: Y, y4: Y, y5: Y) = MConst<X, D2, D3>(Vec(y0, y1, y2), Vec(y3, y4, y5))
  fun <Y: Number> Mat3x1(y0: Y, y1: Y, y2: Y) = MConst<X, D3, D1>(Vec(y0), Vec(y1), Vec(y2))
  fun <Y: Number> Mat3x2(y0: Y, y1: Y, y2: Y, y3: Y, y4: Y, y5: Y) = MConst<X, D3, D2>(Vec(y0, y1), Vec(y2, y3), Vec(y4, y5))
  fun <Y: Number> Mat3x3(y0: Y, y1: Y, y2: Y, y3: Y, y4: Y, y5: Y, y6: Y, y7: Y, y8: Y) = MConst<X, D3, D3>(Vec(y0, y1, y2), Vec(y3, y4, y5), Vec(y6, y7, y8))

  inline fun <R: D1, C: D1, Y: Number> Mat(r: Nat<R>, c: Nat<C>, gen: (Int, Int) -> Y): Mat<X, R, C> =
    Mat(List(r.i) { row -> Vec(List(c.i) { col -> wrap(gen(row, col)) }) })

  inline fun <reified E: D1> Vec(e: Nat<E>, gen: (Int) -> Any): Vec<X, E> = Vec(List(e.i) { wrapOrError(gen(it)) as SFun<X> })

  fun Var(name: String = "") = SVar<X>(name)
  fun Var2(name: String = "") = VVar<X, D2>(name, D2)
  fun Var3(name: String = "") = VVar<X, D3>(name, D3)
  fun Var4(name: String = "") = VVar<X, D4>(name, D4)
  fun Var5(name: String = "") = VVar<X, D5>(name, D5)
  fun Var6(name: String = "") = VVar<X, D6>(name, D6)
  fun Var7(name: String = "") = VVar<X, D7>(name, D7)
  fun Var8(name: String = "") = VVar<X, D8>(name, D8)
  fun Var9(name: String = "") = VVar<X, D9>(name, D9)
  fun Var10(name: String = "") = VVar<X, D10>(name, D10)
  fun Var11(name: String = "") = VVar<X, D11>(name, D11)
  fun Var12(name: String = "") = VVar<X, D12>(name, D12)
  fun Var13(name: String = "") = VVar<X, D13>(name, D13)
  fun Var14(name: String = "") = VVar<X, D14>(name, D14)
  fun Var15(name: String = "") = VVar<X, D15>(name, D15)
  fun Var16(name: String = "") = VVar<X, D16>(name, D16)
  fun Var17(name: String = "") = VVar<X, D17>(name, D17)
  fun Var18(name: String = "") = VVar<X, D18>(name, D18)
  fun Var19(name: String = "") = VVar<X, D19>(name, D19)
  fun Var20(name: String = "") = VVar<X, D20>(name, D20)
  fun Var21(name: String = "") = VVar<X, D21>(name, D21)
  fun Var22(name: String = "") = VVar<X, D22>(name, D22)
  fun Var23(name: String = "") = VVar<X, D23>(name, D23)
  fun Var24(name: String = "") = VVar<X, D24>(name, D24)
  fun Var25(name: String = "") = VVar<X, D25>(name, D25)
  fun Var26(name: String = "") = VVar<X, D26>(name, D26)
  fun Var27(name: String = "") = VVar<X, D27>(name, D27)
  fun Var28(name: String = "") = VVar<X, D28>(name, D28)
  fun Var29(name: String = "") = VVar<X, D29>(name, D29)
  fun Var30(name: String = "") = VVar<X, D30>(name, D30)

  fun Var2x1(name: String = "") = MVar<X, D2, D1>(name, D2, D1)
  fun Var2x2(name: String = "") = MVar<X, D2, D2>(name, D2, D2)
  fun Var2x3(name: String = "") = MVar<X, D2, D3>(name, D2, D3)
  fun Var3x1(name: String = "") = MVar<X, D3, D1>(name, D3, D1)
  fun Var3x2(name: String = "") = MVar<X, D3, D2>(name, D3, D2)
  fun Var3x3(name: String = "") = MVar<X, D3, D3>(name, D3, D3)
  fun Var5x5(name: String = "") = MVar<X, D5, D5>(name, D5, D5)
  fun Var9x9(name: String = "") = MVar<X, D9, D9>(name, D9, D9)

  val DARKMODE = false
  val THICKNESS = 2

  inline fun render(format: Format = Format.SVG, crossinline op: () -> MutableNode) =
    graph(directed = true) {
      val color = if (DARKMODE) WHITE else BLACK


      edge[color, Arrow.NORMAL, Style.lineWidth(THICKNESS)]

      graph[Rank.dir(LEFT_TO_RIGHT), TRANSPARENT.background(), margin(0.0), Attributes.attr("compound", "true"), Attributes.attr("nslimit", "20")]

      node[color, color.font(), Font.config("JetBrains Mono", 20), Style.lineWidth(THICKNESS), Attributes.attr("shape", "Mrecord")]

      op()
    }.toGraphviz().render(format)

  fun extToFormat(string: String): Format = when(string) {
    "dot" -> Format.DOT
    "png" -> Format.PNG
    "ps" -> Format.PS
    else -> Format.SVG
  }
  fun SFun<*>.saveToFile(filename: String) =
    render(extToFormat(filename.split(".").last())) { toGraph() }.saveToFile(filename)
  fun SFun<*>.render(format: Format = Format.SVG) = render(format) { toGraph() }
  fun Renderer.saveToFile(filename: String) = File(filename).writeText(toString().replace("]", "];"))
  fun Fun<*>.show() = render { toGraph() }.show()
  fun Renderer.show() = toFile(File.createTempFile("temp", ".svg")).show()
  fun File.show() = ProcessBuilder("x-www-browser", path).start()
}

object DoublePrecision: Protocol<DReal>(DReal)
object BigDecimalPrecision: Protocol<BDReal>(BDReal)