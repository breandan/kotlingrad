package edu.umontreal.kotlingrad.experimental

fun main() {
  val x = v1; val y = v2; val z = v3
  val xyz: Ex<XX, XX, XX> = (1 + x + 2) + (y + z)
  val x__: Ex<OO, OO, XX> = xyz(x to 1, y to 2)
  val out: Int = x__(2)
  val _y_: Ex<OO, XX, OO> = xyz(x to 1, z to 2)
}

sealed class XO
abstract class XX: XO() // Present
abstract class OO: XO() // Absent

enum class BF { PLUS, MINUS, TIMES, DIV }
open class Ex<R: XO, S: XO, T: XO>(vararg val exs: Ex<*, *, *>, val op: BF? = null) {
  fun <N: Number> call(vararg vrb: VrB<N>): N = (inv<N, R, S, T>(*vrb) as Nt<N>).value
  open fun <T: Number, M: XO, N: XO, O: XO> inv(vararg bnds: VrB<T>): Ex<M, N, O> =
    exs.reduce { it, acc -> if(op == null) acc else it.inv<T, M, N, O>(*bnds) } as Ex<M, N, O>
}

open class Nt<T: Number>(val value: T): Ex<OO, OO, OO>() {
  override fun <T: Number, M: XO, N: XO, O: XO> inv(vararg bnds: VrB<T>) = this as Ex<M, N, O>
}

@JvmName("+:___") operator fun <V1: XO, V2: XO, V3: XO> Ex<V1, V2, V3>.plus(e: Ex<OO, OO, OO>) = Ex<V1, V2, V3>(this, e, op = BF.PLUS)
@JvmName("+:t__") operator fun <V1: XO, V2: XO, V3: XO> Ex<V1, V2, V3>.plus(e: Ex<XX, OO, OO>) = Ex<XX, V2, V3>(this, e, op = BF.PLUS)
@JvmName("+:_t_") operator fun <V1: XO, V2: XO, V3: XO> Ex<V1, V2, V3>.plus(e: Ex<OO, XX, OO>) = Ex<V1, XX, V3>(this, e, op = BF.PLUS)
@JvmName("+:__t") operator fun <V1: XO, V2: XO, V3: XO> Ex<V1, V2, V3>.plus(e: Ex<OO, OO, XX>) = Ex<V1, V2, XX>(this, e, op = BF.PLUS)
@JvmName("+:tt_") operator fun <V1: XO, V2: XO, V3: XO> Ex<V1, V2, V3>.plus(e: Ex<XX, XX, OO>) = Ex<XX, XX, V2>(this, e, op = BF.PLUS)
@JvmName("+:_tt") operator fun <V1: XO, V2: XO, V3: XO> Ex<V1, V2, V3>.plus(e: Ex<OO, XX, XX>) = Ex<V1, XX, XX>(this, e, op = BF.PLUS)
@JvmName("+:t_t") operator fun <V1: XO, V2: XO, V3: XO> Ex<V1, V2, V3>.plus(e: Ex<XX, OO, XX>) = Ex<XX, V2, XX>(this, e, op = BF.PLUS)
@JvmName("+:ttt") operator fun <V1: XO, V2: XO, V3: XO> Ex<V1, V2, V3>.plus(e: Ex<XX, XX, XX>) = Ex<XX, XX, XX>(this, e, op = BF.PLUS)

operator fun <N: Number, V1: XO, V2: XO, V3: XO> Ex<V1, V2, V3>.plus(n: N): Ex<V1, V2, V3> = Ex(this, wrap(n))
operator fun <N: Number, V1: XO, V2: XO, V3: XO> N.plus(e: Ex<V1, V2, V3>): Ex<V1, V2, V3> = Ex(wrap(this), e)

fun <T: Number> wrap(n: T) = Nt(n)

//                                            V1, V2, V3
@JvmName("i:t__") operator fun <N: Number> Ex<XX, OO, OO>.invoke(n: N) = call(v1 to n)
@JvmName("i:_t_") operator fun <N: Number> Ex<OO, XX, OO>.invoke(n: N) = call(v2 to n)
@JvmName("i:__t") operator fun <N: Number> Ex<OO, OO, XX>.invoke(n: N) = call(v3 to n)
@JvmName("i:_tt") operator fun <N: Number> Ex<OO, XX, XX>.invoke(v2: V2Bnd<N>) = inv<N, OO, OO, XX>(v2)
@JvmName("i:_tt") operator fun <N: Number> Ex<OO, XX, XX>.invoke(v3: V3Bnd<N>) = inv<N, OO, XX, OO>(v3)
@JvmName("i:_tt") operator fun <N: Number> Ex<OO, XX, XX>.invoke(v2: V2Bnd<N>, v3: V3Bnd<N>) = call(v2, v3)
@JvmName("i:t_t") operator fun <N: Number> Ex<XX, OO, XX>.invoke(v1: V1Bnd<N>) = inv<N, OO, OO, XX>(v1)
@JvmName("i:t_t") operator fun <N: Number> Ex<XX, OO, XX>.invoke(v3: V3Bnd<N>) = inv<N, XX, OO, OO>(v3)
@JvmName("i:t_t") operator fun <N: Number> Ex<XX, OO, XX>.invoke(v1: V1Bnd<N>, v2: V3Bnd<N>) = call(v1, v2)
@JvmName("i:tt_") operator fun <N: Number> Ex<XX, XX, OO>.invoke(v1: V1Bnd<N>) = inv<N, OO, XX, OO>(v1)
@JvmName("i:tt_") operator fun <N: Number> Ex<XX, XX, OO>.invoke(v2: V2Bnd<N>) = inv<N, XX, OO, OO>(v2)
@JvmName("i:tt_") operator fun <N: Number> Ex<XX, XX, OO>.invoke(v1: V1Bnd<N>, v2: V2Bnd<N>) = call(v1, v2)
@JvmName("i:ttt") operator fun <N: Number> Ex<XX, XX, XX>.invoke(v1: V1Bnd<N>) = inv<N, OO, XX, XX>(v1)
@JvmName("i:ttt") operator fun <N: Number> Ex<XX, XX, XX>.invoke(v2: V2Bnd<N>) = inv<N, XX, OO, XX>(v2)
@JvmName("i:ttt") operator fun <N: Number> Ex<XX, XX, XX>.invoke(v3: V3Bnd<N>) = inv<N, XX, XX, OO>(v3)
@JvmName("i:ttt") operator fun <N: Number> Ex<XX, XX, XX>.invoke(v1: V1Bnd<N>, v3: V3Bnd<N>) = inv<N, OO, XX, OO>(v1, v3)
@JvmName("i:ttt") operator fun <N: Number> Ex<XX, XX, XX>.invoke(v1: V1Bnd<N>, v2: V2Bnd<N>) = inv<N, OO, OO, XX>(v1, v2)
@JvmName("i:ttt") operator fun <N: Number> Ex<XX, XX, XX>.invoke(v2: V2Bnd<N>, v3: V3Bnd<N>) = inv<N, XX, OO, OO>(v2, v3)
@JvmName("i:ttt") operator fun <N: Number> Ex<XX, XX, XX>.invoke(v1: V1Bnd<N>, v2: V2Bnd<N>, v3: V3Bnd<N>) = call(v1, v2, v3)

open class v1: Vr<XX, OO, OO>() { companion object: v1() }
open class v2: Vr<OO, XX, OO>() { companion object: v2() }
open class v3: Vr<OO, OO, XX>() { companion object: v3() }
class V1Bnd<N: Number>(vr: Vr<*, *, *>, value: N): VrB<N>(vr, value)
class V2Bnd<N: Number>(vr: Vr<*, *, *>, value: N): VrB<N>(vr, value)
class V3Bnd<N: Number>(vr: Vr<*, *, *>, value: N): VrB<N>(vr, value)
open class VrB<N: Number>(open val vr: Vr<*, *, *>, val value: N)
open class Vr<R: XO, S: XO, T: XO>: Ex<R, S, T>() {
  override fun <T: Number, M: XO, N: XO, O: XO> inv(vararg bnds: VrB<T>): Ex<M, N, O> =
    bnds.map { it.vr to it.value }.toMap()
      .let { if(this in it) wrap(it[this]!!) else this } as Ex<M, N, O>
}

infix fun <N: Number> v1.to(n: N) = V1Bnd(v1, n)
infix fun <N: Number> v2.to(n: N) = V2Bnd(v2, n)
infix fun <N: Number> v3.to(n: N) = V3Bnd(v3, n)