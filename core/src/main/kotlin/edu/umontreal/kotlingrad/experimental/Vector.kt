@file:Suppress("ClassName", "LocalVariableName", "NonAsciiCharacters", "FunctionName", "MemberVisibilityCanBePrivate", "UNUSED_VARIABLE")
package edu.umontreal.kotlingrad.experimental

import edu.mcgill.kaliningraph.circuits.*
import edu.umontreal.kotlingrad.utils.matmul
import org.jetbrains.bio.viktor.F64Array
import kotlin.reflect.KProperty

/**
 * Vector function.
 */

sealed class VFun<X: SFun<X>, E: D1>(override val bindings: Bindings<X>): Fun<X> {
  constructor(vararg funs: Fun<X>): this(Bindings(*funs))

  @Suppress("UNCHECKED_CAST")
  override fun invoke(newBindings: Bindings<X>): VFun<X, E> =
    VComposition(this, newBindings)
      .run { if (bindings.complete || newBindings.readyToBind || EAGER) evaluate else this }

  // Materializes the concrete vector from the dataflow graph
  operator fun invoke(): Vec<X, E> =
    VComposition(this).evaluate.let {
      try {
        it as Vec<X, E>
      } catch (e: ClassCastException) {
        show("before"); it.show("after")
        throw NumberFormatException("Vector function has unbound free variables: ${bindings.allFreeVariables.keys}")
      }
    }

  val mapInput by lazy { SVar(proto, "mapInput") }
  open fun map(ef: (SFun<X>) -> SFun<X>): VFun<X, E> = VMap(this, ef(mapInput), mapInput)
  open fun <C: D1> vMap(ef: (SFun<X>) -> VFun<X, C>): MFun<X, E, C> = VVMap(this, ef(mapInput), mapInput)

  fun <Q: D1> d(v1: VVar<X, Q>): MFun<X, E, Q> = Jacobian(this, v1)

  fun d(v1: SVar<X>) = VDerivative(this, v1)
  fun d(v1: SVar<X>, v2: SVar<X>) = Jacobian(this, VVar(bindings.proto, "j2", D2, Vec(v1, v2)))
  fun d(v1: SVar<X>, v2: SVar<X>, v3: SVar<X>) = Jacobian(this, VVar(bindings.proto, "j3", D3, Vec(v1, v2, v3)))
  fun d(v1: SVar<X>, v2: SVar<X>, v3: SVar<X>, v4: SVar<X>) = Jacobian(this, VVar(bindings.proto, "j4", D4, Vec(v1, v2, v3, v4)))
  fun d(v1: SVar<X>, v2: SVar<X>, v3: SVar<X>, v4: SVar<X>, v5: SVar<X>) = Jacobian(this, VVar(bindings.proto, "j5", D5, Vec(v1, v2, v3, v4, v5)))
  fun d(v1: SVar<X>, v2: SVar<X>, v3: SVar<X>, v4: SVar<X>, v5: SVar<X>, v6: SVar<X>) = Jacobian(this, VVar(bindings.proto, "j6", D6, Vec(v1, v2, v3, v4, v5, v6)))
  fun d(v1: SVar<X>, v2: SVar<X>, v3: SVar<X>, v4: SVar<X>, v5: SVar<X>, v6: SVar<X>, v7: SVar<X>) = Jacobian(this, VVar(bindings.proto, "j7", D7, Vec(v1, v2, v3, v4, v5, v6, v7)))
  fun d(v1: SVar<X>, v2: SVar<X>, v3: SVar<X>, v4: SVar<X>, v5: SVar<X>, v6: SVar<X>, v7: SVar<X>, v8: SVar<X>) = Jacobian(this, VVar(bindings.proto, "j8", D8, Vec(v1, v2, v3, v4, v5, v6, v7, v8)))
  fun d(v1: SVar<X>, v2: SVar<X>, v3: SVar<X>, v4: SVar<X>, v5: SVar<X>, v6: SVar<X>, v7: SVar<X>, v8: SVar<X>, v9: SVar<X>) = Jacobian(this, VVar(bindings.proto, "j9", D9, Vec(v1, v2, v3, v4, v5, v6, v7, v8, v9)))
  fun d(vararg vars: SVar<X>): Map<SVar<X>, VFun<X, E>> = vars.map { it to d(it) }.toMap()
  fun grad(): Map<SVar<X>, VFun<X, E>> = bindings.sVars.map { it to d(it) }.toMap()

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
    is VVar -> "VVar($name)"
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

  override operator fun invoke(vararg numbers: Number): VFun<X, E> = invoke(bindings.zip(numbers.map { wrap(it) }))
  override operator fun invoke(vararg funs: Fun<X>): VFun<X, E> = invoke(bindings.zip(funs.toList()))
  override operator fun invoke(vararg ps: Pair<Fun<X>, Any>): VFun<X, E> = invoke(ps.toList().bind())
}

abstract class UnVFun<X: SFun<X>, E: D1>(override val bindings: Bindings<X>): VFun<X, E>(bindings), UnFun<X> {
  constructor(f: Fun<X>): this(Bindings(f))
}

abstract class BiVFun<X: SFun<X>, E: D1>(
  override val left: Fun<X>,
  override val right: Fun<X>
): VFun<X, E>(left, right), BiFun<X>

abstract class PolyVFun<X: SFun<X>, E: D1>(
  override val bindings: Bindings<X>,
  override vararg val inputs: Fun<X>
): VFun<X, E>(bindings), PolyFun<X> {
  constructor(vararg inputs: Fun<X>): this(Bindings(*inputs), *inputs)
}

class VNegative<X: SFun<X>, E: D1>(override val input: VFun<X, E>): UnVFun<X, E>(input)

class VMap<X: SFun<X>, E: D1>(override val input: VFun<X, E>, val ssMap: SFun<X>, placeholder: SVar<X>):
  UnVFun<X, E>(input.bindings + ssMap.bindings - placeholder)
class VVMap<X: SFun<X>, R: D1, C: D1>(val input: VFun<X, R>, val svMap: VFun<X, C>, placeholder: SVar<X>):
  PolyMFun<X, R, C>(input.bindings + svMap.bindings - placeholder, input)

class VSum<X: SFun<X>, E: D1>(override val left: VFun<X, E>, override val right: VFun<X, E>): BiVFun<X, E>(left, right)

class VVProd<X: SFun<X>, E: D1>(override val left: VFun<X, E>, override val right: VFun<X, E>): BiVFun<X, E>(left, right)
class SVProd<X: SFun<X>, E: D1>(override val left: SFun<X>, override val right: VFun<X, E>): BiVFun<X, E>(left, right)
class VSProd<X: SFun<X>, E: D1>(override val left: VFun<X, E>, override val right: SFun<X>): BiVFun<X, E>(left, right)
class MVProd<X: SFun<X>, R: D1, C: D1>(override val left: MFun<X, R, C>, override val right: VFun<X, C>): BiVFun<X, R>(left, right)
class VMProd<X: SFun<X>, R: D1, C: D1>(override val left: VFun<X, C>, override val right: MFun<X, R, C>): BiVFun<X, R>(left, right)
class MSumRows<X: SFun<X>, R: D1, C: D1>(val input: MFun<X, R, C>): PolyVFun<X, C>(input)

class VDerivative<X : SFun<X>, E: D1>(override val input: VFun<X, E>, override val vrb: SVar<X>) : BiVFun<X, E>(input, vrb), Grad<X> {
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

class Gradient<X : SFun<X>, E: D1>(override val input: SFun<X>, override val vrb: VVar<X, E>): BiVFun<X, E>(input, vrb), Grad<X> {
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
  override val proto: X,
  override val name: String = "",
  val length: E,
  val sVars: Vec<X, E> = Vec(List(length.i) { SVar(proto, "$name[$it]") })
): Variable<X>, VFun<X, E>() {
  override val bindings: Bindings<X> = Bindings(mapOf(this to sVars))
  override fun equals(other: Any?) = other is VVar<*, *> && name == other.name
  override fun hashCode(): Int = name.hashCode()
  operator fun getValue(thisRef: Any?, property: KProperty<*>) =
    VVar(proto, if (name.isEmpty()) property.name else name, length, sVars)
}

class VComposition<X: SFun<X>, E: D1>(
  val input: VFun<X, E>,
  inputs: Bindings<X> = Bindings(input.proto)
): PolyVFun<X, E>(input.bindings + inputs, input) {
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
      else -> TODO("${this@bind}")
    }.also { result -> bnds.checkForUnpropagatedVariables(this@bind, result) }
}

open class VConst<X: SFun<X>, E: D1> constructor(vararg val consts: SConst<X>): Vec<X, E>(consts.toList()), Constant<X> {
  constructor(proto: X, fVec: F64Array): this(*fVec.toDoubleArray().map { proto.wrap(it) }.toTypedArray())
  constructor(proto: X, vararg values: Number): this(*values.map { proto.wrap(it) }.toTypedArray())
  override val proto by lazy { consts[0].proto }

  val simdVec by lazy { F64Array(consts.size) { consts[it].doubleValue } }

  override fun plus(addend: VFun<X, E>) = when(addend) {
    is VConst<X, E> -> VConst(proto, simdVec + addend.simdVec)
    else -> super.plus(addend)
  }

  override fun minus(subtrahend: VFun<X, E>) = when(subtrahend) {
    is VConst<X, E> -> VConst(proto, simdVec - subtrahend.simdVec)
    else -> super.minus(subtrahend)
  }

  override fun ʘ(multiplicand: VFun<X, E>) = when(multiplicand) {
    is VConst<X, E> -> VConst(proto, simdVec * multiplicand.simdVec)
    else -> super.ʘ(multiplicand)
  }

  override fun dot(multiplicand: VFun<X, E>): SFun<X> = when(multiplicand) {
    is VConst<X, E> -> SConst(simdVec dot multiplicand.simdVec)
    else -> super.dot(multiplicand)
  }

  override fun times(multiplicand: SFun<X>) = when (multiplicand) {
    is SConst<X> -> VConst(proto, simdVec * multiplicand.doubleValue)
    else -> super.times(multiplicand)
  }

  override operator fun <Q: D1> times(multiplicand: MFun<X, Q, E>): VFun<X, Q> = when (multiplicand) {
    is MConst<X, Q, E> -> VConst(proto, simdVec matmul multiplicand.simdVec)
    else -> super.times(multiplicand)
  }
}

open class Vec<X: SFun<X>, E: D1>(val contents: List<SFun<X>>):
  VFun<X, E>(*contents.toTypedArray()), Iterable<SFun<X>> by contents {
  constructor(len: Nat<E>, gen: (Int) -> SFun<X>): this(List(len.i) { gen(it) })
  override val proto by lazy { contents.first().proto }
  val size = contents.size

  operator fun get(index: Int) = contents[index]

  override fun plus(addend: VFun<X, E>) = when (addend) {
    is Vec<X, E> -> Vec(mapIndexed { i, v -> v + addend[i] })
    else -> super.plus(addend)
  }

  override fun minus(subtrahend: VFun<X, E>) = when (subtrahend) {
    is Vec<X, E> -> Vec(mapIndexed { i, v -> v - subtrahend[i] })
    else -> super.minus(subtrahend)
  }

  override fun ʘ(multiplicand: VFun<X, E>) = when(multiplicand) {
    is Vec<X, E> -> Vec(mapIndexed { i, v -> v * multiplicand[i] })
    else -> super.ʘ(multiplicand)
  }

  override fun times(multiplicand: SFun<X>) = map { it * multiplicand }

  override fun dot(multiplicand: VFun<X, E>) = when(multiplicand) {
    is Vec<X, E> -> mapIndexed { i, v -> v * multiplicand[i] }.reduce { acc, it -> acc + it }
    else -> super.dot(multiplicand)
  }

  override operator fun <Q: D1> times(multiplicand: MFun<X, Q, E>): VFun<X, Q> = when (multiplicand) {
    is Mat<X, Q, E> -> Vec(multiplicand.map { row: VFun<X, E> -> row dot this })
    else -> super.times(multiplicand)
  }

  override fun map(ef: (SFun<X>) -> SFun<X>) = Vec<X, E>(contents.map { ef(it)(mapInput to it) })

  override fun <C: D1> vMap(ef: (SFun<X>) -> VFun<X, C>) = Mat<X, E, C>(contents.map { ef(it)(mapInput to it) })

  override fun unaryMinus() = map { -it }

  override fun sum() = reduce { acc, it -> acc + it }

  companion object {
    operator fun <T: SFun<T>> invoke(s0: SConst<T>): VConst<T, D1> = VConst(s0)
    operator fun <T: SFun<T>> invoke(s0: SConst<T>, s1: SConst<T>): VConst<T, D2> = VConst(s0, s1)
    operator fun <T: SFun<T>> invoke(s0: SConst<T>, s1: SConst<T>, s2: SConst<T>): VConst<T, D3> = VConst(s0, s1, s2)
    operator fun <T: SFun<T>> invoke(s0: SConst<T>, s1: SConst<T>, s2: SConst<T>, s3: SConst<T>): VConst<T, D4> = VConst(s0, s1, s2, s3)
    operator fun <T: SFun<T>> invoke(s0: SConst<T>, s1: SConst<T>, s2: SConst<T>, s3: SConst<T>, s4: SConst<T>): VConst<T, D5> = VConst(s0, s1, s2, s3, s4)
    operator fun <T: SFun<T>> invoke(s0: SConst<T>, s1: SConst<T>, s2: SConst<T>, s3: SConst<T>, s4: SConst<T>, s5: SConst<T>): VConst<T, D6> = VConst(s0, s1, s2, s3, s4, s5)
    operator fun <T: SFun<T>> invoke(s0: SConst<T>, s1: SConst<T>, s2: SConst<T>, s3: SConst<T>, s4: SConst<T>, s5: SConst<T>, s6: SConst<T>): VConst<T, D7> = VConst(s0, s1, s2, s3, s4, s5, s6)
    operator fun <T: SFun<T>> invoke(s0: SConst<T>, s1: SConst<T>, s2: SConst<T>, s3: SConst<T>, s4: SConst<T>, s5: SConst<T>, s6: SConst<T>, s7: SConst<T>): VConst<T, D8> = VConst(s0, s1, s2, s3, s4, s5, s6, s7)
    operator fun <T: SFun<T>> invoke(s0: SConst<T>, s1: SConst<T>, s2: SConst<T>, s3: SConst<T>, s4: SConst<T>, s5: SConst<T>, s6: SConst<T>, s7: SConst<T>, s8: SConst<T>): VConst<T, D9> = VConst(s0, s1, s2, s3, s4, s5, s6, s7, s8)

    operator fun <T: SFun<T>> invoke(t0: SFun<T>): Vec<T, D1> = Vec(arrayListOf(t0))
    operator fun <T: SFun<T>> invoke(t0: SFun<T>, t1: SFun<T>): Vec<T, D2> = Vec(arrayListOf(t0, t1))
    operator fun <T: SFun<T>> invoke(t0: SFun<T>, t1: SFun<T>, t2: SFun<T>): Vec<T, D3> = Vec(arrayListOf(t0, t1, t2))
    operator fun <T: SFun<T>> invoke(t0: SFun<T>, t1: SFun<T>, t2: SFun<T>, t3: SFun<T>): Vec<T, D4> = Vec(arrayListOf(t0, t1, t2, t3))
    operator fun <T: SFun<T>> invoke(t0: SFun<T>, t1: SFun<T>, t2: SFun<T>, t3: SFun<T>, t4: SFun<T>): Vec<T, D5> = Vec(arrayListOf(t0, t1, t2, t3, t4))
    operator fun <T: SFun<T>> invoke(t0: SFun<T>, t1: SFun<T>, t2: SFun<T>, t3: SFun<T>, t4: SFun<T>, t5: SFun<T>): Vec<T, D6> = Vec(arrayListOf(t0, t1, t2, t3, t4, t5))
    operator fun <T: SFun<T>> invoke(t0: SFun<T>, t1: SFun<T>, t2: SFun<T>, t3: SFun<T>, t4: SFun<T>, t5: SFun<T>, t6: SFun<T>): Vec<T, D7> = Vec(arrayListOf(t0, t1, t2, t3, t4, t5, t6))
    operator fun <T: SFun<T>> invoke(t0: SFun<T>, t1: SFun<T>, t2: SFun<T>, t3: SFun<T>, t4: SFun<T>, t5: SFun<T>, t6: SFun<T>, t7: SFun<T>): Vec<T, D8> = Vec(arrayListOf(t0, t1, t2, t3, t4, t5, t6, t7))
    operator fun <T: SFun<T>> invoke(t0: SFun<T>, t1: SFun<T>, t2: SFun<T>, t3: SFun<T>, t4: SFun<T>, t5: SFun<T>, t6: SFun<T>, t7: SFun<T>, t8: SFun<T>): Vec<T, D9> = Vec(arrayListOf(t0, t1, t2, t3, t4, t5, t6, t7, t8))
  }
}

/**
 * Type level integers.
 */

interface Nat<T: D0> { val i: Int }
sealed class INat<T: D0>: SConst<INat<T>>() {
  abstract val i: Int

  override fun toString() = "$i"
}

sealed class D0(override val i: Int = 0):INat<D0>(){ companion object:  D0(), Nat<D0> }
sealed class D1(override val i: Int = 1):    D0(i) { companion object:  D1(), Nat<D1> }
sealed class D2(override val i: Int = 2):    D1(i) { companion object:  D2(), Nat<D2> }
sealed class D3(override val i: Int = 3):    D2(i) { companion object:  D3(), Nat<D3> }
sealed class D4(override val i: Int = 4):    D3(i) { companion object:  D4(), Nat<D4> }
sealed class D5(override val i: Int = 5):    D4(i) { companion object:  D5(), Nat<D5> }
sealed class D6(override val i: Int = 6):    D5(i) { companion object:  D6(), Nat<D6> }
sealed class D7(override val i: Int = 7):    D6(i) { companion object:  D7(), Nat<D7> }
sealed class D8(override val i: Int = 8):    D7(i) { companion object:  D8(), Nat<D8> }
sealed class D9(override val i: Int = 9):    D8(i) { companion object:  D9(), Nat<D9> }
sealed class D10(override val i: Int = 10):  D9(i) { companion object: D10(), Nat<D10> }
sealed class D11(override val i: Int = 11): D10(i) { companion object: D11(), Nat<D11> }
sealed class D12(override val i: Int = 12): D11(i) { companion object: D12(), Nat<D12> }
sealed class D13(override val i: Int = 13): D12(i) { companion object: D13(), Nat<D13> }
sealed class D14(override val i: Int = 14): D13(i) { companion object: D14(), Nat<D14> }
sealed class D15(override val i: Int = 15): D14(i) { companion object: D15(), Nat<D15> }
sealed class D16(override val i: Int = 16): D15(i) { companion object: D16(), Nat<D16> }
sealed class D17(override val i: Int = 17): D16(i) { companion object: D17(), Nat<D17> }
sealed class D18(override val i: Int = 18): D17(i) { companion object: D18(), Nat<D18> }
sealed class D19(override val i: Int = 19): D18(i) { companion object: D19(), Nat<D19> }
sealed class D20(override val i: Int = 20): D19(i) { companion object: D20(), Nat<D20> }
sealed class D21(override val i: Int = 21): D20(i) { companion object: D21(), Nat<D21> }
sealed class D22(override val i: Int = 22): D21(i) { companion object: D22(), Nat<D22> }
sealed class D23(override val i: Int = 23): D22(i) { companion object: D23(), Nat<D23> }
sealed class D24(override val i: Int = 24): D23(i) { companion object: D24(), Nat<D24> }
sealed class D25(override val i: Int = 25): D24(i) { companion object: D25(), Nat<D25> }
sealed class D26(override val i: Int = 26): D25(i) { companion object: D26(), Nat<D26> }
sealed class D27(override val i: Int = 27): D26(i) { companion object: D27(), Nat<D27> }
sealed class D28(override val i: Int = 28): D27(i) { companion object: D28(), Nat<D28> }
sealed class D29(override val i: Int = 29): D28(i) { companion object: D29(), Nat<D29> }
sealed class D30(override val i: Int = 30): D29(i) { companion object: D30(), Nat<D30> }
sealed class D50(override val i: Int = 50): D30(i) { companion object: D50(), Nat<D50> }
sealed class D100(override val i: Int = 100): D50(i) { companion object: D100(), Nat<D100> }

fun main() {

}