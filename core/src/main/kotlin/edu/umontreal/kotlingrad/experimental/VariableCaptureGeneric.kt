//package edu.umontreal.kotlingrad.experimental

abstract class T
abstract class F
interface Eq<A, B, C> { fun call(vararg vrb: Vr) = 0 }
class Eqn<A, B, C>(prev: Eq<*, *, *>? = null, vararg vrb: Vr): Eq<A, B, C> {
  override fun call(vararg vrb: Vr) = 0
}

operator fun <V1, V2> Eq<*, V1, V2>.plus(x: x): Eq<T, V1, V2> = Eqn()
operator fun <V1, V2> Eq<V1, *, V2>.plus(y: y): Eq<V1, T, V2> = Eqn()
operator fun <V1, V2> Eq<V1, V2, *>.plus(z: z): Eq<V1, V2, T> = Eqn()

operator fun <V1, V2, V3> Eq<V1, V2, V3>.plus(c: Number): Eq<V1, V2, V3> = this
operator fun <V1, V2, V3> Int.plus(e: Eq<V1, V2, V3>): Eq<V1, V2, V3> = e

@JvmName("tff") operator fun Eq<T, F, F>.invoke(xBnd: Int) = call(x(xBnd))
@JvmName("ftf") operator fun Eq<F, T, F>.invoke(yBnd: Int) = call(y(yBnd))
@JvmName("fft") operator fun Eq<F, F, T>.invoke(zBnd: Int) = call(z(zBnd))

@JvmName("ftt") operator fun Eq<F, T, T>.invoke(yBnd: YBd) = Eqn<F, F, T>(this, yBnd)
@JvmName("ftt") operator fun Eq<F, T, T>.invoke(zBnd: ZBd) = Eqn<F, T, F>(this, zBnd)
@JvmName("ftt") operator fun Eq<F, T, T>.invoke(yBnd: YBd, zBnd: ZBd) = call(yBnd, zBnd)

@JvmName("tft") operator fun Eq<T, F, T>.invoke(xBnd: XBd) = Eqn<F, F, T>(this, xBnd)
@JvmName("tft") operator fun Eq<T, F, T>.invoke(zBnd: ZBd) = Eqn<T, F, F>(this, zBnd)
@JvmName("tft") operator fun Eq<T, F, T>.invoke(xBnd: XBd, yBnd: ZBd) = call(xBnd, yBnd)

@JvmName("ttf") operator fun Eq<T, T, F>.invoke(xBnd: XBd) = Eqn<F, T, F>(this, xBnd)
@JvmName("ttf") operator fun Eq<T, T, F>.invoke(yBnd: YBd) = Eqn<T, F, F>(this, yBnd)
@JvmName("ttf") operator fun Eq<T, T, F>.invoke(xBnd: XBd, yBnd: YBd) = call(xBnd, yBnd)

@JvmName("ttt") operator fun Eq<T, T, T>.invoke(xBnd: XBd) = Eqn<F, T, T>(this, xBnd)
@JvmName("ttt") operator fun Eq<T, T, T>.invoke(yBnd: YBd) = Eqn<T, F, T>(this, yBnd)
@JvmName("ttt") operator fun Eq<T, T, T>.invoke(zBnd: ZBd) = Eqn<T, T, F>(this, zBnd)
@JvmName("ttt") operator fun Eq<T, T, T>.invoke(xBnd: XBd, zBnd: ZBd) = Eqn<F, T, F>(this, xBnd, zBnd)
@JvmName("ttt") operator fun Eq<T, T, T>.invoke(xBnd: XBd, yBnd: YBd) = Eqn<F, F, T>(this, xBnd, yBnd)
@JvmName("ttt") operator fun Eq<T, T, T>.invoke(yBnd: YBd, zBnd: ZBd) = Eqn<T, F, F>(this, yBnd, zBnd)
@JvmName("ttt") operator fun Eq<T, T, T>.invoke(xBnd: XBd, yBnd: YBd, zBnd: ZBd) = call(xBnd, yBnd, zBnd)

open class x: Vr(), Eq<T, F, F> { companion object: x() { operator fun invoke(int: Int) = XBd(this, int) } }
class XBd(val vr: Vr, val value: Int): x()
open class y: Vr(), Eq<F, T, F> { companion object: y() { operator fun invoke(int: Int) = YBd(this, int) } }
class YBd(val vr: Vr, val value: Int): y()
open class z: Vr(), Eq<F, F, T> { companion object: z() { operator fun invoke(int: Int) = ZBd(this, int) } }
class ZBd(val vr: Vr, val value: Int): z()
open class Vr

fun main() {
  val xyz = 1 + x + 2 + y + z
  val zon = xyz(x(1), y(2))
  val out = zon(2)

  val yon = xyz(x(1), z(2))
}