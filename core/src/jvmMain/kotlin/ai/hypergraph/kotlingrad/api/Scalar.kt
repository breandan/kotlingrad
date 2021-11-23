@file:Suppress("FunctionName", "LocalVariableName", "unused", "UNUSED_VARIABLE")

package ai.hypergraph.kotlingrad.api

import ai.hypergraph.kaliningraph.circuits.*
import ai.hypergraph.kotlingrad.shapes.*
import kotlin.Double.Companion.NaN
import kotlin.math.*
import kotlin.reflect.KProperty

/**
 * Interface representing a generic mathematical function.
 *
 * TODO: Implement proper monad like [java.util.function.Function] or [Function]
 */

interface Fun<X: SFun<X>>: (Bindings<X>) -> Fun<X> {
  val inputs: Array<out Fun<X>>
  val bindings: Bindings<X>
    get() = Bindings(*inputs)
  val op: Op

  fun isConstant(): Boolean = bindings.allVars.isEmpty()
  fun wrap(number: Number): SConst<X> = SConst(number.toDouble())

  override operator fun invoke(newBindings: Bindings<X>): Fun<X>
  operator fun invoke(): Fun<X>
  operator fun invoke(vararg numbers: Number): Fun<X> =
    invoke(bindings.zip(numbers.map { wrap(it) }))

  operator fun invoke(vararg funs: Fun<X>): Fun<X> = invoke(bindings.zip(funs.toList()))

  operator fun invoke(vararg ps: FunToAny<X>): Fun<X> = invoke(ps.toList().bind())

  fun toGate(): Gate = when (this) {
    is NilFun -> Gate.wrap(this)
    is UnFun -> Gate(op, input.toGate())
    is BiFun -> Gate(op, left.toGate(), right.toGate())
    is PolyFun -> Gate(op, *inputs.map { it.toGate() }.toTypedArray())
    else -> TODO(this::class.simpleName!!)
  }

  fun toGraph() = toGate().graph

  fun List<FunToAny<X>>.bind() = Bindings(associate { it.first to wrapOrError(it.second) })

  fun wrapOrError(any: Any): Fun<X> = when (any) {
    is Fun<*> -> any as Fun<X>
    is Number -> wrap(any)
    is List<*> -> when {
      any.isEmpty() -> throw Exception("Empty collection")
      any.first() is Number -> Vec<X, D1>(any.map { wrap(it as Number) })
      else -> TODO() // else if(any.first() is List<*>) Mat<X, D1, D1>()
    }
    is Array<*> -> wrapOrError(any.toList())
    else -> throw NumberFormatException("Invoke expects a number or function but got: $any")
  }

  fun asString(): String = when (this) {
    is Constant -> "$this"
    is Variable -> "Var($name)"
    is Grad -> "d($input) / d($vrb)"
    is SComposition -> "($input)$bindings"
    is Power<*> -> "($left).$op($right)" //TODO: separate asString() and toCode()
    is BiFun<*> -> "($left) $op ($right)"
    is UnFun<*> -> "$op($input)"
    is PolyFun<*> -> "$op$inputs"
    else -> this::class.simpleName!!
  }
}

// https://arxiv.org/pdf/2001.02209.pdf

// Describes the arity of the operator

interface NilFun<X: SFun<X>>: Fun<X>

interface UnFun<X: SFun<X>>: PolyFun<X> {
  val input: Fun<X>
}

interface BiFun<X: SFun<X>>: PolyFun<X> {
  val left: Fun<X>
  val right: Fun<X>
}

interface PolyFun<X: SFun<X>>: Fun<X> {
  override val inputs: Array<out Fun<X>>
}

interface Grad<X: SFun<X>>: BiFun<X> {
  val input: Fun<X>
  val vrb: Variable<X>
  override val left: Fun<X>
    get() = input
  override val right: Fun<X>
    get() = vrb
}

interface Variable<X: SFun<X>>: Fun<X>, NilFun<X> {
  val name: String
}

interface Constant<X: SFun<X>>: Fun<X>, NilFun<X>

/**
 * Scalar function.
 */

sealed class SFun<X: SFun<X>>
constructor(override vararg val inputs: Fun<X>): PolyFun<X>, Field<SFun<X>> {
  val ZERO: Special<X> by lazy { Zero() }
  val ONE: Special<X> by lazy { One() }
  val TWO: Special<X> by lazy { Two() }
  val E: Special<X> by lazy { E<X>() }

  override fun plus(addend: SFun<X>): SFun<X> = Sum(this, addend)
  override fun times(multiplicand: SFun<X>): SFun<X> = Prod(this, multiplicand)
  override fun div(divisor: SFun<X>): SFun<X> = this * divisor.pow(-ONE)
  open operator fun <E: D1> times(multiplicand: VFun<X, E>): VFun<X, E> = SVProd(this, multiplicand)
  open operator fun <R: D1, C: D1> times(multiplicand: MFun<X, R, C>): MFun<X, R, C> = SMProd(this, multiplicand)

  /**
   * TODO: Parameterize operator in constructor instead of using subtyping,
   * this would allow us to migrate from subclass to operator matching [Gate].
   */

  class AOP<X: SFun<X>>(val op: Op, val of: (Fun<X>) -> SFun<X>)

  // TODO: Implement a proper curry and lower variadic apply onto it.
  fun apply(op: Op): (SFun<X>) -> SFun<X> = when (op) {
    Ops.sum -> { it: SFun<X> -> this + it }
    Ops.prod -> { it: SFun<X> -> this * it }
    else -> TODO(op::class.simpleName!!)
  }

  /**
   * TODO: Figure out how to avoid casting. Once stable, port to [VFun], [MFun].
   */

  @Suppress("UNCHECKED_CAST")
  fun apply(vararg xs: Fun<X>): SFun<X> = when (op) {
    Ops.id -> this
    Ops.sin -> (xs[0] as SFun<X>).sin()
    Ops.cos -> (xs[0] as SFun<X>).cos()
    Ops.tan -> (xs[0] as SFun<X>).tan()
    Ops.sub -> -(xs[0] as SFun<X>)

    Ops.d -> TODO()
    Ops.sum -> (xs[0] as SFun<X>) + xs[1] as SFun<X>
    Ops.prod -> (xs[0] as SFun<X>) * xs[1] as SFun<X>
    Ops.pow -> (xs[0] as SFun<X>) pow xs[1] as SFun<X>
    Ops.log -> (xs[0] as SFun<X>).log(xs[1] as SFun<X>)
    Ops.dot -> (xs[0] as VFun<X, DN>) dot xs[1] as VFun<X, DN>

    Ops.Σ -> (xs[0] as VFun<X, DN>).sum()
    Ops.λ -> TODO()
    else -> TODO(op::class.simpleName!!)
  }

  override val op: Op = when (this) {
    is SVar -> Ops.id
    is SConst -> Ops.id
    is Sine -> Ops.sin
    is Cosine -> Ops.cos
    is Tangent -> Ops.tan
    is Negative -> Ops.sub

    is Sum -> Ops.sum
    is Prod -> Ops.prod
    is Power -> Ops.pow
    is Log -> Ops.log
    is Derivative -> Ops.d
    is DProd -> Ops.dot

    is SComposition -> Ops.λ
    is VSumAll<X, *> -> Ops.Σ
  }

  override fun invoke(newBindings: Bindings<X>): SFun<X> =
    SComposition(this, newBindings).run {
      if (bindings.complete || newBindings.readyToBind || EAGER) evaluate else this
    }

  override operator fun invoke(): SFun<X> = SComposition(this).evaluate
  override operator fun invoke(vararg numbers: Number): SFun<X> =
    invoke(bindings.zip(numbers.map { wrap(it) }))

  override operator fun invoke(vararg funs: Fun<X>): SFun<X> =
    invoke(bindings.zip(funs.toList()))

  override operator fun invoke(vararg ps: FunToAny<X>): SFun<X> =
    invoke(ps.toList().bind())

  open fun d(v1: SVar<X>): SFun<X> =
    Derivative(this, v1)//.let { if (EAGER) it.df() else it }

  open fun sin(): SFun<X> = Sine(this)
  open fun cos(): SFun<X> = Cosine(this)
  open fun tan(): SFun<X> = Tangent(this)
  fun exp(): SFun<X> = Power(E, this)

  open fun <L: D1> d(vVar: VVar<X, L>): VFun<X, L> =
    Gradient(this, vVar)//.let { if(EAGER) it.df() else it }

  open fun <R: D1, C: D1> d(mVar: MVar<X, R, C>): MFun<X, R, C> =
    MGradient(this, mVar)//.let { if(EAGER) it.df() else it }

  open fun grad(): Map<SVar<X>, SFun<X>> =
    bindings.sVars.associateWith { Derivative(this, it) }

  fun ln() = log(E)
  override fun log(base: SFun<X>): SFun<X> = Log(this, base)
  override fun pow(exponent: SFun<X>): SFun<X> = Power(this, exponent)
  override fun unaryMinus(): SFun<X> = Negative(this)
  override fun unaryPlus(): SFun<X> = this
  open fun sqrt(): SFun<X> = this pow (ONE / TWO)

  operator fun div(divisor: Number): SFun<X> = this / wrap(divisor)
  operator fun plus(addend: Number): SFun<X> = this + wrap(addend)
  operator fun minus(subtrahend: Number): SFun<X> = this - wrap(subtrahend)
  operator fun times(multiplicand: Number): SFun<X> = this * wrap(multiplicand)
  infix fun pow(exp: Number): SFun<X> = this pow wrap(exp)

  override fun toString() = when (this) {
    is SVar -> name
    is SComposition -> "($input)$bindings"
    else -> asString()
  }

  // TODO: Implement symbolic dis/unification
  override fun equals(other: Any?) = super.equals(other)
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
class Log<X: SFun<X>>(override val left: SFun<X>, override val right: SFun<X> = E()): SFun<X>(left, right), BiFun<X>

class Derivative<X: SFun<X>> constructor(
  override val input: SFun<X>,
  override val vrb: SVar<X>
): SFun<X>(input, vrb), Grad<X> {
  fun df() = input.df()

  @Suppress("UNCHECKED_CAST")
  fun SFun<X>.df(): SFun<X> = when (this@df) {
    is SVar -> if (this == vrb) ONE else ZERO
    is SConst -> ZERO
    is Sum -> left.df() + right.df()
    is Prod -> left.df() * right + left * right.df()
    is Power ->
      //https://en.wikipedia.org/wiki/Differentiation_rules#The_polynomial_or_elementary_power_rule
      if (right.isConstant()) right * left.pow(right - ONE) * left.df()
      //https://en.wikipedia.org/wiki/Differentiation_rules#Generalized_power_rule
      else this * (left.df() * right / left + right.df() * left.ln())
    is Negative -> -input.df()
    is Log -> (left pow -ONE) * left.df()
    is Sine -> input.cos() * input.df()
    is Cosine -> -input.sin() * input.df()
    is Tangent -> (input.cos() pow -TWO) * input.df()
    is Derivative -> input.df().df()
    is DProd -> (left.d(vrb) as VFun<X, DN> dot right as VFun<X, DN>) + (left as VFun<X, DN> dot right.d(vrb))
    is SComposition -> evaluate.df()
    is VSumAll<X, *> -> input.d(vrb).sum()
//    is Custom<X> -> fn.df()
  }
}

class SComposition<X: SFun<X>> constructor(
  override val input: SFun<X>,
  arguments: Bindings<X> = Bindings(input)
): SFun<X>(input), UnFun<X> {
  override val bindings: Bindings<X> = input.bindings + arguments
  val evaluate: SFun<X> by lazy { bind(bindings) }

  // TODO: Look into deep recursion so that we don't blow the stack
  // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-deep-recursive-function
  // https://medium.com/@elizarov/deep-recursion-with-coroutines-7c53e15993e3

  fun SFun<X>.bind(bnds: Bindings<X>): SFun<X> =
    bnds[this@bind] ?: when (this@bind) {
      is Derivative -> df().bind(bnds)
      // TODO: should we really be merging bindings for nested composition?
      is SComposition -> input.bind(bnds + bindings)
      else -> apply(*inputs.map {
        if (it is SFun<X>) it.bind(bnds) else it(bnds)
      }.toTypedArray())
    } //.also { result -> bnds.checkForUnpropagatedVariables(this@bind, result) }
  //appl(*inputs.map { it.bind(bnds) }.toTypedArray())) as SFun<X>
}

class DProd<X: SFun<X>> constructor(
  override val left: VFun<X, *>,
  override val right: VFun<X, *>
): SFun<X>(left, right), BiFun<X>

class VSumAll<X: SFun<X>, E: D1>
constructor(val input: VFun<X, E>): SFun<X>(input), PolyFun<X> {
  override val inputs: Array<out Fun<X>> = arrayOf(input)
}

class SVar<X: SFun<X>>(override val name: String = ""): Variable<X>, SFun<X>() {
  constructor(x: X, name: String = ""): this(name)

  override val bindings: Bindings<X> = Bindings(mapOf(this to this))
  override fun equals(other: Any?) = other is SVar<*> && name == other.name
  override fun hashCode(): Int = name.hashCode()
  operator fun getValue(thisRef: Any?, property: KProperty<*>) =
    SVar<X>(name.ifEmpty { property.name })
}

open class SConst<X: SFun<X>>
constructor(open val value: Number): SFun<X>(), Constant<X> {
  override fun toString() = value.toString()
  override fun equals(other: Any?) =
    if (other is SConst<*>) value == other.value else super.equals(other)

  override fun hashCode() = value.hashCode()

  open val doubleValue: Double by lazy { value.toDouble() }

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

  // TODO: Think more carefully about how to do this for other number systems.
  // Might need to push down to implementation when trigonometry is undefined.
  override fun sin() = wrap(sin(doubleValue))
  override fun cos() = wrap(cos(doubleValue))
  override fun tan() = wrap(tan(doubleValue))
  override fun sqrt() = wrap(sqrt(doubleValue))

  override fun unaryMinus() = wrap(-doubleValue)

  /**
   * Constant propagation.
   */

  override fun log(base: SFun<X>) = when (base) {
    is SConst<X> -> wrap(log(doubleValue, base.doubleValue))
    else -> super.log(base)
  }

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

sealed class Special<X: SFun<X>>(override val value: Number): SConst<X>(value) {
  override fun toString() = this::class.simpleName!!
}

class It<X: SFun<X>>: Special<X>(NaN)
class Zero<X: SFun<X>>: Special<X>(0.0)
class One<X: SFun<X>>: Special<X>(1.0)
class Two<X: SFun<X>>: Special<X>(2.0)
class E<X: SFun<X>>: Special<X>(E)

// TODO: RationalNumber, ComplexNumber, Quaternion
abstract class RealNumber<X: RealNumber<X, Y>, Y: Number>
constructor(override val value: Y): SConst<X>(value) {
  override fun toString() = value.toString()
  abstract override fun wrap(number: Number): SConst<X>
}

open class DReal(override val value: Double): RealNumber<DReal, Double>(value) {
  override fun wrap(number: Number) = DReal(number.toDouble())

  companion object: DReal(NaN)
}

/**
 * Extensions to convert numerical types from host language to eDSL.
 */

fun <X: RealNumber<X, *>> X.Var(): SVar<X> = SVar()
fun <E: D1, X: RealNumber<X, *>> X.Var(e: Nat<E>): VVar<X, E> = VVar(e)
fun <R: D1, C: D1, X: RealNumber<X, *>> X.Var(r: Nat<R>, c: Nat<C>): MVar<X, R, C> = MVar(r, c)

fun <R: D1, C: D1, X: RealNumber<X, *>> X.Mat(r: Nat<R>, c: Nat<C>, gen: (Int, Int) -> Any): Mat<X, R, C> =
  (0 until r.i).map { i -> (0 until c.i).map { j -> wrapOrError(gen(i, j)) as SFun<X> } }
    .map { Vec<X, C>(it) }.let { Mat(it) }

fun <E: D1, X: RealNumber<X, *>> X.Vec(e: Nat<E>, gen: (Int) -> Any): Vec<X, E> =
  Vec(List(e.i) { wrapOrError(gen(it)) as SFun<X> })

operator fun <X: RealNumber<X, *>> Number.times(multiplicand: SFun<X>) = multiplicand.wrap(this) * multiplicand
operator fun <X: RealNumber<X, *>, E: D1> Number.times(multiplicand: VFun<X, E>): VFun<X, E> = multiplicand.wrap(this) * multiplicand
operator fun <X: RealNumber<X, *>, R: D1, C: D1> Number.times(multiplicand: MFun<X, R, C>): MFun<X, R, C> = multiplicand.wrap(this) * multiplicand
operator fun <X: RealNumber<X, *>> Number.div(divisor: SFun<X>) = divisor.wrap(this) / divisor
operator fun <X: RealNumber<X, *>> Number.plus(addend: SFun<X>) = addend.wrap(this) + addend
operator fun <X: RealNumber<X, *>> Number.minus(subtrahend: SFun<X>) = subtrahend.wrap(this) - subtrahend

fun <X: RealNumber<X, *>> Number.pow(exp: SFun<X>) = exp.wrap(this) pow exp
fun <X: RealNumber<X, *>> pow(base: SFun<X>, exp: Number) = base pow base.wrap(exp)

fun <X: RealNumber<X, *>> SFun<X>.toDouble() =
  try {
    (this as SConst<X>).doubleValue
  } catch (e: ClassCastException) {
    show("before")
    e.printStackTrace()
    throw NumberFormatException("Scalar function ${javaClass.simpleName} has unbound free variables: ${bindings.allFreeVariables.keys}: $this")
  }

fun <X: SFun<X>> d(fn: SFun<X>) = Differential(fn)
class IndVar<X: SFun<X>> constructor(val fn: SFun<X>)

class Differential<X: SFun<X>>(private val fx: SFun<X>) {
  // TODO: ensure correctness for arbitrary nested functions using the Chain rule
  infix operator fun div(arg: Differential<X>) = fx.d(arg.fx.bindings.sVars.first())
}

//TODO: Figure out how to incorporate functions R -> R into type system
//@JvmName("infxsin") fun sin(angle: SFun<DReal>): SFun<DReal> = angle.sin()
//@JvmName("infxcos") fun cos(angle: SFun<DReal>): SFun<DReal> = angle.cos()
//@JvmName("infxtan") fun tan(angle: SFun<DReal>): SFun<DReal> = angle.tan()
//@JvmName("psfxsin") fun SFun<DReal>.sin(): SFun<DReal> = Sine(this)
//@JvmName("psfxcos") fun SFun<DReal>.cos(): SFun<DReal> = Cosine(this)
//@JvmName("psfxtan") fun SFun<DReal>.tan(): SFun<DReal> = Tangent(this)
//
//sealed class TrigFun
//constructor(override val input: SFun<DReal>):
//  SFun<DReal>(input), UnFun<DReal> {
//  override fun d(v: SVar<DReal>) = when (this) {
//    is Sine -> input.cos() * input.d(v)
//    is Cosine -> -input.sin() * input.d(v)
//    is Tangent -> (input.cos() pow -TWO) * input.d(v)
//  }
//}
//
//class Sine(override val input: SFun<DReal>): TrigFun(input)
//class Cosine(override val input: SFun<DReal>): TrigFun(input)
//class Tangent(override val input: SFun<DReal>): TrigFun(input)

fun <T: SFun<T>> sin(angle: SFun<T>) = angle.sin()
fun <T: SFun<T>> cos(angle: SFun<T>) = angle.cos()
fun <T: SFun<T>> tan(angle: SFun<T>) = angle.tan()
fun <T: SFun<T>> exp(exponent: SFun<T>) = exponent.exp()
fun <T: SFun<T>> sqrt(radicand: SFun<T>) = radicand.sqrt()

operator fun Number.invoke(vararg numbers: Number) = this