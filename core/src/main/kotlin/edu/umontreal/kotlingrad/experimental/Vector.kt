@file:Suppress("ClassName", "LocalVariableName", "NonAsciiCharacters", "FunctionName", "MemberVisibilityCanBePrivate", "UNUSED_VARIABLE")
package edu.umontreal.kotlingrad.experimental

import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.minus
import guru.nidi.graphviz.model.Factory
import guru.nidi.graphviz.model.MutableNode

/**
 * Vector function.
 */

sealed class VFun<X: SFun<X>, E: D1>(override val bindings: Bindings<X>): Fun<X> {
  constructor(vararg funs: Fun<X>): this(Bindings(*funs))

  @Suppress("UNCHECKED_CAST")
  override fun invoke(newBindings: Bindings<X>): VFun<X, E> =
    VComposition(this, newBindings).evaluate//.run { if (newBindings.areReassignmentFree) evaluate else this }

  // Materializes the concrete vector from the dataflow graph
  operator fun invoke(): Vec<X, E> = invoke(bindings) as Vec<X, E>

  open fun map(ef: (SFun<X>) -> SFun<X>): VFun<X, E> = VMap(this, ef)

  fun d(v1: Var<X>) = VDerivative(this, v1)
  fun d(v1: Var<X>, v2: Var<X>) = Jacobian<X, E, D2>(this, v1, v2)
  fun d(v1: Var<X>, v2: Var<X>, v3: Var<X>) = Jacobian<X, E, D3>(this, v1, v2, v3)
  fun d(v1: Var<X>, v2: Var<X>, v3: Var<X>, v4: Var<X>) = Jacobian<X, E, D4>(this, v1, v2, v3, v4)
  fun d(v1: Var<X>, v2: Var<X>, v3: Var<X>, v4: Var<X>, v5: Var<X>) = Jacobian<X, E, D5>(this, v1, v2, v3, v4, v5)
  fun d(v1: Var<X>, v2: Var<X>, v3: Var<X>, v4: Var<X>, v5: Var<X>, v6: Var<X>) = Jacobian<X, E, D6>(this, v1, v2, v3, v4, v5, v6)
  fun d(v1: Var<X>, v2: Var<X>, v3: Var<X>, v4: Var<X>, v5: Var<X>, v6: Var<X>, v7: Var<X>) = Jacobian<X, E, D7>(this, v1, v2, v3, v4, v5, v6, v7)
  fun d(v1: Var<X>, v2: Var<X>, v3: Var<X>, v4: Var<X>, v5: Var<X>, v6: Var<X>, v7: Var<X>, v8: Var<X>) = Jacobian<X, E, D8>(this, v1, v2, v3, v4, v5, v6, v7, v8)
  fun d(v1: Var<X>, v2: Var<X>, v3: Var<X>, v4: Var<X>, v5: Var<X>, v6: Var<X>, v7: Var<X>, v8: Var<X>, v9: Var<X>) = Jacobian<X, E, D9>(this, v1, v2, v3, v4, v5, v6, v7, v8, v9)
  fun d(vararg vars: Var<X>): Map<Var<X>, VFun<X, E>> = vars.map { it to VDerivative(this, it) }.toMap()
  fun grad(): Map<Var<X>, VFun<X, E>> = bindings.sVars.map { it to VDerivative(this, it) }.toMap()

  open operator fun unaryMinus(): VFun<X, E> = VNegative(this)
  open operator fun plus(addend: VFun<X, E>): VFun<X, E> = VSum(this, addend)
  open operator fun minus(subtrahend: VFun<X, E>): VFun<X, E> = VSum(this, -subtrahend)
  open infix fun ʘ(multiplicand: VFun<X, E>): VFun<X, E> = VVProd(this, multiplicand)
  open operator fun times(multiplicand: SFun<X>): VFun<X, E> = VSProd(this, multiplicand)
  open operator fun <Q: D1> times(multiplicand: MFun<X, Q, E>): VFun<X, E> = VMProd(this, multiplicand)
  open infix fun dot(multiplicand: VFun<X, E>): SFun<X> = DProd(this, multiplicand)

  open fun magnitude(): SFun<X> = VMagnitude(this)

  override fun toGraph(): MutableNode = Factory.mutNode(if (this is VVar) "VVar($name)" else "${hashCode()}").apply {
    when (this@VFun) {
      is VVar -> "$name-Vec$length"
      is Gradient -> { fn.toGraph() - this; Factory.mutNode("$this").apply { add(Label.of(vVar.toString())) } - this; add(Label.of("grad")) }
      is Vec -> { contents.map { it.toGraph() - this } }
      is BiFun<*> -> { (left.toGraph() - this).add(Color.BLUE); (right.toGraph() - this).add(Color.RED); add(Label.of(opCode())) }
      is UnFun<*> -> { input.toGraph() - this; add(Label.of(opCode())) }
      else -> TODO(this@VFun.javaClass.toString())
    }
  }

  override fun toString() = when (this) {
    is Vec -> contents.joinToString(", ", "[", "]")
    is VDerivative -> "d($vFun) / d($v1)"//d(${v1.joinToString(", ")})"
    is BiFun<*> -> "($left) ${opCode()} ($right)"
    is UnFun<*> -> "${opCode()}($input)"
    is Gradient -> "($fn).d($vVar)"
    is VMap -> "$input.map { $ef }"
    is VVar -> "VVar($name)"
    is VComposition -> "($vFun)$inputs"
    else -> TODO(this.javaClass.name)
  }
}

class VNegative<X: SFun<X>, E: D1>(override val input: VFun<X, E>): VFun<X, E>(input), UnFun<X>
class VMap<X: SFun<X>, E: D1>(override val input: VFun<X, E>, val ef: (SFun<X>) -> SFun<X>): VFun<X, E>(input), UnFun<X>

class VSum<X: SFun<X>, E: D1>(override val left: VFun<X, E>, override val right: VFun<X, E>): VFun<X, E>(left, right), BiFun<X>

class VVProd<X: SFun<X>, E: D1>(override val left: VFun<X, E>, override val right: VFun<X, E>): VFun<X, E>(left, right), BiFun<X>
class SVProd<X: SFun<X>, E: D1>(override val left: SFun<X>, override val right: VFun<X, E>): VFun<X, E>(left, right), BiFun<X>
class VSProd<X: SFun<X>, E: D1>(override val left: VFun<X, E>, override val right: SFun<X>): VFun<X, E>(left, right), BiFun<X>
class MVProd<X: SFun<X>, R: D1, C: D1>(override val left: MFun<X, R, C>, override val right: VFun<X, C>): VFun<X, R>(left, right), BiFun<X>
class VMProd<X: SFun<X>, R: D1, C: D1>(override val left: VFun<X, C>, override val right: MFun<X, R, C>): VFun<X, C>(left, right), BiFun<X>

class VDerivative<X : SFun<X>, E: D1>(val vFun: VFun<X, E>, val v1: Var<X>) : VFun<X, E>(vFun) {
  fun VFun<X, E>.df(): VFun<X, E> = when (this@df) {
    is VVar -> Gradient(v1, this@df).df()
    is VConst<X, E> -> Vec(consts.map { Zero() })
    is VSum -> left.df() + right.df()
    is VVProd -> left.df() ʘ right + left ʘ right.df()
    is SVProd -> left.d(v1) * right + left * right.df()
    is VSProd -> left.df() * right + left * right.d(v1)
    is VNegative -> -input.df()
    is VDerivative -> vFun.df().df()
    is Vec -> Vec(contents.map { it.d(v1) })
    is MVProd<X, E, *> -> invoke().df()
    is VMProd<X, *, E> -> invoke().df()
    is Gradient -> invoke()
    is VMap -> input.df().map(ef).map { it * ef(it) } // Chain rule
    is VComposition -> evaluate.df()
  }
}

class Gradient<X : SFun<X>, E: D1>(val fn: SFun<X>, val vVar: VVar<X, E>): VFun<X, E>(fn) {
  fun df() = fn.df()
  fun SFun<X>.df(): VFun<X, E> = when (this@df) {
    is Var -> Vec(List(vVar.length.i) { if(this@df == vVar.sVars[it]) One() else Zero() })
    is SConst -> Vec(List(vVar.length.i) { Zero() })
    is Sum -> left.df() + right.df()
    is Prod -> left.df() * right + left * right.df()
    is Power ->
      if (right.bindings.sVars.isEmpty()) right * left.pow(right - One()) * left.df()
      else (left.df() * right * (One<X>() / left) + right.df() * left.ln())
    is Negative -> -input.df()
    is Log -> (left pow -One<X>()) * left.df()
    is DProd -> left as VFun<X, E> //invoke().df()
    is VMagnitude -> value as VFun<X, E>//invoke().df()
    is Composition -> evaluate.df()
    else -> TODO(this@df.javaClass.name)
  }
}

class VVar<X: SFun<X>, E: D1>(
  override val name: String = "",
  val length: E,
  val sVars: Vec<X, E> = Vec(List(length.i) { Var("$name.$it") })
): Variable<X>, VFun<X, E>() {
  override val bindings: Bindings<X> = Bindings(mapOf(this to this))
}

class Jacobian<X : SFun<X>, R: D1, C: D1>(val vfn: VFun<X, R>, vararg val vrbs: Var<X>): MFun<X, R, C>(vfn) {
  override fun invoke(newBindings: Bindings<X>) = Mat<X, C, R>(vrbs.map { VDerivative(vfn, it)() }).ᵀ(newBindings)
}

class VComposition<X: SFun<X>, E: D1>(val vFun: VFun<X, E>, val inputs: Bindings<X>): VFun<X, E>(vFun.bindings + inputs) {
  val evaluate: VFun<X, E> by lazy { bind(bindings) }

  @Suppress("UNCHECKED_CAST")
  fun VFun<X, E>.bind(bindings: Bindings<X>): VFun<X, E> =
    bindings[this@bind] ?: when (this@bind) {
      is VVar<X, E> -> this@bind
      is VConst<X, E> -> this@bind
      is Vec<X, E> -> map { it(bindings) }
      is VNegative<X, E> -> -input(bindings)
      is VSum<X, E> -> left.bind(bindings) + right.bind(bindings)
      is VVProd<X, E> -> left.bind(bindings) ʘ right.bind(bindings)
      is SVProd<X, E> -> left(bindings) * right.bind(bindings)
      is VSProd<X, E> -> left.bind(bindings) * right(bindings)
      is VDerivative -> df().bind(bindings)
      is Gradient -> df().bind(bindings)
      is MVProd<X, *, *> -> left(bindings) as MFun<X, E, E> * (right as VFun<X, E>).bind(bindings)
      is VMProd<X, *, *> -> (left as Vec<X, E>).bind(bindings) * (right as MFun<X, E, E>)(bindings)
      is VMap<X, E> -> input.bind(bindings).map { ef(it)(bindings) }
      is VComposition -> vFun.bind(bindings)
    }
}

open class VConst<X: SFun<X>, E: D1>(vararg val consts: SConst<X>): Vec<X, E>(consts.toList())

open class Vec<X: SFun<X>, E: D1>(val contents: List<SFun<X>>):
  VFun<X, E>(*contents.toTypedArray()), Iterable<SFun<X>> by contents {
  val size = contents.size

  override fun toString() = contents.joinToString(", ", "[", "]")

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

  override fun times(multiplicand: SFun<X>): Vec<X, E> = map { it * multiplicand }

  override fun dot(multiplicand: VFun<X, E>) = when(multiplicand) {
    is Vec<X, E> -> mapIndexed { i, v -> v * multiplicand[i] }.reduce { acc, it -> acc + it }
    else -> super.dot(multiplicand)
  }

  override fun map(ef: (SFun<X>) -> SFun<X>): Vec<X, E> = Vec(contents.map { ef(it) })

  override fun magnitude() = map { it * it }.reduce { acc, p -> acc + p }.sqrt()

  override fun unaryMinus(): Vec<X, E> = map { -it }

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
sealed class D0(open val i: Int = 0) {
  companion object: D0(), Nat<D0>

  override fun toString() = "$i"
}

sealed class D1(override val i: Int = 1): D0(i) { companion object: D1(), Nat<D1> }
sealed class D2(override val i: Int = 2): D1(i) { companion object: D2(), Nat<D2> }
sealed class D3(override val i: Int = 3): D2(i) { companion object: D3(), Nat<D3> }
sealed class D4(override val i: Int = 4): D3(i) { companion object: D4(), Nat<D4> }
sealed class D5(override val i: Int = 5): D4(i) { companion object: D5(), Nat<D5> }
sealed class D6(override val i: Int = 6): D5(i) { companion object: D6(), Nat<D6> }
sealed class D7(override val i: Int = 7): D6(i) { companion object: D7(), Nat<D7> }
sealed class D8(override val i: Int = 8): D7(i) { companion object: D8(), Nat<D8> }
sealed class D9(override val i: Int = 9): D8(i) { companion object: D9(), Nat<D9> }