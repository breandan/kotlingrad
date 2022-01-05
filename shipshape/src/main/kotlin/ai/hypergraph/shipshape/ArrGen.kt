package ai.hypergraph.shipshape

import org.intellij.lang.annotations.Language

@Language("kt")
fun genArrays() = """
// This file was generated by Shipshape
@file:Suppress("UNUSED_PARAMETER", "UNCHECKED_CAST")

package ai.hypergraph.kotlingrad.typelevel.arrays
  
import kotlin.jvm.JvmName

// Multi-typed arrays
data class Y1<A>(val e1: A)
data class Y2<A, B>(val e1: A, val e2: B)
data class Y3<A, B, C>(val e1: A, val e2: B, val e3: C)
data class Y4<A, B, C, D>(val e1: A, val e2: B, val e3: C, val e4: D)

open class VT<E, L: S<*>> internal constructor(val len: L, val a: List<E>): List<E> by a {
  internal constructor(l: L, vararg es: E): this(l, es.toList())

  internal fun <A: S<*>, B: S<*>> fetch(intRange: Y2<A, B>): List<E> = subList(intRange.e1.toInt(), intRange.e2.toInt())
}

/** TODO: Unify this representation with [ai.hypergraph.kaliningraph.tensor.Matrix] */
typealias Mat<E, R, C> = VT<VT<E, C>, R>

infix fun <T> T.cc(that: T) = VT(this, that)

fun <T1: VT<E, L>, E, L: S<*>> T1.append(that: E): VT<E, Q1<L>> = VT(this.len + S1, this.a + listOf(that))
fun <T1: VT<E, L>, E, L: S<*>> T1.prepend(that: E): VT<E, Q1<L>> = VT(this.len + S1, listOf(that) + this.a)

@JvmName("cc2") infix fun <T1: VT<E, L>, T2: VT<E, L1>, E, L: S<*>> T1.cc(that: T2): VT<E, Q1<L>> = VT(this.len + that.len, this.a + that.a)
@JvmName("cc3") infix fun <T1: VT<E, L>, T2: VT<E, L2>, E, L: S<*>> T1.cc(that: T2): VT<E, Q2<L>> = VT(this.len + that.len, this.a + that.a)
@JvmName("cc4") infix fun <T1: VT<E, L>, T2: VT<E, L3>, E, L: S<*>> T1.cc(that: T2): VT<E, Q3<L>> = VT(this.len + that.len, this.a + that.a)
@JvmName("cc5") infix fun <T1: VT<E, L>, T2: VT<E, L4>, E, L: S<*>> T1.cc(that: T2): VT<E, Q4<L>> = VT(this.len + that.len, this.a + that.a)
@JvmName("cc6") infix fun <T1: VT<E, L>, T2: VT<E, L5>, E, L: S<*>> T1.cc(that: T2): VT<E, Q5<L>> = VT(this.len + that.len, this.a + that.a)

fun <E> VT(v1: E) = VT(S1, v1)
fun <E> VT(v1: E, v2: E) = VT(S2, v1, v2)
fun <E> VT(v1: E, v2: E, v3: E) = VT(S3, v1, v2, v3)
fun <E> VT(v1: E, v2: E, v3: E, v4: E) = VT(S4, v1, v2, v3, v4)
fun <E> VT(v1: E, v2: E, v3: E, v4: E, v5: E) = VT(S5, v1, v2, v3, v4, v5)
fun <E> VT(v1: E, v2: E, v3: E, v4: E, v5: E, v6: E) = VT(S6, v1, v2, v3, v4, v5, v6)
fun <E> VT(v1: E, v2: E, v3: E, v4: E, v5: E, v6: E, v7: E) = VT(S7, v1, v2, v3, v4, v5, v6, v7)
fun <E> VT(v1: E, v2: E, v3: E, v4: E, v5: E, v6: E, v7: E, v8: E) = VT(S8, v1, v2, v3, v4, v5, v6, v7, v8)
fun <E> VT(v1: E, v2: E, v3: E, v4: E, v5: E, v6: E, v7: E, v8: E, v9: E) = VT(S9, v1, v2, v3, v4, v5, v6, v7, v8, v9)

typealias V1<E> = VT<E, L1>
typealias V2<E> = VT<E, L2>
typealias V3<E> = VT<E, L3>
typealias V4<E> = VT<E, L4>
typealias V5<E> = VT<E, L5>
typealias V6<E> = VT<E, L6>
typealias V7<E> = VT<E, L7>
typealias V8<E> = VT<E, L8>
typealias V9<E> = VT<E, L9>

fun <E, D1: S<*>, D2: S<*>> List<E>.chunked(d1: D1, d2: D2): List<VT<E, D2>> = chunked(d1.toInt()).map { VT(d2, it) }

inline fun <reified R: S<*>> asInt() = R::class.simpleName!!.drop(1).toInt()
fun <E, R: S<*>, C: S<*>> Mat(r: R, c: C, vararg es: E): Mat<E, R, C> = Mat(r, c, es.toList())
fun <E, R: S<*>, C: S<*>> Mat(r: R, c: C, es: List<E>): Mat<E, R, C> = Mat(r, es.chunked(r, c))
fun <E, R: S<*>, C: S<*>> Mat(r: R, c: C, f: (Int, Int) -> E): Mat<E, R, C> =
  Mat(r, c, allPairs(r.toInt(), c.toInt()).map { (r, c) -> f(r, c) })

fun <E> Mat2x1(t1: E, t2: E): Mat<E, L2, L1> = Mat(S2, S1, t1, t2)
fun <E> Mat1x2(t1: E, t2: E): Mat<E, L1, L2> = Mat(S1, S2, t1, t2)
//...Optional pseudoconstructors

operator fun <E, R: S<*>, C1: S<*>, C2: S<*>> Mat<E, R, C1>.times(that: Mat<E, C1, C2>): Mat<E, R, C2> = TODO()
operator fun <E, R: S<*>, C: S<*>> Mat<E, R, C>.get(r: Int, c: Int): E = a[r][c]
//fun <E, R: S<*>, C: S<*>> Mat<E, R, C>.transpose(): Mat<E, C, R> = Mat { r, c -> this[c][r]}

@JvmName("get1") operator fun <R, L : Q1<R>, E> VT<E, L>.get(i: L1) = a[0]
@JvmName("get2") operator fun <R, L : Q2<R>, E> VT<E, L>.get(i: L2) = a[1]
@JvmName("get3") operator fun <R, L : Q3<R>, E> VT<E, L>.get(i: L3) = a[2]
@JvmName("get4") operator fun <R, L : Q4<R>, E> VT<E, L>.get(i: L4) = a[3]
@JvmName("get5") operator fun <R, L : Q5<R>, E> VT<E, L>.get(i: L5) = a[4]
@JvmName("get6") operator fun <R, L : Q6<R>, E> VT<E, L>.get(i: L6) = a[5]
@JvmName("get7") operator fun <R, L : Q7<R>, E> VT<E, L>.get(i: L7) = a[6]
@JvmName("get8") operator fun <R, L : Q8<R>, E> VT<E, L>.get(i: L8) = a[7]
@JvmName("get9") operator fun <R, L : Q9<R>, E> VT<E, L>.get(i: L9) = a[8]

val <R, L : Q1<R>, E> VT<E, L>.first: E get() = component1()
val <R, L : Q2<R>, E> VT<E, L>.second: E get() = component2()
val <R, L : Q3<R>, E> VT<E, L>.third: E get() = component3()

operator fun <T> Array<T>.get(range: IntRange) = sliceArray(range)

fun <E, Z : Q1<P>, P> VT<E, Z>.take1(): VT<E, L1> = VT(S1, fetch(S0..S1))
fun <E, Z : Q2<P>, P> VT<E, Z>.take2(): VT<E, L2> = VT(S2, fetch(S0..S2))
fun <E, Z : Q3<P>, P> VT<E, Z>.take3(): VT<E, L3> = VT(S3, fetch(S0..S3))
fun <E, Z : Q4<P>, P> VT<E, Z>.take4(): VT<E, L4> = VT(S4, fetch(S0..S4))

fun <E, Z : Q2<P>, P> VT<E, Z>.drop1(): VT<E, S<P>> = VT(len - S1, fetch(S1..len))
fun <E, Z : Q3<P>, P> VT<E, Z>.drop2(): VT<E, S<P>> = VT(len - S2, fetch(S2..len))
fun <E, Z : Q4<P>, P> VT<E, Z>.drop3(): VT<E, S<P>> = VT(len - S3, fetch(S3..len))
fun <E, Z : Q5<P>, P> VT<E, Z>.drop4(): VT<E, S<P>> = VT(len - S4, fetch(S4..len))

//                              ┌────j────┐    ┌────k────┐    where j, j are the relative offsets Y - X, Z - Y respectively
// Encodes the constraint:  P < X    <    Y && Y    <    Z    where X, Y are the start and end of range in a vector of length Z
@JvmName("sv121") operator fun <E, X: Q1<P>, Y: Q2<X>, Z : Q1<Y>, P> VT<E, Z>.get(r: Y2<X, Y>): VT<E, L2> = VT(S2, fetch(r))
@JvmName("sv122") operator fun <E, X: Q1<P>, Y: Q2<X>, Z : Q2<Y>, P> VT<E, Z>.get(r: Y2<X, Y>): VT<E, L2> = VT(S2, fetch(r))
@JvmName("sv221") operator fun <E, X: Q2<P>, Y: Q2<X>, Z : Q1<Y>, P> VT<E, Z>.get(r: Y2<X, Y>): VT<E, L2> = VT(S2, fetch(r))
@JvmName("sv222") operator fun <E, X: Q2<P>, Y: Q2<X>, Z : Q2<Y>, P> VT<E, Z>.get(r: Y2<X, Y>): VT<E, L2> = VT(S2, fetch(r))

operator fun <A, B> S<A>.rangeTo(that: S<B>) = Y2(this, that)
""".trimIndent()