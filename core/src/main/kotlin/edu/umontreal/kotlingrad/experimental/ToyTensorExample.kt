package edu.umontreal.kotlingrad.experimental

fun main() {
  val v = Vt(1, 2) + Vt(1, 3)
//  val w = Vt(1, 2) + Vt(0, 0, 0)

  val m = Mt1x2(0, 0) * Mt2x1(0, 0)
//  val n = Mt1x2(0, 0) * Mt1x2(0, 0)
}

open class Vt<E, Len: D1> constructor(val contents: List<E>) {
  companion object {
    operator fun <T> invoke(t: T): Vt<T, D1> = Vt(listOf(t))
    operator fun <T> invoke(t0: T, t1: T): Vt<T, D2> = Vt(listOf(t0, t1))
    operator fun <T> invoke(t0: T, t1: T, t2: T): Vt<T, D3> = Vt(listOf(t0, t1, t2))
  }
}

operator fun <E, C: D1, V: Vt<E, C>> V.plus(v: V): V = TODO()

open class Mt<X, R: D1, C: D1>(vararg val rows: Vt<X, C>)
fun <X> Mt1x2(d0: X, d1: X): Mt<X, D1, D2> = Mt(Vt(d0, d1))
fun <X> Mt2x1(d0: X, d1: X): Mt<X, D2, D1> = Mt(Vt(d0), Vt(d1))

operator fun <X, Q: D1, R: D1, S: D1> Mt<X, Q, R>.times(m: Mt<X, R, S>): Mt<X, Q, S> = TODO()