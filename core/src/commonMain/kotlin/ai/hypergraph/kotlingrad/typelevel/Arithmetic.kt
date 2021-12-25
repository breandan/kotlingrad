package ai.hypergraph.kotlingrad.typelevel

import kotlin.jvm.JvmName
import kotlin.reflect.KClass

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

// Idea: encode the result of A OP B as a join, i.e., C := LUB(A, OP, B)
// Problem: Must be acyclic, i.e., A * 1 = A, A + 0 = A will not work
// https://kotlinlang.org/spec/type-system.html#least-upper-bound
sealed class Num {
  fun supertype() = this::class.toString()
  val i: Int = when(this) {
    is I -> 1
    is II -> 2
    is III -> 3
    is IV -> 4
    is V -> 5
    is VI -> 6
  }
}
class I   : Num(), One, P_I_II, P_I_III, P_I_IV, P_I_V, M_I_II, M_I_III, M_I_IV
class II  : Num(), Two, P_I_II, P_II_III, P_II_IV, M_I_II, M_II_III
class III : Num(), Three, P_I_III, P_II_III, M_I_III, M_II_III
class IV  : Num(), Four, P_I_IV, P_II_IV, M_I_IV
class V   : Num(), Five, P_I_V
class VI  : Num(), Six

sealed interface INum {
  fun int() = i().i
  fun i(): Num = when(this) {
    is One -> I()
    is Two -> II()
    is Three -> III()
    is Four -> IV()
    is Five -> V()
    is Six -> VI()
  }
}

interface One  : INum
interface Two  : INum
interface Three: INum
interface Four : INum
interface Five : INum
interface Six  : INum

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

// Computes the least upper bound between two types
fun join(a1: Any, a2: Any): INum = a1.run {
  println((a1 as Num).supertype())
  if (a1 is II && a2 is III) object : P_II_III {}
  else TODO()
}

fun <X: T, Y: T, Z: T, T> op(op: X, x: Y, y: Z): T = join(x!!, y!!) as T

fun <X:Z, Y: Z, Z> apply(x: X, op: Y): Z = x