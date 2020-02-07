@file:Suppress("ClassName", "LocalVariableName", "NonAsciiCharacters", "FunctionName", "MemberVisibilityCanBePrivate", "UNUSED_VARIABLE")
package edu.umontreal.kotlingrad.experimental

import guru.nidi.graphviz.attribute.Color.*
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.minus
import guru.nidi.graphviz.model.Factory.*
import guru.nidi.graphviz.model.MutableNode
import java.lang.ClassCastException

/**
 * Vector function.
 */

sealed class VFun<X: SFun<X>, E: D1>(override val bindings: Bindings<X>): Fun<X> {
  constructor(vararg funs: Fun<X>): this(Bindings(*funs))

  @Suppress("UNCHECKED_CAST")
  override fun invoke(newBindings: Bindings<X>): VFun<X, E> =
    VComposition(this, newBindings).run { if (bindings.complete || newBindings.readyToBind || EAGER) evaluate else this }

  // Materializes the concrete vector from the dataflow graph
  operator fun invoke(): Vec<X, E> =
    invoke(Bindings()).let {
      try {
        it as Vec<X, E>
      } catch (e: ClassCastException) {
        show("before")
        it.show("after")
        e.printStackTrace()
        throw NumberFormatException("Vector function has unbound free variables: ${bindings.allFreeVariables.keys}")
      }
    }

  val mapInput = SVar<X>("mapInput")
  open fun map(ef: (SFun<X>) -> SFun<X>): VFun<X, E> = VMap(this, ef(mapInput), mapInput)
  open fun <C: D1> vMap(ef: (SFun<X>) -> VFun<X, C>): MFun<X, E, C> = VVMap(this, ef(mapInput), mapInput)

  fun <Q: D1> d(v1: VVar<X, Q>): MFun<X, E, Q> = Jacobian(this, v1)//.let { if(EAGER) it.df() else it }

  fun d(v1: SVar<X>) = VDerivative(this, v1)//.let { if (EAGER) it.df() else it }
  fun d(v1: SVar<X>, v2: SVar<X>) = Jacobian(this, VVar("j2", D2, Vec(v1, v2)))
  fun d(v1: SVar<X>, v2: SVar<X>, v3: SVar<X>) = Jacobian(this, VVar("j3", D3, Vec(v1, v2, v3)))
  fun d(v1: SVar<X>, v2: SVar<X>, v3: SVar<X>, v4: SVar<X>) = Jacobian(this, VVar("j4", D4, Vec(v1, v2, v3, v4)))
  fun d(v1: SVar<X>, v2: SVar<X>, v3: SVar<X>, v4: SVar<X>, v5: SVar<X>) = Jacobian(this, VVar("j5", D5, Vec(v1, v2, v3, v4, v5)))
  fun d(v1: SVar<X>, v2: SVar<X>, v3: SVar<X>, v4: SVar<X>, v5: SVar<X>, v6: SVar<X>) = Jacobian(this, VVar("j6", D6, Vec(v1, v2, v3, v4, v5, v6)))
  fun d(v1: SVar<X>, v2: SVar<X>, v3: SVar<X>, v4: SVar<X>, v5: SVar<X>, v6: SVar<X>, v7: SVar<X>) = Jacobian(this, VVar("j7", D7, Vec(v1, v2, v3, v4, v5, v6, v7)))
  fun d(v1: SVar<X>, v2: SVar<X>, v3: SVar<X>, v4: SVar<X>, v5: SVar<X>, v6: SVar<X>, v7: SVar<X>, v8: SVar<X>) = Jacobian(this, VVar("j8", D8, Vec(v1, v2, v3, v4, v5, v6, v7, v8)))
  fun d(v1: SVar<X>, v2: SVar<X>, v3: SVar<X>, v4: SVar<X>, v5: SVar<X>, v6: SVar<X>, v7: SVar<X>, v8: SVar<X>, v9: SVar<X>) = Jacobian(this, VVar("j9", D9, Vec(v1, v2, v3, v4, v5, v6, v7, v8, v9)))
  fun d(vararg vars: SVar<X>): Map<SVar<X>, VFun<X, E>> = vars.map { it to d(it) }.toMap()
  fun grad(): Map<SVar<X>, VFun<X, E>> = bindings.sVars.map { it to d(it) }.toMap()

  open operator fun unaryMinus(): VFun<X, E> = VNegative(this)
  open operator fun plus(addend: VFun<X, E>): VFun<X, E> = VSum(this, addend)
  open operator fun minus(subtrahend: VFun<X, E>): VFun<X, E> = VSum(this, -subtrahend)
  open infix fun ʘ(multiplicand: VFun<X, E>): VFun<X, E> = VVProd(this, multiplicand)
  open operator fun times(multiplicand: SFun<X>): VFun<X, E> = VSProd(this, multiplicand)
  open operator fun <Q: D1> times(multiplicand: MFun<X, Q, E>): VFun<X, E> = VMProd(this, multiplicand)
  open infix fun dot(multiplicand: VFun<X, E>): SFun<X> = DProd(this, multiplicand)

  open fun sum(): SFun<X> = VSumAll(this)

  override fun toGraph(): MutableNode = mutNode(if (this is VVar) "VVar($name)" else "${hashCode()}").apply {
    when (this@VFun) {
      is VVar -> "$name-Vec$length"
      is Gradient -> { fn.toGraph() - this; mutNode("$this").apply { add(Label.of(vVar.toString())) } - this; add(Label.of("Gradient")) }
      is VDerivative -> { vFun.toGraph() - this; mutNode("$this").apply { add(Label.of(sVar.toString())) } - this; add(Label.of("VDerivative")) }
      is Vec -> { contents.map { it.toGraph() - this }; add(Label.of("Vec")) }
      is BiFun<*> -> { (left.toGraph() - this).add(BLUE); (right.toGraph() - this).add(RED); add(Label.of(opCode())) }
      is UnFun<*> -> { input.toGraph() - this; add(Label.of(opCode())) }
      is VMap<*, *> -> { (input.toGraph() - this).add(BLUE); (ssMap.toGraph() - this).add(RED); add(Label.of(opCode())) }
      is VComposition -> { vFun.toGraph() - this; mutNode("$this").apply { add(Label.of(bindings.allFreeVariables.keys.toString())) } - this; add(Label.of("VComp")) }
      else -> TODO(this@VFun.javaClass.toString())
    }
  }

  override fun toString() = when (this) {
    is Vec -> contents.joinToString(", ", "[", "]")
    is VDerivative -> "d($vFun) / d($sVar)"//d(${v1.joinToString(", ")})"
    is BiFun<*> -> "($left) ${opCode()} ($right)"
    is UnFun<*> -> "${opCode()}($input)"
    is Gradient -> "($fn).d($vVar)"
    is VMap -> "$input.map { $ssMap }"
    is VVar -> "VVar($name)"
    is VComposition -> "($vFun)$bindings"
    else -> javaClass.simpleName
  }
}

class VNegative<X: SFun<X>, E: D1>(override val input: VFun<X, E>): VFun<X, E>(input), UnFun<X>
class VMap<X: SFun<X>, E: D1>(override val input: VFun<X, E>, val ssMap: SFun<X>, placeholder: SVar<X>):
  VFun<X, E>(input.bindings + ssMap.bindings - placeholder), UnFun<X>
class VVMap<X: SFun<X>, R: D1, C: D1>(override val input: VFun<X, R>, val svMap: VFun<X, C>, placeholder: SVar<X>):
  MFun<X, R, C>(input.bindings + svMap.bindings - placeholder), UnFun<X>

class VSum<X: SFun<X>, E: D1>(override val left: VFun<X, E>, override val right: VFun<X, E>): VFun<X, E>(left, right), BiFun<X>

class VVProd<X: SFun<X>, E: D1>(override val left: VFun<X, E>, override val right: VFun<X, E>): VFun<X, E>(left, right), BiFun<X>
class SVProd<X: SFun<X>, E: D1>(override val left: SFun<X>, override val right: VFun<X, E>): VFun<X, E>(left, right), BiFun<X>
class VSProd<X: SFun<X>, E: D1>(override val left: VFun<X, E>, override val right: SFun<X>): VFun<X, E>(left, right), BiFun<X>
class MVProd<X: SFun<X>, R: D1, C: D1>(override val left: MFun<X, R, C>, override val right: VFun<X, C>): VFun<X, R>(left, right), BiFun<X>
class VMProd<X: SFun<X>, R: D1, C: D1>(override val left: VFun<X, C>, override val right: MFun<X, R, C>): VFun<X, C>(left, right), BiFun<X>
class MSumRows<X: SFun<X>, R: D1, C: D1>(override val input: MFun<X, R, C>): VFun<X, C>(input), UnFun<X>

class VDerivative<X : SFun<X>, E: D1>(val vFun: VFun<X, E>, val sVar: SVar<X>) : VFun<X, E>(vFun) {
  fun df() = vFun.df()
  private fun VFun<X, E>.df(): VFun<X, E> = when (this@df) {
    is VVar -> sVars.map { it.d(sVar) }
    is VConst<X, E> -> map { Zero() }
    is VSum -> left.df() + right.df()
    is VVProd -> left.df() ʘ right + left ʘ right.df()
    is SVProd -> left.d(sVar) * right + left * right.df()
    is VSProd -> left.df() * right + left * right.d(sVar)
    is VNegative -> -input.df()
    is VDerivative -> vFun.df()
    is Vec -> Vec(contents.map { it.d(sVar) })
    is MVProd<X, *, *> ->
      (left.d(sVar) as MFun<X, E, E>) * right as VFun<X, E> +
      (left as MFun<X, E, E> * right.d(sVar))
    is VMProd<X, *, *> ->
      (left.d(sVar) as VFun<X, E> * right as MFun<X, E, E>) +
      (left as VFun<X, E> * right.d(sVar))
    is Gradient -> invoke().df() // map { it.d(sVar) }
    is VMap -> input.df().map { it * ssMap(mapInput to it).d(sVar) } // Chain rule
    is VComposition -> evaluate.df()
    else -> TODO(this@df.javaClass.name)
  }
}

class Gradient<X : SFun<X>, E: D1>(val fn: SFun<X>, val vVar: VVar<X, E>): VFun<X, E>(fn) {
  fun df() = fn.df()
  private fun SFun<X>.df(): VFun<X, E> = when (this@df) {
    is SVar -> vVar.sVars.map { if(this@df == it) One() else Zero() }
    is SConst -> vVar.sVars.map { Zero() }
    is Sum -> left.df() + right.df()
    is Prod -> left.df() * right + left * right.df()
    is Power ->
      if (right.isConstant()) right * left.pow(right - One()) * left.df()
      else (left.df() * right * (One<X>() / left) + right.df() * left.ln())
    is Negative -> -input.df()
    is Log -> (left pow -One<X>()) * left.df()
    is DProd ->
      (left.d(vVar) as MFun<X, E, E> * right as VFun<X, E>) +
      (left as VFun<X, E> * right.d(vVar))
    is SComposition -> evaluate.df()
    is VSumAll<*, *> -> (input as VFun<X, E>).d(vVar).sum()
    else -> TODO(this@df.javaClass.name)
  }
}

class VVar<X: SFun<X>, E: D1>(
  override val name: String = "",
  val length: E,
//  val svs: List<SVar<X>> = List(length.i) { SVar("$name.$it") },
  val sVars: Vec<X, E> = Vec(List(length.i) { SVar("$name[$it]") })
): Variable<X>, VFun<X, E>() {
  override val bindings: Bindings<X> = Bindings(mapOf(this to sVars))
  override fun equals(other: Any?) = other is VVar<*, *> && name == other.name
  override fun hashCode(): Int = name.hashCode()
}

class VComposition<X: SFun<X>, E: D1>(val vFun: VFun<X, E>, val inputs: Bindings<X>): VFun<X, E>(vFun.bindings + inputs) {
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
      is VComposition -> vFun.bind(bnds)
      is MSumRows<X, *, *> -> input(bnds).sum() as VFun<X, E>
      else -> TODO(this@bind.toString() + "/" + bnds)
    }.let { result ->
      val freeVars = result.bindings.allFreeVariables.keys
      val boundVars = bnds.allBoundVariables
      val unpropagated = freeVars.filter { it in boundVars }
      if (unpropagated.isNotEmpty()) {
          show("input"); result.show("result")
          println("Bindings were $bnds")
        (result as Vec).contents.forEach { println(it) }
          throw Exception("Bindings included unpropagated variables: $unpropagated")
      } else result
    }
}

open class VConst<X: SFun<X>, E: D1>(vararg val consts: SConst<X>): Vec<X, E>(consts.toList()), Constant<X>

open class Vec<X: SFun<X>, E: D1>(val contents: List<SFun<X>>):
  VFun<X, E>(*contents.toTypedArray()), Iterable<SFun<X>> by contents {
  constructor(len: Int, gen: () -> SFun<X>): this(List(len) { gen() })
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

  override fun times(multiplicand: SFun<X>) = map { it * multiplicand }

  override fun dot(multiplicand: VFun<X, E>) = when(multiplicand) {
    is Vec<X, E> -> mapIndexed { i, v -> v * multiplicand[i] }.reduce { acc, it -> acc + it }
    else -> super.dot(multiplicand)
  }

  override operator fun <Q: D1> times(multiplicand: MFun<X, Q, E>): VFun<X, E> = when(multiplicand) {
    is Mat<X, Q, E> -> Vec(multiplicand.rows.map { r -> r dot this})
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