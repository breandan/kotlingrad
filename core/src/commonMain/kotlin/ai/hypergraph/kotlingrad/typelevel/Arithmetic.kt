package ai.hypergraph.kotlingrad.typelevel

import kotlin.jvm.JvmName

interface TLNat {
  val eval: Int
}

abstract class Suc<T: TLNat>(val succ: T): TLNat {
  override val eval: Int = succ.eval + 1
}

object D0: TLNat {
  override val eval: Int = 0
}
typealias N1 = Suc<D0>; object D1: N1(D0)
typealias N2 = Suc<N1>; object D2: N2(D1)
typealias N3 = Suc<N2>; object D3: N3(D2)
typealias N4 = Suc<N3>; object D4: N4(D3)
typealias N5 = Suc<N4>; object D5: N5(D4)
typealias N6 = Suc<N5>; object D6: N6(D5)
typealias N7 = Suc<N6>; object D7: N7(D6)
typealias N8 = Suc<N7>; object D8: N8(D7)
typealias N9 = Suc<N8>; object D9: N9(D8)

class Sum<L: TLNat, R: TLNat>(left: L, right: R): TLNat {
  override val eval: Int = left.eval + right.eval
}

class Pdt<L: TLNat, R: TLNat>(left: L, right: R): TLNat {
  override val eval: Int = left.eval * right.eval
}

inline fun <reified S1: N3> Vec<S1>.takesThree() = size
fun takesFour(four: N4): N7 = D7

class Vec<O: TLNat>(o: O) {
  val size = o.eval
}

@JvmName("1+1=2")
operator fun N1.plus(n: N1): N2 = D2
@JvmName("2+2=4")
operator fun N2.plus(n: N2): N4 = D4
inline operator fun <reified S1: N1, reified S2: N2> Vec<S1>.plus(other: Vec<S2>): Vec<N3> = Vec(D3)

// Try to encode arithmetic as a lattice
data class T1<A>(val e1: A)
data class T3<A, B, C>(val e1: A, val e2: B, val e3: C)
data class T4<A: D, B, C, D>(val e1: A, val e2: B, val e3: C, val e4: D)

//https://youtrack.jetbrains.com/issue/KT-50466
fun main() {
  val t = op(object: PLUS{}, object: II{}, object: III{})
  // val t: P_II_III = plus(object: I{}, object: III{}) // "Specify type explicitly", it will produce P_II_III
  // val t: Three = op(object: III{}, object: II{}, object: PLUS{}) // Sensitive to input argument order
  // If we omit the type for t and "Specify type explicitly" on the following type, it is "Three". With the inferred type above, it is "Five"
  val r = apply(t, object: EVAL {})
  // val r: Five = apply(t, object: EVAL {})
}

interface I   : One, P_I_II, P_I_III, P_I_IV, P_I_V, M_I_II, M_I_III, M_I_IV
interface II  : Two, P_I_II, P_II_III, P_II_IV, M_I_II, M_II_III
interface III : Three, P_I_III, P_II_III, M_I_III, M_II_III
interface IV  : Four, P_I_IV, P_II_IV, M_I_IV
interface V   : Five, P_I_V
interface VI  : Six

interface One
interface Two
interface Three
interface Four
interface Five
interface Six

interface M_I_II   : Two
interface M_I_III  : Three
interface M_I_IV   : Four
interface M_II_III : Six
interface M_II_II  : Four

interface P_I_II   : Three
interface P_I_III  : Four
interface P_I_IV   : Five
interface P_II_III : Five
interface P_II_IV  : Six
interface P_I_V    : Six
interface EVAL     : One, Two, Three, Four, Five, Six
interface PLUS     : P_I_II, P_I_III, P_I_IV, P_II_III, P_II_IV
interface TIMES    : M_I_II, M_I_III, M_I_IV, M_II_III

fun <X: T, Y: T, Z: T, T> op(x: X, y: Y, op: Z): T = TODO()
fun <X:Z, Y: Z, Z> apply(x: X, op: Y): Z = TODO()