package edu.umontreal.kotlingrad.experimental

fun main() {
  val xyz = (1 + x + 2) + (y + z)
  val zon = xyz(x to 1, y to 2)
  val out = zon(2)

  val yon = xyz(x to 1, z to 2)
}

abstract class B
abstract class T: B()
abstract class F: B()
//interface Eq<R: B, S: B, T: B> {
//  fun call(vararg vrb: Vr<*, *, *>) = 0
//  fun <M: B, N: B, O: B> inv(vararg vrb: Vr<*, *, *>): Eq<M, N, O>
//}
open class Ex<R: B, S: B, T: B>(
  vararg exs: Ex<*, *, *>
//  val op: (Array<out Vr<*, *, *>>) -> Ex<*, *, *>,
//  vararg vrb: Vr<*, *, *>
) {
  fun call(vararg vrb: Vr<*, *, *>) = 0
  open fun <M: B, N: B, O: B> inv(vararg vrb: Vr<*, *, *>) = Ex<M, N, O>()
}

@JvmName("+:___") operator fun <Q: B, R: B, S: B> Ex<Q, R, S>.plus(e: Ex<F, F, F>) = Ex<Q, R, S>(this, e)
@JvmName("+:t__") operator fun <Q: B, R: B, S: B> Ex<Q, R, S>.plus(e: Ex<T, F, F>) = Ex<T, R, S>(this, e)
@JvmName("+:_t_") operator fun <Q: B, R: B, S: B> Ex<Q, R, S>.plus(e: Ex<F, T, F>) = Ex<Q, T, S>(this, e)
@JvmName("+:__t") operator fun <Q: B, R: B, S: B> Ex<Q, R, S>.plus(e: Ex<F, F, T>) = Ex<Q, R, T>(this, e)
@JvmName("+:tt_") operator fun <Q: B, R: B, S: B> Ex<Q, R, S>.plus(e: Ex<T, T, F>) = Ex<T, T, R>(this, e)
@JvmName("+:_tt") operator fun <Q: B, R: B, S: B> Ex<Q, R, S>.plus(e: Ex<F, T, T>) = Ex<Q, T, T>(this, e)
@JvmName("+:t_t") operator fun <Q: B, R: B, S: B> Ex<Q, R, S>.plus(e: Ex<T, F, T>) = Ex<T, R, T>(this, e)
@JvmName("+:ttt") operator fun <Q: B, R: B, S: B> Ex<Q, R, S>.plus(e: Ex<T, T, T>) = Ex<T, T, T>(this, e)

operator fun <V1: B, V2: B, V3: B> Ex<V1, V2, V3>.plus(c: Number): Ex<V1, V2, V3> = this
operator fun <V1: B, V2: B, V3: B> Int.plus(e: Ex<V1, V2, V3>): Ex<V1, V2, V3> = e

//                              X  Y  Z
@JvmName("tff") operator fun Ex<T, F, F>.invoke(xBnd: Int) = call(x to xBnd)
@JvmName("ftf") operator fun Ex<F, T, F>.invoke(yBnd: Int) = call(y to yBnd)
@JvmName("fft") operator fun Ex<F, F, T>.invoke(zBnd: Int) = call(z to zBnd)

@JvmName("ftt") operator fun Ex<F, T, T>.invoke(yBnd: YBd) = inv<F, F, T>(yBnd)
@JvmName("ftt") operator fun Ex<F, T, T>.invoke(zBnd: ZBd) = inv<F, T, F>(zBnd)
@JvmName("ftt") operator fun Ex<F, T, T>.invoke(yBnd: YBd, zBnd: ZBd) = call(yBnd, zBnd)

@JvmName("tft") operator fun Ex<T, F, T>.invoke(xBnd: XBd) = inv<F, F, T>(xBnd)
@JvmName("tft") operator fun Ex<T, F, T>.invoke(zBnd: ZBd) = inv<T, F, F>(zBnd)
@JvmName("tft") operator fun Ex<T, F, T>.invoke(xBnd: XBd, yBnd: ZBd) = call(xBnd, yBnd)

@JvmName("ttf") operator fun Ex<T, T, F>.invoke(xBnd: XBd) = inv<F, T, F>(xBnd)
@JvmName("ttf") operator fun Ex<T, T, F>.invoke(yBnd: YBd) = inv<T, F, F>(yBnd)
@JvmName("ttf") operator fun Ex<T, T, F>.invoke(xBnd: XBd, yBnd: YBd) = call(xBnd, yBnd)

@JvmName("ttt") operator fun Ex<T, T, T>.invoke(xBnd: XBd) = inv<F, T, T>(xBnd)
@JvmName("ttt") operator fun Ex<T, T, T>.invoke(yBnd: YBd) = inv<T, F, T>(yBnd)
@JvmName("ttt") operator fun Ex<T, T, T>.invoke(zBnd: ZBd) = inv<T, T, F>(zBnd)
@JvmName("ttt") operator fun Ex<T, T, T>.invoke(xBnd: XBd, zBnd: ZBd) = inv<F, T, F>(xBnd, zBnd)
@JvmName("ttt") operator fun Ex<T, T, T>.invoke(xBnd: XBd, yBnd: YBd) = inv<F, F, T>(xBnd, yBnd)
@JvmName("ttt") operator fun Ex<T, T, T>.invoke(yBnd: YBd, zBnd: ZBd) = inv<T, F, F>(yBnd, zBnd)
@JvmName("ttt") operator fun Ex<T, T, T>.invoke(xBnd: XBd, yBnd: YBd, zBnd: ZBd) = call(xBnd, yBnd, zBnd)

open class x: Vr<T, F, F>() { companion object: x() }
open class y: Vr<F, T, F>() { companion object: y() }
open class z: Vr<F, F, T>() { companion object: z() }
class XBd(val vr: Vr<*, *, *>, val value: Int): x()
class YBd(val vr: Vr<*, *, *>, val value: Int): y()
class ZBd(val vr: Vr<*, *, *>, val value: Int): z()
open class Vr<R: B, S: B, T: B>: Ex<R, S, T>() {
  override fun <M: B, N: B, O: B> inv(vararg vrb: Vr<*, *, *>): Ex<M, N, O> = TODO("Add support for variable rebinding")
}

infix fun x.to(int: Int) = XBd(x, int)
infix fun y.to(int: Int) = YBd(y, int)
infix fun z.to(int: Int) = ZBd(z, int)