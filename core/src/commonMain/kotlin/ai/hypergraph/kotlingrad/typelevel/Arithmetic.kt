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

interface Top { val i: Int get() = 4 }
interface III: Top { override val i: Int get() = 3; companion object: III }
interface IPII: III
interface IPI: II
interface II: IPII, Top { override val i: Int get() = 2 ; companion object: II }
interface I: IPI, IPII, Top { override val i: Int get() = 1 ; companion object: I }
class Bot(override val i: Int = 0): I, II, III
interface Plus: IPI, IPII, Top { companion object: Plus }

// Kotlin type inference seems able to compute a join/meet
fun <O: Z, X: Z, Y: Z, Z> op(op: O, e1: X, e2: Y, z: Z = TODO()): Z = TODO()

fun main() {
  val t: III = op(Plus, I, II)
}