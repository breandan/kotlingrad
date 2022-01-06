// This file was generated by Shipshape
@file:Suppress("UNUSED_PARAMETER", "UNCHECKED_CAST")

package ai.hypergraph.kotlingrad.typelevel.peano

import kotlin.jvm.JvmName

open class S<X>(val x: X?) {
  override fun equals(other: Any?) = if(other is S<*>) if(x == null && other.x == null) true else x == other.x else false
  override fun hashCode() = x.hashCode().hashCode()
}
object O: S<O>(null)
fun S<*>.toInt(i: Int = 0): Int = (x as? S<*>)?.toInt(i + 1) ?: i

operator fun Number.plus(s: S<*>): Int = toInt() + s.toInt()
operator fun Number.minus(s: S<*>): Int = toInt() - s.toInt()
operator fun Number.times(s: S<*>): Int = toInt() * s.toInt()
operator fun Number.div(s: S<*>): Int = toInt() / s.toInt()

operator fun S<*>.plus(n: Number): Int = toInt() + n.toInt()
operator fun S<*>.minus(n: Number): Int = toInt() - n.toInt()
operator fun S<*>.times(n: Number): Int = toInt() * n.toInt()
operator fun S<*>.div(n: Number): Int = toInt() / n.toInt()

val S0: L0 = O
val S1: L1 = S(O)
val S2: L2 = S(S1)
val S3: L3 = S(S2)
val S4: L4 = S(S3)
val S5: L5 = S(S4)
val S6: L6 = S(S5)
val S7: L7 = S(S6)
val S8: L8 = S(S7)
val S9: L9 = S(S8)
val S10: L10 = S(S9)
val S11: L11 = S(S10)
val S12: L12 = S(S11)
val S13: L13 = S(S12)
val S14: L14 = S(S13)
val S15: L15 = S(S14)
val S16: L16 = S(S15)

typealias L0 = O
typealias L1 = S<O>
typealias L2 = Q2<O>
typealias L3 = Q3<O>
typealias L4 = Q4<O>
typealias L5 = Q5<O>
typealias L6 = Q6<O>
typealias L7 = Q7<O>
typealias L8 = Q8<O>
typealias L9 = Q9<O>
typealias L10 = Q10<O>
typealias L11 = Q11<O>
typealias L12 = Q12<O>
typealias L13 = Q13<O>
typealias L14 = Q14<O>
typealias L15 = Q15<O>
typealias L16 = Q16<O>
typealias Q1<T> = S<T>
typealias Q2<T> = S<Q1<T>>
typealias Q3<T> = S<Q2<T>>
typealias Q4<T> = S<Q3<T>>
typealias Q5<T> = S<Q4<T>>
typealias Q6<T> = S<Q5<T>>
typealias Q7<T> = S<Q6<T>>
typealias Q8<T> = S<Q7<T>>
typealias Q9<T> = S<Q8<T>>
typealias Q10<T> = S<Q9<T>>
typealias Q11<T> = S<Q10<T>>
typealias Q12<T> = S<Q11<T>>
typealias Q13<T> = S<Q12<T>>
typealias Q14<T> = S<Q13<T>>
typealias Q15<T> = S<Q14<T>>
typealias Q16<T> = S<Q15<T>>

fun <W: S<*>, X: S<W>> W.plus1(): X = S(this) as X
fun <W: S<*>, X: S<W>> X.minus1(): W = x as W
fun <W: S<*>, X: Q2<W>> W.plus2(): X = plus1().plus1()
fun <W: S<*>, X: Q2<W>> X.minus2(): W = minus1().minus1()
fun <W: S<*>, X: Q3<W>> W.plus3(): X = plus1().plus2()
fun <W: S<*>, X: Q3<W>> X.minus3(): W = minus1().minus2()
fun <W: S<*>, X: Q4<W>> W.plus4(): X = plus2().plus2()
fun <W: S<*>, X: Q4<W>> X.minus4(): W = minus2().minus2()
fun <W: S<*>, X: Q5<W>> W.plus5(): X = plus2().plus3()
fun <W: S<*>, X: Q5<W>> X.minus5(): W = minus2().minus3()
fun <W: S<*>, X: Q6<W>> W.plus6(): X = plus3().plus3()
fun <W: S<*>, X: Q6<W>> X.minus6(): W = minus3().minus3()
fun <W: S<*>, X: Q7<W>> W.plus7(): X = plus3().plus4()
fun <W: S<*>, X: Q7<W>> X.minus7(): W = minus3().minus4()
fun <W: S<*>, X: Q8<W>> W.plus8(): X = plus4().plus4()
fun <W: S<*>, X: Q8<W>> X.minus8(): W = minus4().minus4()
fun <W: S<*>, X: Q9<W>> W.plus9(): X = plus4().plus5()
fun <W: S<*>, X: Q9<W>> X.minus9(): W = minus4().minus5()
fun <W: S<*>, X: Q10<W>> W.plus10(): X = plus5().plus5()
fun <W: S<*>, X: Q10<W>> X.minus10(): W = minus5().minus5()
fun <W: S<*>, X: Q11<W>> W.plus11(): X = plus5().plus6()
fun <W: S<*>, X: Q11<W>> X.minus11(): W = minus5().minus6()
fun <W: S<*>, X: Q12<W>> W.plus12(): X = plus6().plus6()
fun <W: S<*>, X: Q12<W>> X.minus12(): W = minus6().minus6()
fun <W: S<*>, X: Q13<W>> W.plus13(): X = plus6().plus7()
fun <W: S<*>, X: Q13<W>> X.minus13(): W = minus6().minus7()
fun <W: S<*>, X: Q14<W>> W.plus14(): X = plus7().plus7()
fun <W: S<*>, X: Q14<W>> X.minus14(): W = minus7().minus7()
fun <W: S<*>, X: Q15<W>> W.plus15(): X = plus7().plus8()
fun <W: S<*>, X: Q15<W>> X.minus15(): W = minus7().minus8()
fun <W: S<*>, X: Q16<W>> W.plus16(): X = plus8().plus8()
fun <W: S<*>, X: Q16<W>> X.minus16(): W = minus8().minus8()

@JvmName("n+0") operator fun <W: S<*>> W.plus(x: O): W = this
@JvmName("0+n") operator fun <X: S<*>> O.plus(x: X): X = x
@JvmName("n+1") operator fun <W: S<*>, X: S<O>> W.plus(x: X): S<W> = plus1()
@JvmName("1+n") operator fun <W: S<*>, X: S<O>> X.plus(w: W): S<W> = w.plus1()
@JvmName("n-1") operator fun <W: S<*>, X: S<W>, Y: S<O>> X.minus(y: Y): W = minus1()
@JvmName("n÷1") operator fun <W: S<*>, X: S<O>> W.div(x: X): W = this
@JvmName("n*1") operator fun <W: S<*>, X: S<O>> W.times(x: X): W = this
@JvmName("1*n") operator fun <W: S<*>, X: S<O>> X.times(w: W): W = w
@JvmName("n*0") operator fun <W: S<*>> W.times(x: O): O = O
@JvmName("0*n") operator fun <X: S<*>> O.times(x: X): O = O

@JvmName("n+2") operator fun <V: L2, W: S<*>, X: Q2<W>> W.plus(x: V): X = plus2()
@JvmName("n+3") operator fun <V: L3, W: S<*>, X: Q3<W>> W.plus(x: V): X = plus3()
@JvmName("n+4") operator fun <V: L4, W: S<*>, X: Q4<W>> W.plus(x: V): X = plus4()
@JvmName("n+5") operator fun <V: L5, W: S<*>, X: Q5<W>> W.plus(x: V): X = plus5()
@JvmName("n+6") operator fun <V: L6, W: S<*>, X: Q6<W>> W.plus(x: V): X = plus6()
@JvmName("n+7") operator fun <V: L7, W: S<*>, X: Q7<W>> W.plus(x: V): X = plus7()
@JvmName("n+8") operator fun <V: L8, W: S<*>, X: Q8<W>> W.plus(x: V): X = plus8()
@JvmName("n+9") operator fun <V: L9, W: S<*>, X: Q9<W>> W.plus(x: V): X = plus9()
@JvmName("n+10") operator fun <V: L10, W: S<*>, X: Q10<W>> W.plus(x: V): X = plus10()
@JvmName("n+11") operator fun <V: L11, W: S<*>, X: Q11<W>> W.plus(x: V): X = plus11()
@JvmName("n+12") operator fun <V: L12, W: S<*>, X: Q12<W>> W.plus(x: V): X = plus12()
@JvmName("n+13") operator fun <V: L13, W: S<*>, X: Q13<W>> W.plus(x: V): X = plus13()
@JvmName("n+14") operator fun <V: L14, W: S<*>, X: Q14<W>> W.plus(x: V): X = plus14()
@JvmName("n+15") operator fun <V: L15, W: S<*>, X: Q15<W>> W.plus(x: V): X = plus15()
@JvmName("n+16") operator fun <V: L16, W: S<*>, X: Q16<W>> W.plus(x: V): X = plus16()

@JvmName("n-2") operator fun <V: L2, W: S<*>, X: Q2<W>> X.minus(v: V): W = minus2()
@JvmName("n-3") operator fun <V: L3, W: S<*>, X: Q3<W>> X.minus(v: V): W = minus3()
@JvmName("n-4") operator fun <V: L4, W: S<*>, X: Q4<W>> X.minus(v: V): W = minus4()
@JvmName("n-5") operator fun <V: L5, W: S<*>, X: Q5<W>> X.minus(v: V): W = minus5()
@JvmName("n-6") operator fun <V: L6, W: S<*>, X: Q6<W>> X.minus(v: V): W = minus6()
@JvmName("n-7") operator fun <V: L7, W: S<*>, X: Q7<W>> X.minus(v: V): W = minus7()
@JvmName("n-8") operator fun <V: L8, W: S<*>, X: Q8<W>> X.minus(v: V): W = minus8()
@JvmName("n-9") operator fun <V: L9, W: S<*>, X: Q9<W>> X.minus(v: V): W = minus9()
@JvmName("n-10") operator fun <V: L10, W: S<*>, X: Q10<W>> X.minus(v: V): W = minus10()
@JvmName("n-11") operator fun <V: L11, W: S<*>, X: Q11<W>> X.minus(v: V): W = minus11()
@JvmName("n-12") operator fun <V: L12, W: S<*>, X: Q12<W>> X.minus(v: V): W = minus12()
@JvmName("n-13") operator fun <V: L13, W: S<*>, X: Q13<W>> X.minus(v: V): W = minus13()
@JvmName("n-14") operator fun <V: L14, W: S<*>, X: Q14<W>> X.minus(v: V): W = minus14()
@JvmName("n-15") operator fun <V: L15, W: S<*>, X: Q15<W>> X.minus(v: V): W = minus15()
@JvmName("n-16") operator fun <V: L16, W: S<*>, X: Q16<W>> X.minus(v: V): W = minus16()

@JvmName("2*2") operator fun <W: L2, X: L2> W.times(x: X): L4 = S4
@JvmName("2*3") operator fun <W: L2, X: L3> W.times(x: X): L6 = S6
@JvmName("2*4") operator fun <W: L2, X: L4> W.times(x: X): L8 = S8
@JvmName("2*5") operator fun <W: L2, X: L5> W.times(x: X): L10 = S10
@JvmName("2*6") operator fun <W: L2, X: L6> W.times(x: X): L12 = S12
@JvmName("2*7") operator fun <W: L2, X: L7> W.times(x: X): L14 = S14
@JvmName("2*8") operator fun <W: L2, X: L8> W.times(x: X): L16 = S16
@JvmName("3*2") operator fun <W: L3, X: L2> W.times(x: X): L6 = S6
@JvmName("3*3") operator fun <W: L3, X: L3> W.times(x: X): L9 = S9
@JvmName("3*4") operator fun <W: L3, X: L4> W.times(x: X): L12 = S12
@JvmName("3*5") operator fun <W: L3, X: L5> W.times(x: X): L15 = S15
@JvmName("4*2") operator fun <W: L4, X: L2> W.times(x: X): L8 = S8
@JvmName("4÷2") operator fun <W: L4, X: L2> W.div(x: X): L2 = S2
@JvmName("4*3") operator fun <W: L4, X: L3> W.times(x: X): L12 = S12
@JvmName("4*4") operator fun <W: L4, X: L4> W.times(x: X): L16 = S16
@JvmName("5*2") operator fun <W: L5, X: L2> W.times(x: X): L10 = S10
@JvmName("5*3") operator fun <W: L5, X: L3> W.times(x: X): L15 = S15
@JvmName("6*2") operator fun <W: L6, X: L2> W.times(x: X): L12 = S12
@JvmName("6÷2") operator fun <W: L6, X: L2> W.div(x: X): L3 = S3
@JvmName("6÷3") operator fun <W: L6, X: L3> W.div(x: X): L2 = S2
@JvmName("7*2") operator fun <W: L7, X: L2> W.times(x: X): L14 = S14
@JvmName("8*2") operator fun <W: L8, X: L2> W.times(x: X): L16 = S16
@JvmName("8÷2") operator fun <W: L8, X: L2> W.div(x: X): L4 = S4
@JvmName("8÷4") operator fun <W: L8, X: L4> W.div(x: X): L2 = S2
@JvmName("9÷3") operator fun <W: L9, X: L3> W.div(x: X): L3 = S3
@JvmName("10÷2") operator fun <W: L10, X: L2> W.div(x: X): L5 = S5
@JvmName("10÷5") operator fun <W: L10, X: L5> W.div(x: X): L2 = S2
@JvmName("12÷2") operator fun <W: L12, X: L2> W.div(x: X): L6 = S6
@JvmName("12÷3") operator fun <W: L12, X: L3> W.div(x: X): L4 = S4
@JvmName("12÷4") operator fun <W: L12, X: L4> W.div(x: X): L3 = S3
@JvmName("12÷6") operator fun <W: L12, X: L6> W.div(x: X): L2 = S2
@JvmName("14÷2") operator fun <W: L14, X: L2> W.div(x: X): L7 = S7
@JvmName("14÷7") operator fun <W: L14, X: L7> W.div(x: X): L2 = S2
@JvmName("15÷3") operator fun <W: L15, X: L3> W.div(x: X): L5 = S5
@JvmName("15÷5") operator fun <W: L15, X: L5> W.div(x: X): L3 = S3
@JvmName("16÷2") operator fun <W: L16, X: L2> W.div(x: X): L8 = S8
@JvmName("16÷4") operator fun <W: L16, X: L4> W.div(x: X): L4 = S4
@JvmName("16÷8") operator fun <W: L16, X: L8> W.div(x: X): L2 = S2