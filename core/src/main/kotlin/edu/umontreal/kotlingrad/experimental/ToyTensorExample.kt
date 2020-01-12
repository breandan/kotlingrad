package edu.umontreal.kotlingrad.experimental

fun main() {
  val v: Vt<Int, N2> = Vt(1, 2) + Vt(1, 3)
//val w = Vt(1, 2) + Vt(0, 0, 0)

  val m: Mt<Int, N1, N1> = Mt1x2(0, 0) * Mt2x1(0, 0)
//val n = Mt1x2(0, 0) * Mt1x2(0, 0)

  val q: Vt<Int, N3> = Vt(0) cc Vt(0, 0)

  val s: Vt<Int, N2> = Vt(0, 0, 0).rev().take(T2)
  val z: Vt<Int, N1> = s.take(T1)

  val y: Int = z[T1]
//val x = z[T2]
}

open class Vt<E, L: N9> constructor(val contents: List<E>) {
  companion object {
    operator fun <T> invoke(t: T): Vt<T, N1> = Vt(listOf(t))
    operator fun <T> invoke(t0: T, t1: T): Vt<T, N2> = Vt(listOf(t0, t1))
    operator fun <T> invoke(t0: T, t1: T, t2: T): Vt<T, N3> = Vt(listOf(t0, t1, t2))
  }

  operator fun <T: L> get(t: T): E = contents[t.i - 1]
  fun <T: L> take(t: T): Vt<E, T> = Vt(contents.take(t.i))
  
  fun rev() = Vt<E, L>(contents.asReversed())
}

@JvmName("vt1ccvt1") infix fun <E> Vt<E, N1>.cc(v: Vt<E, N1>): Vt<E, N2> = TODO()
@JvmName("vt1ccvt2") infix fun <E> Vt<E, N1>.cc(v: Vt<E, N2>): Vt<E, N3> = TODO()
@JvmName("vt2ccvt1") infix fun <E> Vt<E, N2>.cc(v: Vt<E, N1>): Vt<E, N3> = TODO()

operator fun <E, C: N9, V: Vt<E, C>> V.plus(v: V): V = TODO()

open class Mt<X, R: N9, C: N9>(vararg val vecs: Vt<X, C>)

fun <X> Mt1x1(d0: X): Mt<X, N1, N1> = Mt(Vt(d0))
fun <X> Mt1x2(d0: X, d1: X): Mt<X, N1, N2> = Mt(Vt(d0, d1))
fun <X> Mt2x1(d0: X, d1: X): Mt<X, N2, N1> = Mt(Vt(d0), Vt(d1))

operator fun <X, Q: N9, R: N9, S: N9> Mt<X, Q, R>.times(m: Mt<X, R, S>): Mt<X, Q, S> = TODO()

open class Cb<X, M: N9, R: N9, C: N9>(vararg val mats: Mt<X, R, C>)

fun <X> Cb1x1x2(d0: X, d1: X): Cb<X, N2, N1, N1> = Cb(Mt1x1(d0), Mt1x1(d1))
fun <X> Cb1x2x1(d0: X, d1: X): Cb<X, N1, N1, N2> = Cb(Mt1x2(d0, d1))
fun <X> Cb2x1x1(d0: X, d1: X): Cb<X, N1, N2, N1> = Cb(Mt2x1(d0, d1))

infix fun <X, M: N9, R: N9, C: N9, C1: N9> Cb<X, M, R, C>.c3(cb: Cb<X, M, C, C1>): Cb<X, M, R, C1> = TODO()

open class N9(open val i: Int = 0) {
  override fun toString() = "$i"
}
open class N8(override val i: Int = 1): N9(i)
open class N7(override val i: Int = 2): N8(i)
open class N6(override val i: Int = 3): N7(i)
open class N5(override val i: Int = 4): N6(i)
open class N4(override val i: Int = 5): N5(i)
open class N3(override val i: Int = 6): N4(i)
open class N2(override val i: Int = 7): N3(i)
open class N1(override val i: Int = 8): N2(i)

val T1 = N1()
val T2 = N2()
val T3 = N3()
val T4 = N4()
val T5 = N5()
val T6 = N6()
val T7 = N7()
val T8 = N8()
val T9 = N9()