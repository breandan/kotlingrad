package edu.umontreal.kotlingrad.ice9

import edu.mcgill.kaliningraph.Vertex.Companion.randomString
import edu.umontreal.kotlingrad.ice9.ctxs.*
import kotlin.reflect.KProperty

//open class Num
//
//object X: Num()
//object Y: Num()
//object Z: Num()

open class Fx<M: Typ<M>>(val x: M) {
  operator fun plus(arg: Fx<M>): Fx<M> = when (arg.x) {
    is Dbl -> "dbl"
    is Int -> "int"
    else -> UnknownError("asdf")
  }.let { this }

  fun <T> sin(): T = TODO()

  fun <T: Typ<T>> Fx<T>.hello(): Fx<T> = TODO()
}

sealed class Typ<M: Typ<M>>
object Dbl: Typ<Dbl>()
object Int: Typ<Int>()

class Alg<M: Typ<M>>(d: M) {
  fun <Q, T: Fx<Q>> tx(t: T): T = TODO()
  fun Var(): Fx<M> = TODO()
}

open class Var<M: Typ<M>>(val d: M, val id: String = randomString()): Fx<M>(d) {
  operator fun getValue(m: Var<M>?, property: KProperty<*>): Var<M> =
    Var(d, property.name)
}

class VarInt: Var<Int>(Int)
class VarDbl: Var<Dbl>(Dbl)

fun main() {
  val z by VarInt()
  val a by VarInt()
  val q = sin(z + a) + sin(z)
}