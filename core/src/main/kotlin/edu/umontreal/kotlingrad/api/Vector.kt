@file:Suppress("ClassName", "LocalVariableName", "NonAsciiCharacters", "FunctionName", "MemberVisibilityCanBePrivate", "UNUSED_VARIABLE")
package edu.umontreal.kotlingrad.api

import edu.mcgill.kaliningraph.circuits.*
import edu.umontreal.kotlingrad.shapes.*
import edu.umontreal.kotlingrad.utils.matmul
import org.jetbrains.bio.viktor.F64Array
import kotlin.reflect.KProperty

/**
 * Vector function.
 */

sealed class VFun<X: SFun<X>, E: D1>(override vararg val inputs: Fun<X>): Fun<X> {
  @Suppress("UNCHECKED_CAST")
  override fun invoke(newBindings: Bindings<X>): VFun<X, E> =
    VComposition(this, newBindings)
      .run { if (bindings.complete || newBindings.readyToBind || EAGER) evaluate else this }

  // Materializes the concrete vector from the dataflow graph
  override operator fun invoke(): Vec<X, E> =
    VComposition(this).evaluate.let {
      try {
        it as Vec<X, E>
      } catch (e: ClassCastException) {
        show("before"); it.show("after")
        throw NumberFormatException("Vector function has unbound free variables: ${bindings.allFreeVariables.keys}")
      }
    }

  override operator fun invoke(vararg numbers: Number): VFun<X, E> =
    invoke(bindings.zip(numbers.map { wrap(it) }))
  override operator fun invoke(vararg funs: Fun<X>): VFun<X, E> =
    invoke(bindings.zip(funs.toList()))
  override operator fun invoke(vararg ps: Pair<Fun<X>, Any>): VFun<X, E> =
    invoke(ps.toList().bind())

  companion object { const val KG_IT = "it" }
  val mapInput = SVar<X>(KG_IT)
  open fun map(ef: (SFun<X>) -> SFun<X>): VFun<X, E> =
    VMap(this, ef(mapInput), mapInput)
  open fun <C: D1> vMap(ef: (SFun<X>) -> VFun<X, C>): MFun<X, E, C> =
    VVMap(this, ef(mapInput), mapInput)

  fun <Q: D1> d(v1: VVar<X, Q>): MFun<X, E, Q> = Jacobian(this, v1)

  fun d(v1: SVar<X>) = VDerivative(this, v1)

  fun d(vararg vars: SVar<X>): Map<SVar<X>, VFun<X, E>> =
    vars.map { it to d(it) }.toMap()

  fun grad(): Map<SVar<X>, VFun<X, E>> =
    bindings.sVars.map { it to d(it) }.toMap()

  open operator fun unaryMinus(): VFun<X, E> = VNegative(this)
  open operator fun plus(addend: VFun<X, E>): VFun<X, E> = VSum(this, addend)
  open operator fun minus(subtrahend: VFun<X, E>): VFun<X, E> = VSum(this, -subtrahend)
  open infix fun ʘ(multiplicand: VFun<X, E>): VFun<X, E> = VVProd(this, multiplicand)
  open operator fun times(multiplicand: SFun<X>): VFun<X, E> = VSProd(this, multiplicand)
  open operator fun <Q: D1> times(multiplicand: MFun<X, Q, E>): VFun<X, Q> = VMProd(this, multiplicand)
  open infix fun dot(multiplicand: VFun<X, E>): SFun<X> = DProd(this, multiplicand)

  operator fun times(multiplicand: Number): VFun<X, E> = this * wrap(multiplicand)

  fun magnitude() = (this ʘ this).sum().sqrt()
  open fun sum(): SFun<X> = VSumAll(this)

  override fun toString() = when (this) {
    is Vec -> contents.joinToString(", ", "[", "]")
    is VMap -> "$input.map { $ssMap }"
    is VVar -> "$name: Var$length"
    is VComposition -> "($input)$bindings"
    else -> asString()
  }

  override val op: Op = when(this) {
    is VNegative<X, *> -> Monad.`-`
    is VMap<X, *> -> Polyad.map
    is VSum<X, *> -> Dyad.`+`
    is Gradient<X, *> -> Dyad.d
    is VDerivative<X, *> -> Dyad.d
    is VVProd<X, *> -> Dyad.`*`
    is SVProd<X, *> -> Dyad.`*`
    is VSProd<X, *> -> Dyad.`*`
    is MVProd<X, *, *> -> Dyad.dot
    is VMProd<X, *, *> -> Dyad.dot
    is VComposition<X, *> -> Polyad.λ
    is MSumRows<X, *, *> -> Polyad.Σ
    else -> Monad.id
  }
}

class VNegative<X: SFun<X>, E: D1>(override val input: VFun<X, E>): VFun<X, E>(input), UnFun<X>

class VMap<X: SFun<X>, E: D1>(
  val input: VFun<X, E>,
  val ssMap: SFun<X>, placeholder: SVar<X>
): VFun<X, E>(input, ssMap), BiFun<X> {
  override val left = input
  override val right = ssMap
  override val bindings: Bindings<X> = input.bindings + ssMap.bindings - placeholder
}

class VVMap<X: SFun<X>, R: D1, C: D1>(
  val input: VFun<X, R>,
  val svMap: VFun<X, C>,
  placeholder: SVar<X>
): MFun<X, R, C>(input, svMap), PolyFun<X> {
  override val bindings: Bindings<X> = input.bindings + svMap.bindings - placeholder
  override val inputs: Array<out Fun<X>> = arrayOf(input, svMap)
}

class VSum<X: SFun<X>, E: D1>(override val left: VFun<X, E>, override val right: VFun<X, E>): VFun<X, E>(left, right), BiFun<X>

class VVProd<X: SFun<X>, E: D1>(override val left: VFun<X, E>, override val right: VFun<X, E>): VFun<X, E>(left, right), BiFun<X>
class SVProd<X: SFun<X>, E: D1>(override val left: SFun<X>, override val right: VFun<X, E>): VFun<X, E>(left, right), BiFun<X>
class VSProd<X: SFun<X>, E: D1>(override val left: VFun<X, E>, override val right: SFun<X>): VFun<X, E>(left, right), BiFun<X>
class MVProd<X: SFun<X>, R: D1, C: D1>(override val left: MFun<X, R, C>, override val right: VFun<X, C>): VFun<X, R>(left, right), BiFun<X>
class VMProd<X: SFun<X>, R: D1, C: D1>(override val left: VFun<X, C>, override val right: MFun<X, R, C>): VFun<X, R>(left, right), BiFun<X>
class MSumRows<X: SFun<X>, R: D1, C: D1>(val input: MFun<X, R, C>): VFun<X, C>(input), PolyFun<X> {
  override val inputs: Array<out Fun<X>> = arrayOf(input)
}

class VDerivative<X : SFun<X>, E: D1>(override val input: VFun<X, E>, override val vrb: SVar<X>) : VFun<X, E>(input, vrb), Grad<X> {
  fun df() = input.df()
  private fun VFun<X, E>.df(): VFun<X, E> = when (this@df) {
    is VVar -> sVars.map { it.d(vrb) }
    is VConst<X, E> -> map { Zero() }
    is VSum -> left.df() + right.df()
    is VVProd -> left.df() ʘ right + left ʘ right.df()
    is SVProd -> left.d(vrb) * right + left * right.df()
    is VSProd -> left.df() * right + left * right.d(vrb)
    is VNegative -> -input.df()
    is VDerivative -> input.df()
    is Vec -> Vec(contents.map { it.d(vrb) })
    is MVProd<X, *, *> ->
      (left.d(vrb) as MFun<X, E, E>) * right as VFun<X, E> +
      (left as MFun<X, E, E> * right.d(vrb))
    is VMProd<X, *, *> ->
      (left.d(vrb) as VFun<X, E> * right as MFun<X, E, E>) +
      (left as VFun<X, E> * right.d(vrb))
    is Gradient -> invoke().df() // map { it.d(sVar) }
    is VMap -> input.df().map { it * ssMap(mapInput to it).d(vrb) } // Chain rule
    is VComposition -> evaluate.df()
    else -> TODO(this@df.javaClass.name)
  }
}

class Gradient<X : SFun<X>, E: D1>(override val input: SFun<X>, override val vrb: VVar<X, E>): VFun<X, E>(input, vrb), Grad<X> {
  fun df() = input.df()
  private fun SFun<X>.df(): VFun<X, E> = when (this@df) {
    is SVar -> vrb.sVars.map { if(this@df == it) One() else Zero() }
    is SConst -> vrb.sVars.map { Zero() }
    is Sum -> left.df() + right.df()
    is Prod -> left.df() * right + left * right.df()
    is Power ->
      if (right.isConstant()) right * left.pow(right - One()) * left.df()
      else (left.df() * right * (One<X>() / left) + right.df() * left.ln())
    is Negative -> -input.df()
    is Log -> (left pow -One<X>()) * left.df()
    is DProd ->
      (left.d(vrb) as MFun<X, E, E> * right as VFun<X, E>) +
      (left as VFun<X, E> * right.d(vrb))
    is SComposition -> evaluate.df()
    is VSumAll<X, *> -> (input as VFun<X, E>).d(vrb).sum()
    else -> TODO(this@df.javaClass.name)
  }
}

open class VVar<X: SFun<X>, E: D1> constructor(
  val length: Nat<E>,
  override val name: String = "",
  val sVars: Vec<X, E> = Vec(List(length.i) { SVar("$name[$it]") })
): Variable<X>, VFun<X, E>() {
  override val bindings: Bindings<X> = Bindings(mapOf(this to sVars))
  override fun equals(other: Any?) = other is VVar<*, *> && name == other.name
  override fun hashCode(): Int = name.hashCode()
  operator fun getValue(thisRef: Nothing?, property: KProperty<*>) =
    VVar<X, E>(length, name.ifEmpty { property.name })
}

class VComposition<X: SFun<X>, E: D1>(override val input: VFun<X, E>, arguments: Bindings<X> = Bindings(input)): VFun<X, E>(input), UnFun<X> {
  override val bindings: Bindings<X> = input.bindings + arguments
  val evaluate: VFun<X, E> by lazy { bind(bindings) }

  @Suppress("UNCHECKED_CAST")
  fun VFun<X, E>.bind(bnds: Bindings<X>): VFun<X, E> =
    bnds[this@bind] ?: when (this@bind) {
      is VVar<X, E> -> sVars
      is Vec<X, E> -> map { it(bnds) }
      is VNegative<X, E> -> -input.bind(bnds)
      is VSum<X, E> -> left.bind(bnds) + right.bind(bnds)
      is VVProd<X, E> -> left.bind(bnds) ʘ right.bind(bnds)
      is SVProd<X, E> -> left(bnds) * right.bind(bnds)
      is VSProd<X, E> -> left.bind(bnds) * right(bnds)
      is VDerivative -> df().bind(bnds)
      is Gradient -> df().bind(bnds)
      is MVProd<X, *, *> -> left(bnds) as MFun<X, E, E> * (right as VFun<X, E>).bind(bnds)
      is VMProd<X, *, *> -> (left as VFun<X, E>).bind(bnds) * (right as MFun<X, E, E>)(bnds)
      is VMap<X, E> -> input.bind(bnds).map { ssMap(mapInput to it)(bnds) }
      is VComposition -> input.bind(bnds)
      is MSumRows<X, *, *> -> input(bnds).sum() as VFun<X, E>
    }.also { result -> bnds.checkForUnpropagatedVariables(this@bind, result) }
}

open class VConst<X: SFun<X>, E: D1> constructor(vararg val consts: SConst<X>): Vec<X, E>(consts.toList()), Constant<X> {
  constructor(fVec: F64Array): this(*fVec.toDoubleArray().map { SConst<X>(it) }.toTypedArray())
  constructor(vararg values: Number): this(*values.map { SConst<X>(it) }.toTypedArray())

  val simdVec by lazy { F64Array(consts.size) { consts[it].doubleValue } }

  override fun plus(addend: VFun<X, E>) = when(addend) {
    is VConst<X, E> -> VConst(simdVec + addend.simdVec)
    else -> super.plus(addend)
  }

  override fun minus(subtrahend: VFun<X, E>) = when(subtrahend) {
    is VConst<X, E> -> VConst(simdVec - subtrahend.simdVec)
    else -> super.minus(subtrahend)
  }

  override fun ʘ(multiplicand: VFun<X, E>) = when(multiplicand) {
    is VConst<X, E> -> VConst(simdVec * multiplicand.simdVec)
    else -> super.ʘ(multiplicand)
  }

  override fun dot(multiplicand: VFun<X, E>): SFun<X> = when(multiplicand) {
    is VConst<X, E> -> SConst(simdVec dot multiplicand.simdVec)
    else -> super.dot(multiplicand)
  }

  override fun times(multiplicand: SFun<X>) = when (multiplicand) {
    is SConst<X> -> VConst(simdVec * multiplicand.doubleValue)
    else -> super.times(multiplicand)
  }

  override operator fun <Q: D1> times(multiplicand: MFun<X, Q, E>): VFun<X, Q> = when (multiplicand) {
    is MConst<X, Q, E> -> VConst(simdVec matmul multiplicand.simdVec)
    else -> super.times(multiplicand)
  }
}

open class Vec<X: SFun<X>, E: D1>(val contents: List<SFun<X>>):
  VFun<X, E>(*contents.toTypedArray()), PolyFun<X> {
  constructor(len: Nat<E>, gen: (Int) -> SFun<X>): this(List(len.i) { gen(it) })

  val size = contents.size
  override val inputs: Array<out Fun<X>> = contents.toTypedArray()

  operator fun get(index: Int) = contents[index]

  override fun plus(addend: VFun<X, E>) = when (addend) {
    is Vec<X, E> -> Vec(contents.mapIndexed { i, v -> v + addend[i] })
    else -> super.plus(addend)
  }

  override fun minus(subtrahend: VFun<X, E>) = when (subtrahend) {
    is Vec<X, E> -> Vec(contents.mapIndexed { i, v -> v - subtrahend[i] })
    else -> super.minus(subtrahend)
  }

  override fun ʘ(multiplicand: VFun<X, E>) = when(multiplicand) {
    is Vec<X, E> -> Vec(contents.mapIndexed { i, v -> v * multiplicand[i] })
    else -> super.ʘ(multiplicand)
  }

  override fun times(multiplicand: SFun<X>) = map { it * multiplicand }

  override fun dot(multiplicand: VFun<X, E>) = when(multiplicand) {
    is Vec<X, E> -> contents.mapIndexed { i, v -> v * multiplicand[i] }.reduce { acc, it -> acc + it }
    else -> super.dot(multiplicand)
  }

  override operator fun <Q: D1> times(multiplicand: MFun<X, Q, E>): VFun<X, Q> = when (multiplicand) {
    is Mat<X, Q, E> -> Vec(multiplicand.rows.map { row: VFun<X, E> -> row dot this })
    else -> super.times(multiplicand)
  }

  override fun map(ef: (SFun<X>) -> SFun<X>) =
    Vec<X, E>(contents.map { ef(it) })

  override fun <C: D1> vMap(ef: (SFun<X>) -> VFun<X, C>) =
    Mat<X, E, C>(contents.map { ef(it)(mapInput to it) })

  override fun unaryMinus() = map { -it }

  override fun sum() = contents.reduce { acc, it -> acc + it }
}