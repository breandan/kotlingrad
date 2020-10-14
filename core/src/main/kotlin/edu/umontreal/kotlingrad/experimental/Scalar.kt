@file:Suppress("FunctionName", "LocalVariableName", "unused", "UNUSED_VARIABLE")

package edu.umontreal.kotlingrad.experimental

import edu.mcgill.kaliningraph.circuits.*
import java.io.Serializable
import kotlin.math.*
import kotlin.reflect.KProperty

/**
 * Algebraic primitives.
 */

interface Group<X: Group<X>> {
  operator fun unaryMinus(): X
  operator fun unaryPlus(): X
  operator fun plus(addend: X): X
  operator fun minus(subtrahend: X): X = this + -subtrahend
}

interface Field<X: Field<X>>: Group<X> {
  operator fun div(divisor: X): X
  operator fun times(multiplicand: X): X
  infix fun pow(exponent: X): X
  fun log(base: X): X
}


interface Fun<X: SFun<X>>: (Bindings<X>) -> Fun<X>, Serializable {
  val inputs: Array<out Fun<X>>
  val bindings: Bindings<X>
    get() = Bindings(*inputs)
  val op: Op

  val proto: X
    get() = bindings.proto

  fun isConstant(): Boolean = bindings.allVars.isEmpty()
  fun wrap(number: Number): SConst<X> = SConst(number.toDouble())

  override operator fun invoke(newBindings: Bindings<X>): Fun<X>
  operator fun invoke(vararg numbers: Number): Fun<X> = invoke(bindings.zip(numbers.map { wrap(it) }))
  operator fun invoke(vararg funs: Fun<X>): Fun<X> = invoke(bindings.zip(funs.toList()))
  operator fun invoke(vararg ps: Pair<Fun<X>, Any>): Fun<X> = invoke(ps.toList().bind())

  fun toGate(): Gate = when (this) {
    is NilFun -> Gate.wrap(this)
    is UnFun -> Gate(op, input.toGate())
    is BiFun -> Gate(op, left.toGate(), right.toGate())
    is PolyFun -> Gate(op, *inputs.map { it.toGate() }.toTypedArray())
    else -> TODO()
  }

  fun List<Pair<Fun<X>, Any>>.bind() = Bindings(map { it.first to wrapOrError(it.second) }.toMap())

  fun wrapOrError(any: Any): Fun<X> = when (any) {
    is Fun<*> -> any as Fun<X>
    is Number -> wrap(any)
    else -> throw NumberFormatException("Invoke expects a number or function but got: $any")
  }

  fun asString() = when (this) {
    is Constant -> "$this"
    is Variable -> "Var($name)"
    is Grad -> "d($input) / d($vrb)"
    is SComposition -> "($input)$bindings"
    is BiFun<*> -> "($left) $op ($right)"
    is UnFun<*> -> "$op($input)"
    is PolyFun<*> -> "$op$inputs"
    else -> javaClass.simpleName
  }
}

// https://arxiv.org/pdf/2001.02209.pdf

interface BiFun<X: SFun<X>>: PolyFun<X> {
  val left: Fun<X>
  val right: Fun<X>
//  val fn: (Fun<X>, Fun<X>) -> Fun<X>
}

interface NilFun<X: SFun<X>>: Fun<X>

interface UnFun<X: SFun<X>>: PolyFun<X> {
  val input: Fun<X>
}

interface PolyFun<X: SFun<X>>: Fun<X> {
  override val inputs: Array<out Fun<X>>
}

interface Grad<X : SFun<X>> : BiFun<X> {
  val input: Fun<X>
  val vrb: Variable<X>
  override val left: Fun<X>
    get() = input
  override val right: Fun<X>
    get() = vrb
}

interface Variable<X : SFun<X>> : Fun<X>, NilFun<X> { val name: String; override val proto: X }
interface Constant<X: SFun<X>>: Fun<X>, NilFun<X>

/**
 * Scalar function.
 *
 * TODO: Implement proper monad like [java.util.function.Function] or [Function]
 */

sealed class SFun<X: SFun<X>> constructor(override vararg val inputs: Fun<X>): PolyFun<X>, Field<SFun<X>> {
  val ZERO: Special<X> by lazy { Zero() }
  val ONE: Special<X> by lazy { One() }
  val TWO: Special<X> by lazy { Two() }
  val E: Special<X> by lazy { E<X>() }

  val x by lazy { SVar(proto, "x") }
  val y by lazy { SVar(proto, "y") }
  val z by lazy { SVar(proto, "z") }

  override fun plus(addend: SFun<X>): SFun<X> = Sum(this, addend)
  override fun times(multiplicand: SFun<X>): SFun<X> = Prod(this, multiplicand)
  override fun div(divisor: SFun<X>): SFun<X> = this * divisor.pow(-ONE)
  open operator fun <E: D1> times(multiplicand: VFun<X, E>): VFun<X, E> = SVProd(this, multiplicand)
  open operator fun <R: D1, C: D1> times(multiplicand: MFun<X, R, C>): MFun<X, R, C> = SMProd(this, multiplicand)

  // TODO: Paramaterize OP instead of using subtyping
  class AOP<X: SFun<X>>(val op: Op, val of: (Fun<X>) -> SFun<X>)
  // TODO: Implement proper curry
  fun apply(op: Op): (SFun<X>) -> SFun<X> = when(op) {
    Dyad.`+` -> { it: SFun<X> -> this + it }
    Dyad.`*` -> { it: SFun<X> -> this * it }
    else -> TODO()
  }

  fun forwardApply(vararg xs: SFun<X>): SFun<X> = when(op) {
    Monad.id -> xs[0]
    Monad.sin -> xs[0].sin()
    Monad.cos -> xs[0].cos()
    Monad.tan -> xs[0].tan()
    Monad.`-` -> -xs[0]
    Monad.`+` -> xs[0]
    Dyad.`+` -> xs[0] + xs[1]
    Dyad.`*` -> xs[0] * xs[1]
    Dyad.pow  -> xs[0] pow xs[1]
    Dyad.log  -> xs[0].log(xs[1])
    else -> TODO()
  }

  override fun invoke(newBindings: Bindings<X>): SFun<X> =
    SComposition(this, newBindings)
      .run { if (bindings.complete || newBindings.readyToBind || EAGER) evaluate else this }

  operator fun invoke(): SFun<X> = SComposition(this).evaluate

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

  override fun toString() =  when(this) {
    is SVar -> name
    is SComposition -> "($input)$bindings"
    else -> asString()
  }

  override val op: Op = when (this) {
    is Sine -> Monad.sin
    is Cosine -> Monad.cos
    is Tangent -> Monad.tan
    is Negative -> Monad.`-`

    is Sum -> Dyad.`+`
    is Prod -> Dyad.`*`
    is Power -> Dyad.pow
    is Log -> Dyad.log
    is Derivative -> Dyad.d
    is DProd -> Dyad.dot

    is SComposition -> Polyad.λ
    is VSumAll<X, *> -> Polyad.Σ
    else -> Monad.id
  }

  override operator fun invoke(vararg numbers: Number): SFun<X> = invoke(bindings.zip(numbers.map { wrap(it) }))
  override operator fun invoke(vararg funs: Fun<X>): SFun<X> = invoke(bindings.zip(funs.toList()))
  override operator fun invoke(vararg ps: Pair<Fun<X>, Any>): SFun<X> = invoke(ps.toList().bind())
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

class Derivative<X: SFun<X>>(override val input: SFun<X>, override val vrb: SVar<X>): SFun<X>(input, vrb), Grad<X> {
  fun df() = input.df()
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
    is Derivative -> input.df()
    is DProd -> (left.d(vrb) as VFun<X, D1> dot right as VFun<X, D1>) + (left as VFun<X, D1> dot right.d(vrb))
    is SComposition -> evaluate.df()
    is VSumAll<X, *> -> input.d(vrb).sum()
//    is Custom<X> -> fn.df()
  }
}

// TODO: Unit test this data structure
class SComposition<X : SFun<X>>(
  override val input: SFun<X>,
  arguments: Bindings<X> = Bindings(input.proto)
) : SFun<X>(input), UnFun<X> {
  override val bindings: Bindings<X> = input.bindings + arguments
  val evaluate: SFun<X> by lazy { bind(bindings) }

  @Suppress("UNCHECKED_CAST")
  // TODO: Make this tail recursive so that we don't blow the stack
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
      is Log -> left.bind(bnds).log(right.bind(bnds))
      is Derivative -> df().bind(bnds)
      is DProd -> left(bnds) as VFun<X, D1> dot right(bnds) as VFun<X, D1>
      is SComposition -> input.bind(bnds + bindings)
      is VSumAll<X, *> -> input(bnds).sum()
    } //.also { result -> bnds.checkForUnpropagatedVariables(this@bind, result) }
    //appl(*inputs.map { it.bind(bnds) }.toTypedArray())) as SFun<X>
}

class DProd<X: SFun<X>>(override val left: VFun<X, *>, override val right: VFun<X, *>): SFun<X>(left, right), BiFun<X>

class VSumAll<X: SFun<X>, E: D1>(val input: VFun<X, E>): SFun<X>(input), PolyFun<X>{
  override val inputs: Array<out Fun<X>> = arrayOf(input)
}

class SVar<X: SFun<X>>(override val proto: X, override val name: String = ""): Variable<X>, SFun<X>() {
  override val bindings: Bindings<X> = Bindings(mapOf(this to this))
  override fun equals(other: Any?) = other is SVar<*> && name == other.name
  override fun hashCode(): Int = name.hashCode()
  operator fun getValue(thisRef: Any?, property: KProperty<*>) =
    SVar(proto, if (name.isEmpty()) property.name else name)
}

open class SConst<X: SFun<X>> constructor(open val value: Number? = null): SFun<X>(), Constant<X> {
  override fun toString() = value.toString()
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

sealed class Special<X: SFun<X>>: SConst<X>() {
  override fun toString() = javaClass.simpleName
  override fun equals(other: Any?) =
    if (this === other) true else javaClass == other?.javaClass

  override fun hashCode(): Int = when (this) {
    is Zero -> 0
    is One -> 1
    is Two -> 2
    is E -> 3
  }
}

class Zero<X: SFun<X>>: Special<X>()
class One<X: SFun<X>>: Special<X>()
class Two<X: SFun<X>>: Special<X>()
class E<X: SFun<X>>: Special<X>()

abstract class RealNumber<X: RealNumber<X, Y>, Y: Number>(override val value: Y): SConst<X>() {
  override fun toString() = value.toString()// "${javaClass.name.substringAfterLast(".").substringBefore("$")}($value)"
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
  override val proto by lazy { this }
}

/**
 * Numerical context. Converts numerical types from host language to eDSL.
 */

abstract class Protocol<X: RealNumber<X, *>>(val prototype: X) {
  val x = prototype.x
  val y = prototype.y
  val z = prototype.z
  open val variables = listOf(x, y, z)

  fun wrap(number: Number): SConst<X> = prototype.wrap(number)

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

  inline fun <R: D1, C: D1, Y: Number> Mat(r: Nat<R>, c: Nat<C>, gen: (Int, Int) -> Y): MConst<X, R, C> =
//    Mat(List(r.i) { row -> Vec(List(c.i) { col -> wrap(gen(row, col)) }) })
    (0 until r.i).map { i -> (0 until c.i).map { j -> wrap(gen(i, j)) } }
      .map { VConst<X, C>(*it.toTypedArray()) }.toTypedArray().let { MConst(*it) }

  inline fun <reified E: D1> Vec(e: Nat<E>, gen: (Int) -> Any): Vec<X, E> =
    Vec(List(e.i) { prototype.wrapOrError(gen(it)) as SFun<X> })

  fun Var(name: String = "") = SVar(prototype, name)
  fun Var2(name: String = "") = VVar<X, D2>(prototype, name, D2)
  fun Var3(name: String = "") = VVar<X, D3>(prototype, name, D3)
  fun Var4(name: String = "") = VVar<X, D4>(prototype, name, D4)
  fun Var5(name: String = "") = VVar<X, D5>(prototype, name, D5)
  fun Var6(name: String = "") = VVar<X, D6>(prototype, name, D6)
  fun Var7(name: String = "") = VVar<X, D7>(prototype, name, D7)
  fun Var8(name: String = "") = VVar<X, D8>(prototype, name, D8)
  fun Var9(name: String = "") = VVar<X, D9>(prototype, name, D9)
  fun Var10(name: String = "") = VVar<X, D10>(prototype, name, D10)
  fun Var11(name: String = "") = VVar<X, D11>(prototype, name, D11)
  fun Var12(name: String = "") = VVar<X, D12>(prototype, name, D12)
  fun Var13(name: String = "") = VVar<X, D13>(prototype, name, D13)
  fun Var14(name: String = "") = VVar<X, D14>(prototype, name, D14)
  fun Var15(name: String = "") = VVar<X, D15>(prototype, name, D15)
  fun Var16(name: String = "") = VVar<X, D16>(prototype, name, D16)
  fun Var17(name: String = "") = VVar<X, D17>(prototype, name, D17)
  fun Var18(name: String = "") = VVar<X, D18>(prototype, name, D18)
  fun Var19(name: String = "") = VVar<X, D19>(prototype, name, D19)
  fun Var20(name: String = "") = VVar<X, D20>(prototype, name, D20)
  fun Var21(name: String = "") = VVar<X, D21>(prototype, name, D21)
  fun Var22(name: String = "") = VVar<X, D22>(prototype, name, D22)
  fun Var23(name: String = "") = VVar<X, D23>(prototype, name, D23)
  fun Var24(name: String = "") = VVar<X, D24>(prototype, name, D24)
  fun Var25(name: String = "") = VVar<X, D25>(prototype, name, D25)
  fun Var26(name: String = "") = VVar<X, D26>(prototype, name, D26)
  fun Var27(name: String = "") = VVar<X, D27>(prototype, name, D27)
  fun Var28(name: String = "") = VVar<X, D28>(prototype, name, D28)
  fun Var29(name: String = "") = VVar<X, D29>(prototype, name, D29)
  fun Var30(name: String = "") = VVar<X, D30>(prototype, name, D30)

  fun Var2x1(name: String = "") = MVar<X, D2, D1>(prototype, name, D2, D1)
  fun Var2x2(name: String = "") = MVar<X, D2, D2>(prototype, name, D2, D2)
  fun Var2x3(name: String = "") = MVar<X, D2, D3>(prototype, name, D2, D3)
  fun Var3x1(name: String = "") = MVar<X, D3, D1>(prototype, name, D3, D1)
  fun Var3x2(name: String = "") = MVar<X, D3, D2>(prototype, name, D3, D2)
  fun Var3x3(name: String = "") = MVar<X, D3, D3>(prototype, name, D3, D3)
  fun Var5x5(name: String = "") = MVar<X, D5, D5>(prototype, name, D5, D5)
  fun Var9x9(name: String = "") = MVar<X, D9, D9>(prototype, name, D9, D9)
}

operator fun <X: RealNumber<X, *>> Number.times(multiplicand: SFun<X>) = multiplicand.wrap(this) * multiplicand
operator fun <X: RealNumber<X, *>, E: D1> Number.times(multiplicand: VFun<X, E>): VFun<X, E> = multiplicand.wrap(this) * multiplicand
operator fun <X: RealNumber<X, *>, R: D1, C: D1> Number.times(multiplicand: MFun<X, R, C>): MFun<X, R, C> = multiplicand.wrap(this) * multiplicand
operator fun <X: RealNumber<X, *>> Number.div(divisor: SFun<X>) = divisor.wrap(this) / divisor
operator fun <X: RealNumber<X, *>> Number.plus(addend: SFun<X>) = addend.wrap(this) + addend
operator fun <X: RealNumber<X, *>> Number.minus(subtrahend: SFun<X>) = subtrahend.wrap(this) - subtrahend

fun <X: RealNumber<X, *>> Number.pow(exp: SFun<X>) = exp.wrap(this) pow exp
@JvmName("prefixPowNum") fun <X: RealNumber<X, *>> pow(base: SFun<X>, exp: Number) = base pow base.wrap(exp)
@JvmName("prefixPowFun") fun <X: RealNumber<X, *>> pow(base: Number, exp: SFun<X>) = exp.wrap(base) pow exp

fun <X: RealNumber<X, *>> SFun<X>.toDouble() =
  try {
    (this as SConst<X>).doubleValue
  } catch(e: ClassCastException) {
    show("before")
    e.printStackTrace()
    throw NumberFormatException("Scalar function ${javaClass.simpleName} has unbound free variables: ${bindings.allFreeVariables.keys}")
  }

fun <X: SFun<X>> d(fn: SFun<X>) = Differential(fn)
class IndVar<X: SFun<X>> constructor(val fn: SFun<X>)

class Differential<X: SFun<X>>(private val fx: SFun<X>) {
  // TODO: ensure correctness for arbitrary nested functions using the Chain rule
  infix operator fun div(arg: Differential<X>) = fx.d(arg.fx.bindings.sVars.first())
}

fun <T: SFun<T>> sin(angle: SFun<T>) = angle.sin()
fun <T: SFun<T>> cos(angle: SFun<T>) = angle.cos()
fun <T: SFun<T>> tan(angle: SFun<T>) = angle.tan()
fun <T: SFun<T>> exp(exponent: SFun<T>) = exponent.exp()
fun <T: SFun<T>> sqrt(radicand: SFun<T>) = radicand.sqrt()

operator fun Number.invoke(n: Number) = this

object DoublePrecision: Protocol<DReal>(DReal)
object BigDecimalPrecision: Protocol<BDReal>(BDReal)