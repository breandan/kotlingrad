@file:Suppress("ClassName", "LocalVariableName")
package edu.umontreal.kotlingrad.samples

@Suppress("DuplicatedCode")
fun main() {
  with(DoublePrecision) {
    val f = x pow 2
    println(f(x to 3.0))
    println("f(x) = $f")
    val df_dx = f.df(x)
    println("f'(x) = $df_dx")

    val g = x pow x
    println("g(x) = $g")
    val dg_dx = g.df(x)
    println("g'(x) = $dg_dx")

    val h = x + y
    println("h(x) = $h")
    val dh_dx = h.df(x)
    println("h'(x) = $dh_dx")

    val vf1 = Vec(y + x, y * 2)
    println(vf1)
    val bh = x * vf1 + Vec(1.0, 3.0)
    println(bh(y to 2.0, x to 4.0))
    val vf2 = Vec(x, y)
    val q = vf1 + vf2 + Vec(0.0, 0.0)
    val z = q(x to 1.0).magnitude()(y to 2.0)
    println(z)
  }
}

/**
 * Vector function.
 */

sealed class VFun<X: Fun<X>, E: `1`>(
  open val length: Nat<E>,
  open val sVars: Set<Var<X>> = emptySet()
//  ,open val vVars: Set<VVar<X, *>> = emptySet()
): (Bindings<X>) -> VFun<X, E> {
  constructor(length: Nat<E>, vararg vFns: Vec<X, E>): this(length, vFns.flatMap { it.sVars }.toSet()
//    , vFns.flatMap { it.vVars }.toSet()
  )

  constructor(length: Nat<E>, vararg vFns: VFun<X, E>): this(length, vFns.flatMap { it.sVars }.toSet()
    //, vFns.flatMap { it.vVars }.toSet()
  )
//  val expand: MFun<X, `1`, E> by lazy { MFun(`1`, length, this) }

  override operator fun invoke(bnds: Bindings<X>): VFun<X, E> =
    when (this) {
      is Vec<X, E> -> Vec(length, contents.map { it(bnds) })
      is VNegative<X, E> -> -value(bnds)
      is VSum<X, E> -> left(bnds) + right(bnds)
      is VVProd<X, E> -> left(bnds) * right(bnds)
      is SVProd<X, E> -> left(bnds) * right(bnds)
//      is VVar<X, E> -> bnds.vMap.getOrElse(this) { this } as VFun<X, E>
      is VDf -> ad()(bnds)
    }

  open fun diff(vararg variable: Var<X>): VFun<X, E> = VDf(this).apply()

  open operator fun unaryMinus(): VFun<X, E> = VNegative(this)
  open operator fun plus(addend: VFun<X, E>): VFun<X, E> = VSum(this, addend)
  open operator fun times(multiplicand: VFun<X, E>): VFun<X, E> = VVProd(this, multiplicand)
  open operator fun times(multiplicand: Fun<X>): VFun<X, E> = VVProd(this, Vec(length, *Array(length.i) { multiplicand }))
//  open operator fun <Q: `1`> times(multiplicand: MFun<X, E, Q>): VFun<X, Q> = (expand * multiplicand).rows.first()

  open infix fun dot(multiplicand: VFun<X, E>): Fun<X> = DProd(this, multiplicand)

  open fun magnitude(): Fun<X> = VMagnitude(this)

  override fun toString() =
    when (this) {
      is Vec -> contents.joinToString(", ", "[", "]")
      is VSum -> "$left + $right"
      is VVProd -> "$left * $right"
      is SVProd -> "$left * $right"
      is VNegative -> "-($value)"
      is VDf -> "d($vfn) / d(${vrbs.joinToString(", ")})"
    }
}

class VNegative<X: Fun<X>, E: `1`>(val value: VFun<X, E>): VFun<X, E>(value.length, value)
class VSum<X: Fun<X>, E: `1`>(val left: VFun<X, E>, val right: VFun<X, E>): VFun<X, E>(left.length, left, right)

class VVProd<X: Fun<X>, E: `1`>(val left: VFun<X, E>, val right: VFun<X, E>): VFun<X, E>(left.length, left, right)
class SVProd<X: Fun<X>, E: `1`>(val left: Fun<X>, val right: VFun<X, E>): VFun<X, E>(right.length, left.sVars + right.sVars
//  , right.vVars
)

//class VVar<X: Fun<X>, E: `1`>(override val name: String, override val length: Nat<E>): Variable, VFun<X, E>(length) { override val vVars: Set<VVar<X, *>> = setOf(this) }

class VDf<X : Fun<X>, E: `1`> internal constructor(val vfn: VFun<X, E>, vararg val vrbs: Var<X>) : VFun<X, E>(vfn.length, vfn) {
  init {
    vrbs.filter { it !in vfn.sVars }.let { require(it.isEmpty()) { "Variables: $it not in function!" } }
  }

  fun apply() = vfn.ad()

  fun VFun<X, E>.ad(): VFun<X, E> = when (this) {
    is RealVector -> VZero(length)
//    is VVar -> VOne(length)
    is VSum -> left.ad() + right.ad()
    is VVProd -> left.ad() * right + right.ad() * left
    is SVProd -> left.df(*vrbs) * right + right.ad() * left
    is VNegative -> -value.ad()
    is VDf -> vfn.ad()
    is Vec -> Vec(length, contents.map { it.df(*vrbs) })
  }
}

open class Vec<X: Fun<X>, E: `1`>(override val length: Nat<E>,
                                  override val sVars: Set<Var<X>> = emptySet(),
                                  open vararg val contents: Fun<X>): VFun<X, E>(length) {
  constructor(length: Nat<E>, contents: List<Fun<X>>): this(length, contents.flatMap { it.sVars }.toSet(), *contents.toTypedArray())
  constructor(length: Nat<E>, vararg contents: Fun<X>): this(length, contents.flatMap { it.sVars }.toSet(), *contents)

  init {
    require(length.i == contents.size || contents.isEmpty()) { "Declared length, $length != ${contents.size}" }
  }

  override fun toString() = contents.joinToString(", ", "[", "]")

  operator fun get(index: Int) = contents[index]

  override fun plus(addend: VFun<X, E>) = when (addend) {
    is Vec -> Vec(length, contents.mapIndexed { i, v -> v + addend.contents[i] })
    else -> super.plus(addend)
  }

  override fun times(multiplicand: VFun<X, E>) = when(multiplicand) {
    is Vec -> Vec(length, contents.mapIndexed { i, v -> v * multiplicand.contents[i] })
    else -> super.times(multiplicand)
  }

  override fun times(multiplicand: Fun<X>) = Vec(length, contents.map {it * multiplicand})

  override fun dot(multiplicand: VFun<X, E>) = when(multiplicand) {
    is Vec -> contents.reduceIndexed { index, acc, element -> acc + element * multiplicand[index] }
    else -> super.dot(multiplicand)
  }

  override fun magnitude() = contents.reduce { acc, p -> acc + p * p }.sqrt()

  override fun unaryMinus() = Vec(length, contents.map { -it })

  companion object {
    operator fun <T: Fun<T>> invoke(t: SConst<T>): VConst<T, `1`> = VConst(`1`, t)
    operator fun <T: Fun<T>> invoke(s0: SConst<T>, s1: SConst<T>): VConst<T, `2`> = VConst(`2`, s0, s1)
    operator fun <T: Fun<T>> invoke(s0: SConst<T>, s1: SConst<T>, s2: SConst<T>): VConst<T, `3`> = VConst(`3`, s0, s1, s2)
    operator fun <T: Fun<T>> invoke(s0: SConst<T>, s1: SConst<T>, s2: SConst<T>, s3: SConst<T>): VConst<T, `4`> = VConst(`4`, s0, s1, s2, s3)
    operator fun <T: Fun<T>> invoke(s0: SConst<T>, s1: SConst<T>, s2: SConst<T>, s3: SConst<T>, s4: SConst<T>): VConst<T, `5`> = VConst(`5`, s0, s1, s2, s3, s4)
    operator fun <T: Fun<T>> invoke(s0: SConst<T>, s1: SConst<T>, s2: SConst<T>, s3: SConst<T>, s4: SConst<T>, s5: SConst<T>): VConst<T, `6`> = VConst(`6`, s0, s1, s2, s3, s4, s5)
    operator fun <T: Fun<T>> invoke(s0: SConst<T>, s1: SConst<T>, s2: SConst<T>, s3: SConst<T>, s4: SConst<T>, s5: SConst<T>, s6: SConst<T>): VConst<T, `7`> = VConst(`7`, s0, s1, s2, s3, s4, s5, s6)
    operator fun <T: Fun<T>> invoke(s0: SConst<T>, s1: SConst<T>, s2: SConst<T>, s3: SConst<T>, s4: SConst<T>, s5: SConst<T>, s6: SConst<T>, s7: SConst<T>): VConst<T, `8`> = VConst(`8`, s0, s1, s2, s3, s4, s5, s6, s7)
    operator fun <T: Fun<T>> invoke(s0: SConst<T>, s1: SConst<T>, s2: SConst<T>, s3: SConst<T>, s4: SConst<T>, s5: SConst<T>, s6: SConst<T>, s7: SConst<T>, s8: SConst<T>): VConst<T, `9`> = VConst(`9`, s0, s1, s2, s3, s4, s5, s6, s7, s8)

    operator fun <T: Fun<T>> invoke(t: Fun<T>): Vec<T, `1`> = Vec(`1`, arrayListOf(t))
    operator fun <T: Fun<T>> invoke(t0: Fun<T>, t1: Fun<T>): Vec<T, `2`> = Vec(`2`, arrayListOf(t0, t1))
    operator fun <T: Fun<T>> invoke(t0: Fun<T>, t1: Fun<T>, t2: Fun<T>): Vec<T, `3`> = Vec(`3`, arrayListOf(t0, t1, t2))
    operator fun <T: Fun<T>> invoke(t0: Fun<T>, t1: Fun<T>, t2: Fun<T>, t3: Fun<T>): Vec<T, `4`> = Vec(`4`, arrayListOf(t0, t1, t2, t3))
    operator fun <T: Fun<T>> invoke(t0: Fun<T>, t1: Fun<T>, t2: Fun<T>, t3: Fun<T>, t4: Fun<T>): Vec<T, `5`> = Vec(`5`, arrayListOf(t0, t1, t2, t3, t4))
    operator fun <T: Fun<T>> invoke(t0: Fun<T>, t1: Fun<T>, t2: Fun<T>, t3: Fun<T>, t4: Fun<T>, t5: Fun<T>): Vec<T, `6`> = Vec(`6`, arrayListOf(t0, t1, t2, t3, t4, t5))
    operator fun <T: Fun<T>> invoke(t0: Fun<T>, t1: Fun<T>, t2: Fun<T>, t3: Fun<T>, t4: Fun<T>, t5: Fun<T>, t6: Fun<T>): Vec<T, `7`> = Vec(`7`, arrayListOf(t0, t1, t2, t3, t4, t5, t6))
    operator fun <T: Fun<T>> invoke(t0: Fun<T>, t1: Fun<T>, t2: Fun<T>, t3: Fun<T>, t4: Fun<T>, t5: Fun<T>, t6: Fun<T>, t7: Fun<T>): Vec<T, `8`> = Vec(`8`, arrayListOf(t0, t1, t2, t3, t4, t5, t6, t7))
    operator fun <T: Fun<T>> invoke(t0: Fun<T>, t1: Fun<T>, t2: Fun<T>, t3: Fun<T>, t4: Fun<T>, t5: Fun<T>, t6: Fun<T>, t7: Fun<T>, t8: Fun<T>): Vec<T, `9`> = Vec(`9`, arrayListOf(t0, t1, t2, t3, t4, t5, t6, t7, t8))
  }
}

open class VConst<X: Fun<X>, E: `1`>(length: Nat<E>, vararg contents: SConst<X>): Vec<X, E>(length, emptySet(), *contents)

class VZero<X: Fun<X>, E: `1`>(length: Nat<E>): VConst<X, E>(length)
class VOne<X: Fun<X>, E: `1`>(length: Nat<E>): VConst<X, E>(length)

abstract class RealVector<X: Fun<X>, E: `1`>(length: Nat<E>): Vec<X, E>(length)
class VDoubleReal<E: `1`>(length: Nat<E>): RealVector<DoubleReal, E>(length)

/**
 * Type level integers.
 */

interface Nat<T: `0`> { val i: Int }
sealed class `0`(open val i: Int = 0) {
  companion object: `0`(), Nat<`0`>

  override fun toString() = "$i"
}

sealed class `1`(override val i: Int = 1): `0`(i) { companion object: `1`(), Nat<`1`> }
sealed class `2`(override val i: Int = 2): `1`(i) { companion object: `2`(), Nat<`2`> }
sealed class `3`(override val i: Int = 3): `2`(i) { companion object: `3`(), Nat<`3`> }
sealed class `4`(override val i: Int = 4): `3`(i) { companion object: `4`(), Nat<`4`> }
sealed class `5`(override val i: Int = 5): `4`(i) { companion object: `5`(), Nat<`5`> }
sealed class `6`(override val i: Int = 6): `5`(i) { companion object: `6`(), Nat<`6`> }
sealed class `7`(override val i: Int = 7): `6`(i) { companion object: `7`(), Nat<`7`> }
sealed class `8`(override val i: Int = 8): `7`(i) { companion object: `8`(), Nat<`8`> }
sealed class `9`(override val i: Int = 9): `8`(i) { companion object: `9`(), Nat<`9`> }