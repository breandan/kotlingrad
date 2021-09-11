package ai.hypergraph.kotlingrad.typelevel

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