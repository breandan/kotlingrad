package edu.umontreal.kotlingrad.experimental

fun main() {
  val xyz = 1 + x + 2 + y + z
  val zon = xyz(x to 1, y to 2)
  val out = zon(2)

  val yon = xyz(x to 1, z to 2)
}

abstract class T
abstract class F
interface Eq<A, B, C> { 
  fun call(vararg vrb: Vr<*, *, *>) = 0
  fun <M, N, O> inv(vararg vrb: Vr<*, *, *>): Eq<M, N, O>
}
class Eqn<A, B, C>(val op: (Array<out Vr<*, *, *>>) -> Eq<*, *, *>, vararg vrb: Vr<*, *, *>): Eq<A, B, C> {
  override fun call(vararg vrb: Vr<*, *, *>) = 0
  override fun <M, N, O> inv(vararg vrb: Vr<*, *, *>) = Eqn<M, N, O>({ op(vrb) })
}

operator fun <Q, R> Eq<*, Q, R>.plus(x: x): Eq<T, Q, R> = Eqn({ this + x })
operator fun <Q, R> Eq<Q, *, R>.plus(y: y): Eq<Q, T, R> = Eqn({ this + y })
operator fun <Q, R> Eq<Q, R, *>.plus(z: z): Eq<Q, R, T> = Eqn({ this + z })
operator fun <Q, R> Eq<*, Q, R>.minus(x: x): Eq<T, Q, R> = Eqn({ this - x })
operator fun <Q, R> Eq<Q, *, R>.minus(y: y): Eq<Q, T, R> = Eqn({ this - y })
operator fun <Q, R> Eq<Q, R, *>.minus(z: z): Eq<Q, R, T> = Eqn({ this - z })
operator fun <Q, R> Eq<*, Q, R>.times(x: x): Eq<T, Q, R> = Eqn({ this * x })
operator fun <Q, R> Eq<Q, *, R>.times(y: y): Eq<Q, T, R> = Eqn({ this * y })
operator fun <Q, R> Eq<Q, R, *>.times(z: z): Eq<Q, R, T> = Eqn({ this * z })
operator fun <Q, R> Eq<*, Q, R>.div(x: x): Eq<T, Q, R> = Eqn({ this / x })
operator fun <Q, R> Eq<Q, *, R>.div(y: y): Eq<Q, T, R> = Eqn({ this / y })
operator fun <Q, R> Eq<Q, R, *>.div(z: z): Eq<Q, R, T> = Eqn({ this / z })

operator fun <V1, V2, V3> Eq<V1, V2, V3>.plus(c: Number): Eq<V1, V2, V3> = this
operator fun <V1, V2, V3> Int.plus(e: Eq<V1, V2, V3>): Eq<V1, V2, V3> = e

@JvmName("tff") operator fun Eq<T, F, F>.invoke(xBnd: Int) = call(x to xBnd)
@JvmName("ftf") operator fun Eq<F, T, F>.invoke(yBnd: Int) = call(y to yBnd)
@JvmName("fft") operator fun Eq<F, F, T>.invoke(zBnd: Int) = call(z to zBnd)

@JvmName("ftt") operator fun Eq<F, T, T>.invoke(yBnd: YBd) = inv<F, F, T>(yBnd)
@JvmName("ftt") operator fun Eq<F, T, T>.invoke(zBnd: ZBd) = inv<F, T, F>(zBnd)
@JvmName("ftt") operator fun Eq<F, T, T>.invoke(yBnd: YBd, zBnd: ZBd) = call(yBnd, zBnd)

@JvmName("tft") operator fun Eq<T, F, T>.invoke(xBnd: XBd) = inv<F, F, T>(xBnd)
@JvmName("tft") operator fun Eq<T, F, T>.invoke(zBnd: ZBd) = inv<T, F, F>(zBnd)
@JvmName("tft") operator fun Eq<T, F, T>.invoke(xBnd: XBd, yBnd: ZBd) = call(xBnd, yBnd)

@JvmName("ttf") operator fun Eq<T, T, F>.invoke(xBnd: XBd) = inv<F, T, F>(xBnd)
@JvmName("ttf") operator fun Eq<T, T, F>.invoke(yBnd: YBd) = inv<T, F, F>(yBnd)
@JvmName("ttf") operator fun Eq<T, T, F>.invoke(xBnd: XBd, yBnd: YBd) = call(xBnd, yBnd)

@JvmName("ttt") operator fun Eq<T, T, T>.invoke(xBnd: XBd) = inv<F, T, T>(xBnd)
@JvmName("ttt") operator fun Eq<T, T, T>.invoke(yBnd: YBd) = inv<T, F, T>(yBnd)
@JvmName("ttt") operator fun Eq<T, T, T>.invoke(zBnd: ZBd) = inv<T, T, F>(zBnd)
@JvmName("ttt") operator fun Eq<T, T, T>.invoke(xBnd: XBd, zBnd: ZBd) = inv<F, T, F>(xBnd, zBnd)
@JvmName("ttt") operator fun Eq<T, T, T>.invoke(xBnd: XBd, yBnd: YBd) = inv<F, F, T>(xBnd, yBnd)
@JvmName("ttt") operator fun Eq<T, T, T>.invoke(yBnd: YBd, zBnd: ZBd) = inv<T, F, F>(yBnd, zBnd)
@JvmName("ttt") operator fun Eq<T, T, T>.invoke(xBnd: XBd, yBnd: YBd, zBnd: ZBd) = call(xBnd, yBnd, zBnd)

open class x: Vr<T, F, F>() { companion object: x() }
open class y: Vr<F, T, F>() { companion object: y() }
open class z: Vr<F, F, T>() { companion object: z() }
class XBd(val vr: Vr<*, *, *>, val value: Int): x()
class YBd(val vr: Vr<*, *, *>, val value: Int): y()
class ZBd(val vr: Vr<*, *, *>, val value: Int): z()
open class Vr<A, B, C>: Eq<A, B, C> {
  override fun <M, N, O> inv(vararg vrb: Vr<*, *, *>): Eq<M, N, O> = TODO("Add support for variable rebinding")
}

infix fun x.to(int: Int) = XBd(x, int)
infix fun y.to(int: Int) = YBd(y, int)
infix fun z.to(int: Int) = ZBd(z, int)