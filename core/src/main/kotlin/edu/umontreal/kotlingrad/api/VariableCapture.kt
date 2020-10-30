package edu.umontreal.kotlingrad.api

import kotlin.reflect.KProperty

sealed class XO
abstract class XX: XO() // Present
abstract class OO: XO() // Absent

enum class OP(val op: String) { ADD("+"), SUB("-"), MUL("*"), DIV("/") }

open class Ex<V1: XO, V2: XO, V3: XO>
constructor(
  vararg val exs: Ex<*, *, *>,
  val op: OP? = null,
  open val name: String? = null
) {
  fun <N: Number> call(vararg vrb: VrB<N>): N = (inv<N, V1, V2, V3>(*vrb) as Nt<N>).value
  open fun <T: Number, V1: XO, V2: XO, V3: XO> inv(vararg bnds: VrB<T>): Ex<V1, V2, V3> =
    if (exs.isEmpty()) this as Ex<V1, V2, V3>
    else exs.map { it.inv<T, V1, V2, V3>(*bnds) }
      .reduce { it, acc -> apply(acc, it) as Ex<V1, V2, V3> }

  private fun apply(me: Ex<*, *, *>, that: Ex<*, *, *>) =
    if(op == null) that else if (me is Nt<*> && that is Nt<*>)
      when (op) {
        OP.ADD -> Nt(me.value.toDouble() + that.value.toDouble())
        OP.SUB -> Nt(me.value.toDouble() - that.value.toDouble())
        OP.MUL -> Nt(me.value.toDouble() * that.value.toDouble())
        OP.DIV -> Nt(me.value.toDouble() / that.value.toDouble())
      } else Ex<V1, V2, V3>(me, that, op = op)

  /**
   * This follow code is a type-level encoding of the 3-element graded poset.
   *
   * For combination, i.e. any arithmetical operation:
   *
   *                |  x      y      z      xy      xz      yz      xyz
   *            -------------------------------------------------------
   *            x   |  x      xy     xz     xy      xz      xyz     xyz
   *            y   |  xy     y      yz     xy      xyz     yz      xyz
   *            z   |  xz     yz     z      xyz     xz      yz      xyz
   *            xy  |  xy     xy     xyz    xy      xyz     xyz     xyz
   *            xz  |  xz     xyz    xz     xyz     xz      xyz     xyz
   *            yz  |  xyz    yz     yz     xyz     xyz     yz      xyz
   *            xyz |  xyz    xyz    xyz    xyz     xyz     xyz     xyz
   *
   * Can be viewed as a Hasse Diagram: https://en.wikipedia.org/wiki/Hasse_diagram
   *
   * For application/invocation, where P is a constant:
   *
   *                |  x      y      z      xy      xz      yz      xyz
   *            -------------------------------------------------------
   *            x   |  P                    y       z               yz
   *            y   |         P             x               z       xz
   *            z   |                P              x       y       xy
   *            xy  |                       P                       z
   *            xz  |                               P               y
   *            yz  |                                       P       x
   *            xyz |                                               P
   */

  @JvmName("p:___") operator fun plus(e: Ex<OO, OO, OO>) = Ex<V1, V2, V3>(this, e, op = OP.ADD)
  @JvmName("p:t__") operator fun plus(e: Ex<XX, OO, OO>) = Ex<XX, V2, V3>(this, e, op = OP.ADD)
  @JvmName("p:_t_") operator fun plus(e: Ex<OO, XX, OO>) = Ex<V1, XX, V3>(this, e, op = OP.ADD)
  @JvmName("p:__t") operator fun plus(e: Ex<OO, OO, XX>) = Ex<V1, V2, XX>(this, e, op = OP.ADD)
  @JvmName("p:tt_") operator fun plus(e: Ex<XX, XX, OO>) = Ex<XX, XX, V2>(this, e, op = OP.ADD)
  @JvmName("p:_tt") operator fun plus(e: Ex<OO, XX, XX>) = Ex<V1, XX, XX>(this, e, op = OP.ADD)
  @JvmName("p:t_t") operator fun plus(e: Ex<XX, OO, XX>) = Ex<XX, V2, XX>(this, e, op = OP.ADD)
  @JvmName("p:ttt") operator fun plus(e: Ex<XX, XX, XX>) = Ex<XX, XX, XX>(this, e, op = OP.ADD)

  @JvmName("m:___") operator fun minus(e: Ex<OO, OO, OO>) = Ex<V1, V2, V3>(this, e, op = OP.SUB)
  @JvmName("m:t__") operator fun minus(e: Ex<XX, OO, OO>) = Ex<XX, V2, V3>(this, e, op = OP.SUB)
  @JvmName("m:_t_") operator fun minus(e: Ex<OO, XX, OO>) = Ex<V1, XX, V3>(this, e, op = OP.SUB)
  @JvmName("m:__t") operator fun minus(e: Ex<OO, OO, XX>) = Ex<V1, V2, XX>(this, e, op = OP.SUB)
  @JvmName("m:tt_") operator fun minus(e: Ex<XX, XX, OO>) = Ex<XX, XX, V2>(this, e, op = OP.SUB)
  @JvmName("m:_tt") operator fun minus(e: Ex<OO, XX, XX>) = Ex<V1, XX, XX>(this, e, op = OP.SUB)
  @JvmName("m:t_t") operator fun minus(e: Ex<XX, OO, XX>) = Ex<XX, V2, XX>(this, e, op = OP.SUB)
  @JvmName("m:ttt") operator fun minus(e: Ex<XX, XX, XX>) = Ex<XX, XX, XX>(this, e, op = OP.SUB)

  @JvmName("t:___") operator fun times(e: Ex<OO, OO, OO>) = Ex<V1, V2, V3>(this, e, op = OP.MUL)
  @JvmName("t:t__") operator fun times(e: Ex<XX, OO, OO>) = Ex<XX, V2, V3>(this, e, op = OP.MUL)
  @JvmName("t:_t_") operator fun times(e: Ex<OO, XX, OO>) = Ex<V1, XX, V3>(this, e, op = OP.MUL)
  @JvmName("t:__t") operator fun times(e: Ex<OO, OO, XX>) = Ex<V1, V2, XX>(this, e, op = OP.MUL)
  @JvmName("t:tt_") operator fun times(e: Ex<XX, XX, OO>) = Ex<XX, XX, V2>(this, e, op = OP.MUL)
  @JvmName("t:_tt") operator fun times(e: Ex<OO, XX, XX>) = Ex<V1, XX, XX>(this, e, op = OP.MUL)
  @JvmName("t:t_t") operator fun times(e: Ex<XX, OO, XX>) = Ex<XX, V2, XX>(this, e, op = OP.MUL)
  @JvmName("t:ttt") operator fun times(e: Ex<XX, XX, XX>) = Ex<XX, XX, XX>(this, e, op = OP.MUL)

  @JvmName("d:___") operator fun div(e: Ex<OO, OO, OO>) = Ex<V1, V2, V3>(this, e, op = OP.DIV)
  @JvmName("d:t__") operator fun div(e: Ex<XX, OO, OO>) = Ex<XX, V2, V3>(this, e, op = OP.DIV)
  @JvmName("d:_t_") operator fun div(e: Ex<OO, XX, OO>) = Ex<V1, XX, V3>(this, e, op = OP.DIV)
  @JvmName("d:__t") operator fun div(e: Ex<OO, OO, XX>) = Ex<V1, V2, XX>(this, e, op = OP.DIV)
  @JvmName("d:tt_") operator fun div(e: Ex<XX, XX, OO>) = Ex<XX, XX, V2>(this, e, op = OP.DIV)
  @JvmName("d:_tt") operator fun div(e: Ex<OO, XX, XX>) = Ex<V1, XX, XX>(this, e, op = OP.DIV)
  @JvmName("d:t_t") operator fun div(e: Ex<XX, OO, XX>) = Ex<XX, V2, XX>(this, e, op = OP.DIV)
  @JvmName("d:ttt") operator fun div(e: Ex<XX, XX, XX>) = Ex<XX, XX, XX>(this, e, op = OP.DIV)

  operator fun <N: Number> plus(n: N) = Ex<V1, V2, V3>(this, Nt(n), op = OP.ADD)
  operator fun <N: Number> minus(n: N) = Ex<V1, V2, V3>(this, Nt(n), op = OP.SUB)
  operator fun <N: Number> times(n: N) = Ex<V1, V2, V3>(this, Nt(n), op = OP.MUL)
  operator fun <N: Number> div(n: N) = Ex<V1, V2, V3>(this, Nt(n), op = OP.DIV)

  open operator fun getValue(nothing: Nothing?, property: KProperty<*>) =
    Ex<V1, V2, V3>(exs = exs, op = op, name = property.name)

  override fun toString() = exs
    .map { if (op in arrayOf(OP.MUL, OP.DIV) && it.op in arrayOf(OP.ADD, OP.SUB)) "($it)" else "$it" }
    .joinToString(op?.op ?: " _ ").let { name?.run { "$name = $it" } ?: it }
}

open class Nt<T: Number>(val value: T): Ex<OO, OO, OO>() { override fun toString() = value.toString() }

operator fun <N: Number, V1: XO, V2: XO, V3: XO> N.plus(e: Ex<V1, V2, V3>) = Ex<V1, V2, V3>(Nt(this), e, op = OP.ADD)
operator fun <N: Number, V1: XO, V2: XO, V3: XO> N.minus(e: Ex<V1, V2, V3>) = Ex<V1, V2, V3>(Nt(this), e, op = OP.SUB)
operator fun <N: Number, V1: XO, V2: XO, V3: XO> N.times(e: Ex<V1, V2, V3>) = Ex<V1, V2, V3>(Nt(this), e, op = OP.MUL)
operator fun <N: Number, V1: XO, V2: XO, V3: XO> N.div(e: Ex<V1, V2, V3>) = Ex<V1, V2, V3>(Nt(this), e, op = OP.DIV)

//                                            V1, V2, V3
@JvmName("i:t__") operator fun <N: Number> Ex<XX, OO, OO>.invoke(n: N) = call(V1() to n)
@JvmName("i:_t_") operator fun <N: Number> Ex<OO, XX, OO>.invoke(n: N) = call(V2() to n)
@JvmName("i:__t") operator fun <N: Number> Ex<OO, OO, XX>.invoke(n: N) = call(V3() to n)
@JvmName("i:t__") operator fun <N: Number> Ex<XX, OO, OO>.invoke(v1: V1Bnd<N>) = call(v1)
@JvmName("i:_t_") operator fun <N: Number> Ex<OO, XX, OO>.invoke(v2: V2Bnd<N>) = call(v2)
@JvmName("i:__t") operator fun <N: Number> Ex<OO, OO, XX>.invoke(v3: V3Bnd<N>) = call(v3)
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

val x by V1(); val y by V2(); val z by V3()
open class V1 internal constructor(name: String = "v1"): Vr<XX, OO, OO>(name) {
  override operator fun getValue(nothing: Nothing?, property: KProperty<*>) = V1(property.name)
}

open class V2 internal constructor(name: String = "v2"): Vr<OO, XX, OO>(name) {
  override operator fun getValue(nothing: Nothing?, property: KProperty<*>) = V2(property.name)
}

open class V3 internal constructor(name: String = "v3"): Vr<OO, OO, XX>(name) {
  override operator fun getValue(nothing: Nothing?, property: KProperty<*>) = V3(property.name)
}

class V1Bnd<N: Number> internal constructor(vr: V1, value: N): VrB<N>(vr, value)
class V2Bnd<N: Number> internal constructor(vr: V2, value: N): VrB<N>(vr, value)
class V3Bnd<N: Number> internal constructor(vr: V3, value: N): VrB<N>(vr, value)

infix fun <N: Number> V1.to(n: N) = V1Bnd(this, n)
infix fun <N: Number> V2.to(n: N) = V2Bnd(this, n)
infix fun <N: Number> V3.to(n: N) = V3Bnd(this, n)

open class VrB<N: Number>(open val vr: Vr<*, *, *>, val value: N)
sealed class Vr<R: XO, S: XO, T: XO>(override val name: String): Ex<R, S, T>() {
  override fun <T: Number, M: XO, N: XO, O: XO> inv(vararg bnds: VrB<T>): Ex<M, N, O> =
    bnds.map { it.vr::class to it.value }.toMap()
      .let { it[this::class]?.let { Nt(it) } ?: this } as Ex<M, N, O>

  override fun toString() = name
}