package edu.umontreal.kotlingrad.ice9

import edu.umontreal.kotlingrad.ice9.ctxs.*

//open class Num
//
//object X: Num()
//object Y: Num()
//object Z: Num()

class Fx<M: Typ>(x: M) {
  operator fun plus(arg: Var): Fx<M> = TODO()
  fun <T> sin(): T = TODO()
}

open class Typ
object Dbl: Typ()
object Int: Typ()

class Alg<M: Typ>(d: M) {
  fun <Q, T: Fx<Q>> tx(t: T): T = TODO()
  fun Var(): Fx<M> = TODO()
}

class Var()

interface IK
interface I: IK
interface J: IK
interface K: IK
open class H

fun main() {
  val z = Alg(Dbl).Var()
  val q = sin(z + Var())
}