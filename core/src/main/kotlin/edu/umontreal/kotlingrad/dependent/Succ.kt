package edu.umontreal.kotlingrad.dependent

interface Natural
interface Succ<N: Natural>: Natural
interface t0: Natural
typealias t1 = Succ<t0>
typealias t2 = Succ<t1>
typealias t3 = Succ<t2>
typealias t4 = Succ<t3>
typealias t5 = Succ<t4>
typealias t6 = Succ<t5>
typealias t7 = Succ<t6>
typealias t8 = Succ<t7>
typealias t9 = Succ<t8>
open class Vect<T, Length: Natural>(t: T, vararg val arr: Int)

fun main() {
  val t = Vect<Int, t5>(1, 2, 3, 4, 5, 6)
}
