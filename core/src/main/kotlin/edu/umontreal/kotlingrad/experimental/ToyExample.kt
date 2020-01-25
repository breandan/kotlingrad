@file:Suppress("FunctionName", "LocalVariableName", "unused", "UNUSED_VARIABLE")

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

    val r = x + 1
    val m = x + 2
    val s = x + 3
    val ro_mos = r(x to m(x to s))
    val rom_os = r(x to m)(x to s)

    val i = 0
    println("r ∘ (m ∘ s) ∘ $i = $ro_mos ∘ $i = ${ro_mos(x to 0)}")
    println("(r ∘ m) ∘ s ∘ $i = $rom_os ∘ $i = ${rom_os(x to 0)}")
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

interface Fun<X: SFun<X>> {
  val bindings: Bindings<X>
}

// Supports arbitrary subgraph reassignment but usually just holds variable-to-value bindings
@Suppress("UNCHECKED_CAST")
data class Bindings<X: SFun<X>>(val fMap: Map<Fun<X>, Fun<X>> = mapOf()) {
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

  // Scalar, vector, and matrix variables
  val sVars: Set<Var<X>> = sMap.keys.filterIsInstance<Var<X>>().toSet()
  val vVars: Set<VVar<X, *>> = vMap.keys.filterIsInstance<VVar<X, *>>().toSet()
  val mVars: Set<MVar<X, *, *>> = mMap.keys.filterIsInstance<MVar<X, *, *>>().toSet()

  val isReassignmentFree = sVars.map { fMap[it]!! }.filterIsInstance<Var<X>>().isEmpty()
  fun fullyDetermines(fn: SFun<X>) = fn.bindings.sVars.all { it in fMap }
  override fun toString() = fMap.toString()
  operator fun contains(v: Var<X>) = v in fMap
  fun curried() = fMap.entries.map { Bindings(mapOf(it.key to it.value)) }

  operator fun get(fn: SConst<X>) = fn
  operator fun get(fn: SFun<X>): SFun<X> = sMap.getOrElse(fn) { fn }
  operator fun <L: D1> get(fn: VFun<X, L>): VFun<X, L> = vMap.getOrElse(fn) { fn } as VFun<X, L>
  operator fun <R: D1, C: D1> get(fn: MFun<X, R, C>): MFun<X, R, C> = mMap.getOrElse(fn) { fn } as MFun<X, R, C>
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
  operator fun <L: D1> invoke(pair: Pair<VFun<X, L>, VFun<X, L>>): SFun<X> = invoke(Bindings(mapOf(pair)))

  @JvmName("mFunReassign")
  operator fun <R: D1, C: D1> invoke(pair: Pair<MFun<X, R, C>, MFun<X, R, C>>): SFun<X> = invoke(Bindings(mapOf(pair)))

  open operator fun invoke(): SFun<X> = invoke(Bindings())

  open fun d(v1: Var<X>): SFun<X> = Derivative(this, v1)
  open fun d(v1: Var<X>, v2: Var<X>): Vec<X, D2> = Vec(Derivative(this, v1), Derivative(this, v2))
  open fun d(v1: Var<X>, v2: Var<X>, v3: Var<X>): Vec<X, D3> = Vec(Derivative(this, v1), Derivative(this, v2), Derivative(this, v3))
  open fun d(vararg vars: Var<X>): Map<Var<X>, SFun<X>> = vars.map { it to Derivative(this, it) }.toMap()

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
  override fun pow(exp: SFun<X>): SFun<X> = Power(this, exp)
  override fun unaryMinus(): SFun<X> = Negative(this)
  open fun sqrt(): SFun<X> = this pow (ONE / TWO)

  override fun toString(): String = when (this) {
    is Log -> "ln($logarithmand)"
    is Negative -> "- ($value)"
    is Power -> "($base).pow($exponent)"
    is Prod -> "($left) * ($right)"
    is Sum -> if(right is Negative) "$left $right" else "$left + $right"
    is Var -> name
    is Derivative -> "d($fn) / d($vrb)"
    is Special -> javaClass.simpleName
    is BiFun<*> -> "($left) $opStr ($right)"
    is UnFun<*> -> "$opStr($input)"
    is VMagnitude -> "|$value|"
    is DProd -> "($left) dot ($right)"
    is Composition -> "($fn)$inputs"
    else -> super.toString()
  }

  val opStr: String = when (this) {
    is Log -> "ln"
    is Negative -> "-"
    is Power -> "pow"
    is Prod -> "×"
    is Sum -> "+"
    is Derivative -> "d"
    is Sine -> "sin"
    is Cosine -> "cos"
    is Tangent -> "tan"
    else -> super.toString()
  }

  fun toGraph(): MutableNode = mutNode(if (this is Var) "$this" else "${hashCode()}").apply {
    when (this@SFun) {
      is Var -> name
      is Derivative -> { fn.toGraph() - this; mutNode("$this").apply { add(Label.of(vrb.toString())) } - this; add(Label.of("d")) }
      is BiFun -> { left.toGraph() - this; right.toGraph() - this; add(Label.of(opStr)) }
      is UnFun -> { input.toGraph() - this; add(Label.of(opStr)) }
      is RealNumber<*, *> -> add(Label.of("$value"))
      is Special -> add(Label.of(this@SFun.toString()))
      is Composition -> { bindings.sMap.entries.map { entry -> mutNode(entry.hashCode().toString()).also { compNode -> entry.key.toGraph() - compNode; entry.value.toGraph() - compNode; compNode.add(Label.of("comp")) } }.map { it - this; add(Label.of("bindings")) } }
      else -> TODO(this@SFun.javaClass.toString())
    }
  }
}

/**
 * Symbolic operators.
 */

sealed class BiFun<X: SFun<X>>(val left: SFun<X>, val right: SFun<X>): SFun<X>(left, right)
sealed class UnFun<X: SFun<X>>(val input: SFun<X>): SFun<X>(input)

class Sine<X: SFun<X>>(val angle: SFun<X>): UnFun<X>(angle)
class Cosine<X: SFun<X>>(val angle: SFun<X>): UnFun<X>(angle)
class Tangent<X: SFun<X>>(val angle: SFun<X>): UnFun<X>(angle)
class Negative<X : SFun<X>>(val value: SFun<X>) : UnFun<X>(value)
class Sum<X : SFun<X>>(addend: SFun<X>, augend: SFun<X>): BiFun<X>(addend, augend)
class Prod<X : SFun<X>>(multiplicand: SFun<X>, multiplicator: SFun<X>): BiFun<X>(multiplicand, multiplicator)
class Power<X : SFun<X>> internal constructor(val base: SFun<X>, val exponent: SFun<X>) : BiFun<X>(base, exponent)
class Log<X : SFun<X>> internal constructor(val logarithmand: SFun<X>, val base: SFun<X> = E()) : BiFun<X>(logarithmand, base)

class Derivative<X : SFun<X>>(val fn: SFun<X>, val vrb: Var<X>) : SFun<X>(fn, vrb) {
  fun SFun<X>.df(): SFun<X> = when (this@df) {
    is Var -> if (this == vrb) ONE else ZERO
    is SConst -> ZERO
    is Sum -> left.df() + right.df()
    is Prod -> left.df() * right + left * right.df()
    is Power -> this * (exponent * Log(base)).df()
    is Negative -> -value.df()
    is Log -> (logarithmand pow -ONE) * logarithmand.df()
    is Sine -> angle.cos() * angle.df()
    is Cosine -> -angle.sin() * angle.df()
    is Tangent -> (angle.cos() pow -TWO) * angle.df()
    is Derivative -> fn.df()
    is DProd -> this().df()
    is VMagnitude -> this().df()
    is Composition -> evaluate.df()
  }
}

// TODO: Unit test this data structure
class Composition<X : SFun<X>>(val fn: SFun<X>, val inputs: Bindings<X>) : SFun<X>(Bindings(fn.bindings, inputs)) {
  val evaluate by lazy { call() }
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

  fun SFun<X>.call(): SFun<X> = inputs.sMap.getOrElse(this@call) { bind() }

  @Suppress("UNCHECKED_CAST")
  fun SFun<X>.bind() = when (this@bind) {
    is Var -> this
    is SConst -> this
    is Prod -> left.call() * right.call()
    is Sum -> left.call() + right.call()
    is Power -> base.call() pow exponent.call()
    is Negative -> -value.call()
    is Sine -> angle.call().sin()
    is Cosine -> angle.call().cos()
    is Tangent -> angle.call().tan()
    is Log -> logarithmand.call().ln()
    is Derivative -> df().call()
    is DProd -> left(inputs) as Vec<X, D1> dot right(inputs) as Vec<X, D1>
    is VMagnitude -> value(inputs).magnitude()
    is Composition -> fn.call().call()
  }
}

class DProd<X: SFun<X>>(val left: VFun<X, *>, val right: VFun<X, *>): SFun<X>(left, right)

class VMagnitude<X: SFun<X>>(val value: VFun<X, *>): SFun<X>(value)//, value.vVars)

interface Variable { val name: String }

class Var<X : SFun<X>>(override val name: String = "") : Variable, SFun<X>() {
  override val bindings: Bindings<X> = Bindings(mapOf(this to this))
}

open class SConst<X : SFun<X>> : SFun<X>()
sealed class Special<X: SFun<X>> : SConst<X>() {
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

abstract class RealNumber<X : SFun<X>, Y>(open val value: Y) : SConst<X>() {
  override fun toString() = value.toString()
  abstract fun wrap(value: Number): X

  override val ZERO by lazy { wrap(0) }
  override val ONE by lazy { wrap(1) }
  override val TWO by lazy { wrap(2) }
  override val E by lazy { wrap(Math.E) }

  override fun <E : D1> times(multiplicand: VFun<X, E>) =
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
}

class DReal(override val value: Double) : RealNumber<DReal, Double>(value) {
  override fun wrap(value: Number) = DReal(value.toDouble())

  override fun sin() = DReal(sin(value))
  override fun cos() = DReal(cos(value))
  override fun tan() = DReal(tan(value))
  override fun sqrt() = DReal(sqrt(value))
  override fun unaryMinus() = DReal(-value)
  override fun ln() = DReal(ln(value))

  /**
   * Constant propagation.
   */

  override fun plus(addend: SFun<DReal>) = when (addend) {
    is DReal -> DReal(value + addend.value)
    else -> super.plus(addend)
  }

  override fun times(multiplicand: SFun<DReal>) = when (multiplicand) {
    is DReal -> DReal(value * multiplicand.value)
    else -> super.times(multiplicand)
  }

  override fun pow(exp: SFun<DReal>) = when (exp) {
    is DReal -> DReal(value.pow(exp.value))
    else -> super.pow(exp)
  }
}

/**
 * Numerical context.
 */

sealed class Protocol<X : SFun<X>> {
  val x = Var<X>("x")
  val y = Var<X>("y")
  val z = Var<X>("z")

  val variables = listOf(x, y, z)

  val zero = Zero<X>()
  val one = One<X>()
  val two = Two<X>()
  val e = E<X>()

  val constants: Map<Special<X>, Number> = mapOf(zero to 0, one to 1, two to 2, e to E)

  private fun <T: Map<Var<X>, Number>> T.bind() =
    Bindings((constants + this@bind).map { it.key to wrap(it.value) }.toMap())

  abstract fun wrap(number: Number): X
  fun <X: RealNumber<X, Y>, Y: Number> SFun<X>.unwap() = (this as X).value

  fun <X: RealNumber<X, Y>, Y: Number> SFun<X>.toDouble() = unwap().toDouble()

  fun <T: SFun<T>> sin(angle: SFun<T>) = angle.sin()
  fun <T: SFun<T>> cos(angle: SFun<T>) = angle.cos()
  fun <T: SFun<T>> tan(angle: SFun<T>) = angle.tan()
  fun <T: SFun<T>> exp(angle: SFun<T>) = angle.exp()

  class IndVar<X: SFun<X>> constructor(val fn: SFun<X>)

  class Differential<X: SFun<X>>(private val fx: SFun<X>) {
    // TODO: ensure correctness for arbitrary nested functions using the Chain rule
    infix operator fun div(arg: Differential<X>) = fx.d(arg.fx.bindings.sVars.first())
  }

  operator fun SFun<X>.invoke(vararg pairs: Pair<Var<X>, Number>) = this(pairs.toMap().bind())

  operator fun <E : D1> VFun<X, E>.invoke(vararg pairs: Pair<Var<X>, Number>) = this(pairs.toMap().bind())

  operator fun <R : D1, C: D1> MFun<X, R, C>.invoke(vararg pairs: Pair<Var<X>, Number>) = this(pairs.toMap().bind())

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

  fun Vec(d0: Number) = Vec(wrap(d0))
  fun Vec(d0: Number, d1: Number) = Vec(wrap(d0), wrap(d1))
  fun Vec(d0: Number, d1: Number, d2: Number) = Vec(wrap(d0), wrap(d1), wrap(d2))
  fun Vec(d0: Number, d1: Number, d2: Number, d3: Number) = Vec(wrap(d0), wrap(d1), wrap(d2), wrap(d3))
  fun Vec(d0: Number, d1: Number, d2: Number, d3: Number, d4: Number) = Vec(wrap(d0), wrap(d1), wrap(d2), wrap(d3), wrap(d4))
  fun Vec(d0: Number, d1: Number, d2: Number, d3: Number, d4: Number, d5: Number) = Vec(wrap(d0), wrap(d1), wrap(d2), wrap(d3), wrap(d4), wrap(d5))
  fun Vec(d0: Number, d1: Number, d2: Number, d3: Number, d4: Number, d5: Number, d6: Number) = Vec(wrap(d0), wrap(d1), wrap(d2), wrap(d3), wrap(d4), wrap(d5), wrap(d6))
  fun Vec(d0: Number, d1: Number, d2: Number, d3: Number, d4: Number, d5: Number, d6: Number, d7: Number) = Vec(wrap(d0), wrap(d1), wrap(d2), wrap(d3), wrap(d4), wrap(d5), wrap(d6), wrap(d7))
  fun Vec(d0: Number, d1: Number, d2: Number, d3: Number, d4: Number, d5: Number, d6: Number, d7: Number, d8: Number) = Vec(wrap(d0), wrap(d1), wrap(d2), wrap(d3), wrap(d4), wrap(d5), wrap(d6), wrap(d7), wrap(d8))

  fun Mat1x1(d0: Number) = Mat<X, D1, D1>(Vec(d0))
  fun Mat1x2(d0: Number, d1: Number) = Mat<X, D1, D2>(Vec(d0, d1))
  fun Mat1x3(d0: Number, d1: Number, d2: Number) = Mat<X, D1, D3>(Vec(d0, d1, d2))
  fun Mat2x1(d0: Number, d1: Number) = Mat<X, D2, D1>(Vec(d0), Vec(d1))
  fun Mat2x2(d0: Number, d1: Number, d2: Number, d3: Number) = Mat<X, D2, D2>(Vec(d0, d1), Vec(d2, d3))
  fun Mat2x3(d0: Number, d1: Number, d2: Number, d3: Number, d4: Number, d5: Number) = Mat<X, D2, D3>(Vec(d0, d1, d2), Vec(d3, d4, d5))
  fun Mat3x1(d0: Number, d1: Number, d2: Number) = Mat<X, D3, D1>(listOf(Vec(d0), Vec(d1), Vec(d2)) )
  fun Mat3x2(d0: Number, d1: Number, d2: Number, d3: Number, d4: Number, d5: Number) = Mat<X, D3, D2>(Vec(d0, d1), Vec(d2, d3), Vec(d4, d5))
  fun Mat3x3(d0: Number, d1: Number, d2: Number, d3: Number, d4: Number, d5: Number, d6: Number, d7: Number, d8: Number) = Mat<X, D3, D3>(Vec(d0, d1, d2), Vec(d3, d4, d5), Vec(d6, d7, d8))

  fun Var2() = Vec(Var<X>(), Var())
  fun Var3() = Vec(Var<X>(), Var(), Var())

  fun Var2x1() = Mat2x1(Var<X>(), Var())
  fun Var2x2() = Mat2x2(Var<X>(), Var(), Var(), Var())
  fun Var2x3() = Mat2x3(Var<X>(), Var(), Var(), Var(), Var(), Var())
  fun Var3x1() = Mat3x1(Var<X>(), Var(), Var())
  fun Var3x2() = Mat3x2(Var<X>(), Var(), Var(), Var(), Var(), Var())
  fun Var3x3() = Mat3x3(Var<X>(), Var(), Var(), Var(), Var(), Var(), Var(), Var(), Var())
}

object DoublePrecision : Protocol<DReal>() {
  override fun wrap(number: Number): DReal = DReal(number.toDouble())
}

object BigDecimalPrecision : Protocol<BDReal>() {
  override fun wrap(number: Number): BDReal = BDReal(number.toDouble())
}