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

interface Fun<X: SFun<X>> {
  val inputs: Inputs<X>
}

/**
 * TODO: Figure out how to merge with
 * @see Bindings
 */

data class Inputs<X: SFun<X>>(
  val sVars: Set<Var<X>> = emptySet(),
  val vVars: Set<VVar<X, *>> = emptySet(),
  val mVars: Set<MVar<X, *, *>> = emptySet()
) {
  constructor(inputs: List<Inputs<X>>): this(
    inputs.flatMap { it.sVars }.toSet(),
    inputs.flatMap { it.vVars }.toSet(),
    inputs.flatMap { it.mVars }.toSet()
  )

  constructor(vararg funs: Fun<X>): this(funs.map { it.inputs })
}

/**
 * Scalar function.
 */

sealed class SFun<X: SFun<X>>(override val inputs: Inputs<X>): Fun<X>, Field<SFun<X>>, (Bindings<X>) -> SFun<X> {
  constructor(vararg funs: Fun<X>) : this(Inputs(*funs))

  override operator fun plus(addend: SFun<X>): SFun<X> = Sum(this, addend)
  override operator fun times(multiplicand: SFun<X>): SFun<X> = Prod(this, multiplicand)
  override operator fun div(divisor: SFun<X>): SFun<X> = this * divisor.pow(-One<X>())
  open operator fun <E : D1> times(multiplicand: VFun<X, E>): VFun<X, E> = SVProd(this, multiplicand)
  open operator fun <R : D1, C : D1> times(multiplicand: MFun<X, R, C>): MFun<X, R, C> = SMProd(this, multiplicand)

  override operator fun invoke(bnds: Bindings<X>): SFun<X> =
      Composition(this, bnds).run { if (bnds.isReassignmentFree) evaluate else this }

  open operator fun invoke(): SFun<X> = invoke(Bindings())

  open fun d(v1: Var<X>): SFun<X> = Derivative(this, v1)
  open fun d(v1: Var<X>, v2: Var<X>): Vec<X, D2> = Vec(Derivative(this, v1), Derivative(this, v2))
  open fun d(v1: Var<X>, v2: Var<X>, v3: Var<X>): Vec<X, D3> = Vec(Derivative(this, v1), Derivative(this, v2), Derivative(this, v3))
  open fun d(vararg vars: Var<X>): Map<Var<X>, SFun<X>> = vars.map { it to Derivative(this, it) }.toMap()

  open fun <L: D1> d(vVar: VVar<X, L>): VFun<X, L> = Gradient(this, vVar)
  open fun <R: D1, C: D1> d(mVar: MVar<X, R, C>): MFun<X, R, C> = TODO()

  open fun <R : D1, C : D1> d(mv: Mat<X, R, C>) = d(*mv.inputs.sVars.toTypedArray()).values.foldIndexed(mutableListOf()) { index, acc: MutableList<MutableList<SFun<X>>>, p ->
    if (index % mv.numCols == 0) acc.add(mutableListOf(p)) else acc.last().add(p)
    acc
  }.map { Vec<X, C>(it) }.let { Mat<X, R, C>(it) }

  open fun grad(): Map<Var<X>, SFun<X>> = inputs.sVars.map { it to Derivative(this, it) }.toMap()

  override fun ln(): SFun<X> = Log(this)
  override fun pow(exp: SFun<X>): SFun<X> = Power(this, exp)
  override fun unaryMinus(): SFun<X> = Negative(this)
  open fun sqrt(): SFun<X> = this pow (One<X>() / Two())

  override fun toString(): String = when (this) {
    is Log -> "ln($logarithmand)"
    is Negative -> "- ($value)"
    is Power -> "($base) pow ($exponent)"
    is Prod -> "($left) * ($right)"
    is Sum -> if(right is Negative) "$left $right" else "$left + $right"
    is Var -> name
    is Derivative -> "d($fn) / d($vrb)"
    is Zero -> "0" //"\uD835\uDFD8" // 𝟘
    is One -> "1"  //"\uD835\uDFD9" // 𝟙
    is Two -> "2"  //"\uD835\uDFDA" // 𝟚
    is E -> "E()"  //"\u2147"       // ⅇ
    is VMagnitude -> "|$value|"
    is DProd -> "($left) dot ($right)"
    is Composition -> "($fn)($bindings)"
    else -> super.toString()
  }

  val opStr: String = when (this) {
    is Log -> "ln"
    is Negative -> "-"
    is Power -> "pow"
    is Prod -> "×"
    is Sum -> "+"
    is Derivative -> "d"
    else -> super.toString()
  }

  fun toGraph(): MutableNode = mutNode(if (this is Var) "$this" else "${hashCode()}").apply {
    when (this@SFun) {
      is Var -> name
      is Negative -> { value.toGraph() - this; add(Label.of("neg")) }
      is Derivative -> { fn.toGraph() - this; mutNode("$this").apply { add(Label.of(vrb.toString())) } - this; add(Label.of("d")) }
      is BiFun -> { left.toGraph() - this; right.toGraph() - this; add(Label.of(opStr)) }
      is RealNumber -> add(Label.of("$value"))
      is One -> add(Label.of("one"))
      is Zero -> add(Label.of("zero"))
      is Composition -> { bindings.sMap.entries.map { entry -> mutNode(entry.hashCode().toString()).also { compNode -> entry.key.toGraph() - compNode; entry.value.toGraph() - compNode; compNode.add(Label.of("comp")) } }.map { it - this; add(Label.of("bindings")) } }
      else -> TODO(this@SFun.javaClass.toString())
    }
  }
}

/**
 * Symbolic operators.
 */

sealed class BiFun<X: SFun<X>>(val left: SFun<X>, val right: SFun<X>): SFun<X>(left, right)

class Sum<X : SFun<X>>(val addend: SFun<X>, val augend: SFun<X>): BiFun<X>(addend, augend)

class Negative<X : SFun<X>>(val value: SFun<X>) : SFun<X>(value)
class Prod<X : SFun<X>>(val multiplicand: SFun<X>, val multiplicator: SFun<X>): BiFun<X>(multiplicand, multiplicator)
class Power<X : SFun<X>> internal constructor(val base: SFun<X>, val exponent: SFun<X>) : BiFun<X>(base, exponent)
class Log<X : SFun<X>> internal constructor(val logarithmand: SFun<X>, val base: SFun<X> = E()) : BiFun<X>(logarithmand, base)
class Derivative<X : SFun<X>>(val fn: SFun<X>, val vrb: Var<X>) : SFun<X>(fn, vrb) {
  fun SFun<X>.df(): SFun<X> = when (this@df) {
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
    is Composition -> bindings.curried().fold(One()) { acc: SFun<X>, binding ->
      acc * fn.df()(binding) * binding.sMap.entries.first().value.df()
    }
  }
}

class Composition<X : SFun<X>>(val fn: SFun<X>, val bindings: Bindings<X>) : SFun<X>(fn) {
  val evaluate by lazy { call() }
  override val inputs: Inputs<X> by lazy { evaluate.inputs }

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

  fun SFun<X>.call(): SFun<X> = bindings.sMap.getOrElse(this@call) { bind() }

  fun SFun<X>.bind() = when (this@bind) {
    is Zero -> bindings.zero
    is One -> bindings.one
    is Two -> bindings.two
    is E -> bindings.e
    is Var -> this
    is SConst -> this
    is Prod -> left.call() * right.call()
    is Sum -> left.call() + right.call()
    is Power -> base.call() pow exponent.call()
    is Negative -> -value.call()
    is Log -> logarithmand.call().ln()
    is Derivative -> df().call()
    is DProd -> left(bindings) as Vec<X, D1> dot right(bindings) as Vec<X, D1>
    is VMagnitude -> value(bindings).magnitude()
    is Composition -> fn.call().call()
  }
}

data class Bindings<X : SFun<X>>(
  val sMap: Map<SFun<X>, SFun<X>> = mapOf(),
  val zero: SFun<X> = Zero(),
  val one: SFun<X> = One(),
  val two: SFun<X> = Two(),
  val e: SFun<X> = E()) {
  //  constructor(sMap: Map<Fun<X>, Fun<X>>,
//              vMap: Map<VFun<X, *>, VFun<X, *>>,
//              zero: Fun<X>,
//              one: Fun<X>,
//              two: Fun<X>,
//              E: Fun<X>): this(sMap, zero, one, two, E)
  val isReassignmentFree = sMap.values.all { it.inputs.sVars.isEmpty() }
  fun fullyDetermines(fn: SFun<X>) = fn.inputs.sVars.all { it in sMap }
  override fun toString() = sMap.toString()
  operator fun contains(v: Var<X>) = v in sMap
  fun curried() = sMap.entries.map { Bindings(mapOf(it.key to it.value), zero, one, two, e) }
}

class DProd<X: SFun<X>>(val left: VFun<X, *>, val right: VFun<X, *>): SFun<X>(left, right)

class VMagnitude<X: SFun<X>>(val value: VFun<X, *>): SFun<X>(value)//, value.vVars)

interface Variable { val name: String }

class Var<X : SFun<X>>(override val name: String = "") : Variable, SFun<X>() {
  override val inputs: Inputs<X> = Inputs(setOf(this))
}

open class SConst<X : SFun<X>> : SFun<X>()
class Zero<X: SFun<X>> : SConst<X>()
class One<X: SFun<X>> : SConst<X>()
class Two<X: SFun<X>> : SConst<X>()
class E<X: SFun<X>> : SConst<X>()

abstract class RealNumber<X : SFun<X>>(open val value: Number) : SConst<X>()

class DReal(override val value: Double) : RealNumber<DReal>(value) {
  override fun unaryMinus() = DReal(-value)
  override fun ln() = DReal(ln(value))
  override fun toString() = value.toString()

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

  override fun sqrt() = DReal(sqrt(value))

  override fun <E : D1> times(multiplicand: VFun<DReal, E>) =
    when (multiplicand) {
      is Vec -> Vec(multiplicand.contents.map { this * it })
      else -> super.times(multiplicand)
    }

  override fun <R : D1, C: D1> times(multiplicand: MFun<DReal, R, C>) =
    when (multiplicand) {
      is Mat -> Mat(multiplicand.rows.map { this * it } as List<Vec<DReal, C>>)
      else -> super.times(multiplicand)
    }
}

/**
 * Numerical context.
 */

sealed class Protocol<X : RealNumber<X>> {
  class IndVar<X: SFun<X>> constructor(val fn: SFun<X>)

  class Differential<X: SFun<X>>(private val fx: SFun<X>) {
    // TODO: ensure correctness for arbitrary nested functions using the Chain rule
    infix operator fun div(arg: Differential<X>) = fx.d(arg.fx.inputs.sVars.first())
  }

  fun <X: SFun<X>> d(fn: SFun<X>) = Differential(fn)
  abstract fun wrap(default: Number): X

  operator fun Number.times(multiplicand: SFun<X>) = multiplicand * wrap(this)
  operator fun SFun<X>.times(multiplicand: Number) = wrap(multiplicand) * this

  operator fun Number.plus(addend: SFun<X>) = addend + wrap(this)
  operator fun SFun<X>.plus(addend: Number) = wrap(addend) + this

  operator fun Number.minus(subtrahend: SFun<X>) = -subtrahend + wrap(this)
  operator fun SFun<X>.minus(subtrahend: Number) = -wrap(subtrahend) + this

  fun Number.pow(exp: SFun<X>) = wrap(this) pow exp
  infix fun SFun<X>.pow(exp: Number) = this pow wrap(exp)
}

object DoublePrecision : Protocol<DReal>() {
  override fun wrap(default: Number): DReal = DReal(default.toDouble())

  val one = wrap(1.0)
  val zero = wrap(0.0)
  val two = wrap(2.0)
  val e = wrap(E)

  fun vrb(name: String) = Var<DReal>(name)

  @JvmName("ValBnd") operator fun SFun<DReal>.invoke(vararg pairs: Pair<Var<DReal>, Number>) =
    this(Bindings(pairs.map { (it.first to wrap(it.second)) }.toMap(), zero, one, two, e))
  @JvmName("FunBnd") operator fun SFun<DReal>.invoke(vararg pairs: Pair<SFun<DReal>, SFun<DReal>>) =
    this(Bindings(pairs.map { (it.first to it.second) }.toMap(), zero, one, two, e))

  operator fun <Y : D1> VFun<DReal, Y>.invoke(vararg sPairs: Pair<Var<DReal>, Number>) =
    this(Bindings(sPairs.map { (it.first to wrap(it.second)) }.toMap(), zero, one, two, e))

  operator fun <Rows : D1, Cols: D1> MFun<DReal, Rows, Cols>.invoke(vararg sPairs: Pair<Var<DReal>, Number>) =
    this(Bindings(sPairs.map { (it.first to wrap(it.second)) }.toMap(), zero, one, two, e))

  fun SFun<DReal>.asDouble() = (this as DReal).value

  val x = vrb("X")
  val y = vrb("Y")
  val z = vrb("Z")

  fun Vec(d0: Double) = Vec(DReal(d0))
  fun Vec(d0: Double, d1: Double) = Vec(DReal(d0), DReal(d1))
  fun Vec(d0: Double, d1: Double, d2: Double) = Vec(DReal(d0), DReal(d1), DReal(d2))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double) = Vec(DReal(d0), DReal(d1), DReal(d2), DReal(d3))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double) = Vec(DReal(d0), DReal(d1), DReal(d2), DReal(d3), DReal(d4))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double) = Vec(DReal(d0), DReal(d1), DReal(d2), DReal(d3), DReal(d4), DReal(d5))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double, d6: Double) = Vec(DReal(d0), DReal(d1), DReal(d2), DReal(d3), DReal(d4), DReal(d5), DReal(d6))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double, d6: Double, d7: Double) = Vec(DReal(d0), DReal(d1), DReal(d2), DReal(d3), DReal(d4), DReal(d5), DReal(d6), DReal(d7))
  fun Vec(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double, d6: Double, d7: Double, d8: Double) = Vec(DReal(d0), DReal(d1), DReal(d2), DReal(d3), DReal(d4), DReal(d5), DReal(d6), DReal(d7), DReal(d8))

  fun Mat1x1(d0: Double) = Mat<DReal, D1, D1>(Vec(d0))
  fun Mat1x2(d0: Double, d1: Double) = Mat<DReal, D1, D2>(Vec(d0, d1))
  fun Mat1x3(d0: Double, d1: Double, d2: Double) = Mat<DReal, D1, D3>(Vec(d0, d1, d2))
  fun Mat2x1(d0: Double, d1: Double) = Mat<DReal, D2, D1>(Vec(d0), Vec(d1))
  fun Mat2x2(d0: Double, d1: Double, d2: Double, d3: Double) = Mat<DReal, D2, D2>(Vec(d0, d1), Vec(d2, d3))
  fun Mat2x3(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double) = Mat<DReal, D2, D3>(Vec(d0, d1, d2), Vec(d3, d4, d5))
  fun Mat3x1(d0: Double, d1: Double, d2: Double) = Mat<DReal, D3, D1>(listOf(Vec(d0), Vec(d1), Vec(d2)) )
  fun Mat3x2(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double) = Mat<DReal, D3, D2>(Vec(d0, d1), Vec(d2, d3), Vec(d4, d5))
  fun Mat3x3(d0: Double, d1: Double, d2: Double, d3: Double, d4: Double, d5: Double, d6: Double, d7: Double, d8: Double) = Mat<DReal, D3, D3>(Vec(d0, d1, d2), Vec(d3, d4, d5), Vec(d6, d7, d8))

  fun Var2() = Vec(Var<DReal>(), Var())
  fun Var3() = Vec(Var<DReal>(), Var(), Var())

  fun Var2x1() = Mat2x1(Var<DReal>(), Var())
  fun Var2x2() = Mat2x2(Var<DReal>(), Var(), Var(), Var())
  fun Var2x3() = Mat2x3(Var<DReal>(), Var(), Var(), Var(), Var(), Var())
  fun Var3x1() = Mat3x1(Var<DReal>(), Var(), Var())
  fun Var3x2() = Mat3x2(Var<DReal>(), Var(), Var(), Var(), Var(), Var())
  fun Var3x3() = Mat3x3(Var<DReal>(), Var(), Var(), Var(), Var(), Var(), Var(), Var(), Var())
}