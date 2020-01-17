@file:Suppress("ClassName", "LocalVariableName", "NonAsciiCharacters", "FunctionName", "MemberVisibilityCanBePrivate")
package edu.umontreal.kotlingrad.experimental

@Suppress("DuplicatedCode")
fun main() {
  with(DoublePrecision) {
    val f = x pow 2
    println(f(x to 3.0))
    println("f(x) = $f")
    val df_dx = f.d(x)
    println("f'(x) = $df_dx")

    val g = x pow x
    println("g(x) = $g")
    val dg_dx = g.d(x)
    println("g'(x) = $dg_dx")

    val h = x + y
    println("h(x) = $h")
    val dh_dx = h.d(x)
    println("h'(x) = $dh_dx")

    val vf1 = Vec(y + x, y * 2)
    println(vf1)
    val bh = x * vf1 + Vec(1.0, 3.0)
    println(bh(y to 2.0, x to 4.0))
    val vf2 = Vec(x, y)
    val q = vf1 + vf2 + Vec(0.0, 0.0)
    val z = q(x to 1.0).magnitude()(y to 2.0)
    println(z)

    val vf3 = vf2 ʘ Vec(x, x)
    val mf1 = vf3.d(x, y)
//    println(vf3.d(x)(y to 2.0))
  }
}

/**
 * Vector function.
 */



sealed class VFun<X: SFun<X>, E: D1>(override val inputs: Inputs<X>): Fun<X>, MFun<X, E, D1>(inputs) {
  constructor(vararg funs: Fun<X>): this(Inputs(*funs))

  override operator fun invoke(bnds: Bindings<X>): VFun<X, E> =
    when (this) {
      is Vec<X, E> -> Vec(contents.map { it(bnds) })
      is VNegative<X, E> -> -value(bnds)
      is VSum<X, E> -> left(bnds) + right(bnds)
      is VVProd<X, E> -> left(bnds) ʘ right(bnds)
      is SVProd<X, E> -> left(bnds) * right(bnds)
      is VSProd<X, E> -> left(bnds) * right(bnds)
      is VDerivative -> df()(bnds)
      is Gradient -> df()(bnds)
      is MVProd<X, *, *> -> left(bnds) as Mat<X, E, E> * (right as Vec<X, E>)(bnds)
      is VMProd<X, *, *> -> (left as Vec<X, E>)(bnds) * (right as Mat<X, E, E>)(bnds)
      is VMap<X, E> -> value(bnds).map(ef)
      else -> TODO(this::class.java.name)
    }

  // Materializes the concrete vector from the dataflow graph
  operator fun invoke(): Vec<X, E> = invoke(Bindings()) as Vec<X, E>

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
  //...
  fun d(vararg vars: Var<X>): Map<Var<X>, VFun<X, E>> = vars.map { it to VDerivative(this, it) }.toMap()
  fun grad(): Map<Var<X>, VFun<X, E>> = inputs.sVars.map { it to VDerivative(this, it) }.toMap()

  override operator fun unaryMinus(): VFun<X, E> = VNegative(this)
  open operator fun plus(addend: VFun<X, E>): VFun<X, E> = VSum(this, addend)
  open operator fun minus(subtrahend: VFun<X, E>): VFun<X, E> = VSum(this, -subtrahend)

  open infix fun ʘ(multiplicand: VFun<X, E>): VFun<X, E> = VVProd(this, multiplicand)
  override operator fun times(multiplicand: SFun<X>): VFun<X, E> = VSProd(this, multiplicand)
  open operator fun <Q: D1> times(multiplicand: MFun<X, Q, E>): VFun<X, E> = VMProd(this, multiplicand)//(expand * multiplicand).rows.first()
  open infix fun dot(multiplicand: VFun<X, E>): SFun<X> = DProd(this, multiplicand)

  open fun magnitude(): SFun<X> = VMagnitude(this)

  override fun toString() = when (this) {
    is Vec -> contents.joinToString(", ", "[", "]")
    is VSum -> "$left + $right"
    is VVProd -> "$left ʘ $right"
    is SVProd -> "$left * $right"
    is VSProd -> "$left * $right"
    is VNegative -> "-($value)"
    is VDerivative -> "d($vFun) / d($v1)"//d(${v1.joinToString(", ")})"
    is MVProd<X, E, *> -> "$left * $right"
    is VMProd<X, *, E> -> "$left * $right"
    is Gradient -> "($fn).d($vVar)"
    is VMap -> "$value.map { $ef }"
    is VVar -> "VVar($name)"
  }
}

class VNegative<X: SFun<X>, E: D1>(val value: VFun<X, E>): VFun<X, E>(value)
class VMap<X: SFun<X>, E: D1>(val value: VFun<X, E>, val ef: (SFun<X>) -> SFun<X>): VFun<X, E>(value)

class VSum<X: SFun<X>, E: D1>(val left: VFun<X, E>, val right: VFun<X, E>): VFun<X, E>(left, right)

class VVProd<X: SFun<X>, E: D1>(val left: VFun<X, E>, val right: VFun<X, E>): VFun<X, E>(left, right)
class SVProd<X: SFun<X>, E: D1>(val left: SFun<X>, val right: VFun<X, E>): VFun<X, E>(left, right)
class VSProd<X: SFun<X>, E: D1>(val left: VFun<X, E>, val right: SFun<X>): VFun<X, E>(left, right)
class MVProd<X: SFun<X>, R: D1, C: D1>(val left: MFun<X, R, C>, val right: VFun<X, C>): VFun<X, R>(left, right)
class VMProd<X: SFun<X>, R: D1, C: D1>(val left: VFun<X, C>, val right: MFun<X, R, C>): VFun<X, C>(left, right)

class Gradient<X : SFun<X>, E: D1>(val fn: SFun<X>, val vVar: VVar<X, E>): VFun<X, E>(fn) {
  fun df() = fn.df()
  fun SFun<X>.df(): VFun<X, E> = when (this@df) {
    is VVar<*, *> -> if (this == vVar) VOne() else VZero()
    is Var -> VZero()
    is SConst -> VZero()
    is Sum -> left.df() + right.df()
    is Prod -> left.df() * right + left * right.df()
    is Power -> this * (exponent * Log(base)).df()
    is Negative -> -value.df()
    is Log -> (logarithmand pow -One<X>()) * logarithmand.df()
//    is Derivative -> fn.df()
    is DProd -> this().df()
    is VMagnitude -> this().df()
    else -> TODO(this@df::class.java.name)
  }
}

class VVar<X: SFun<X>, E: D1>(override val name: String = ""): Variable, VFun<X, E>()
class Jacobian<X : SFun<X>, R: D1, C: D1>(val vfn: VFun<X, R>, vararg val vrbs: Var<X>): MFun<X, R, C>(vfn) {
  override fun invoke(bnds: Bindings<X>) = Mat<X, C, R>(vrbs.map { VDerivative(vfn, it)() }).ᵀ(bnds)
}

class VDerivative<X : SFun<X>, E: D1> internal constructor(val vFun: VFun<X, E>, val v1: Var<X>) : VFun<X, E>(vFun) {
  fun VFun<X, E>.df(): VFun<X, E> = when (this@df) {
    is VConst -> VZero()
    is VVar -> VZero()
    is VSum -> left.df() + right.df()
    is VVProd -> left.df() ʘ right + left ʘ right.df()
    is SVProd -> left.d(v1) * right + left * right.df()
    is VSProd -> left.df() * right + left * right.d(v1)
    is VNegative -> -value.df()
    is VDerivative -> vFun.df()
    is Vec -> Vec(contents.map { it.d(v1) })
    is MVProd<X, E, *> -> this().df()
    is VMProd<X, *, E> -> this().df()
    is Gradient -> this()
    is VMap -> this().df()
  }
}

open class VConst<X: SFun<X>, E: D1>(vararg contents: SConst<X>): Vec<X, E>(contents.asList())

class VZero<X: SFun<X>, E: D1>: VConst<X, E>()
class VOne<X: SFun<X>, E: D1>: VConst<X, E>()

open class Vec<X: SFun<X>, E: D1>(val contents: List<SFun<X>>): VFun<X, E>(*contents.toTypedArray()) {
  val size = contents.size
  val indices = contents.indices

  override fun toString() = contents.joinToString(", ", "[", "]")

  operator fun get(index: Int) = contents[index]

  override fun plus(addend: VFun<X, E>) = when (addend) {
    is Vec -> Vec(contents.mapIndexed { i, v -> v + addend.contents[i] })
    else -> super.plus(addend)
  }

  override fun minus(subtrahend: VFun<X, E>) = when (subtrahend) {
    is Vec -> Vec(contents.mapIndexed { i, v -> v - subtrahend.contents[i] })
    else -> super.minus(subtrahend)
  }

  override fun ʘ(multiplicand: VFun<X, E>) = when(multiplicand) {
    is Vec -> Vec(contents.mapIndexed { i, v -> v * multiplicand.contents[i] })
    else -> super.ʘ(multiplicand)
  }

  override fun times(multiplicand: SFun<X>): Vec<X, E> = Vec(contents.map { it * multiplicand })

  override fun dot(multiplicand: VFun<X, E>) = when(multiplicand) {
    is Vec -> contents.reduceIndexed { index, acc, element -> acc + element * multiplicand[index] }
    else -> super.dot(multiplicand)
  }

  override fun map(ef: (SFun<X>) -> SFun<X>): Vec<X, E> = Vec(contents.map { ef(it) })

  override fun magnitude() = contents.map { it * it }.reduce { acc, p -> acc + p }.sqrt()

  override fun unaryMinus(): Vec<X, E> = Vec(contents.map { -it })

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