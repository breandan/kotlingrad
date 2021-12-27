@file:Suppress("UNCHECKED_CAST")

package ai.hypergraph.kotlingrad.typelevel.arity

import ai.hypergraph.kaliningraph.graphs.*
import kotlin.reflect.KProperty

sealed class XO // Represents either a bound or unbound variable
abstract class XX: XO() // Represents an unbound variable
abstract class OO: XO() // Represents a bound variable

interface IEx<X: IEx<X>> {
  val exs: Array<out X>
  val op: Op?
  val name: String?
  // Call a function with all free variables bound - must bind all free variables or this will throw an error
  fun <N: Number> call(vararg vrb: VrB<N>): N = (inv<N, OOO>(*vrb) as Nt<N>).value

  // Partially evaluate a function by binding only a subset of the free variables, returning another function
  fun <N: Number, Q> inv(vararg bnds: VrB<N>): Q =
          (if (exs.isEmpty()) this
          else exs.map { it.inv<N, Q>(*bnds) as X }
                  .reduce { it, acc -> apply(acc, it) }) as Q

  // Combine two Xs using op - returns a function if either is a function, otherwise returns a value
  fun apply(me: X, that: X): X
}

open class Ex<V1: XO, V2: XO, V3: XO> constructor(
        override val op: Op? = null,
        override val name: String? = null,
        override vararg val exs: Ex<*, *, *>,
): IEx<Ex<*, *, *>> {
  open operator fun getValue(n: Nothing?, property: KProperty<*>): Ex<V1, V2, V3> = Ex(op, property.name, *exs)
  override fun toString() = exs.joinToString("$op") {
    if (op in arrayOf(Ops.prod, Ops.ratio) && it.op in arrayOf(Ops.sum, Ops.sub)) "($it)" else "$it"
  }.let { name?.run { "$name = $it" } ?: it }

  override fun apply(me: Ex<*, *, *>, that: Ex<*, *, *>) =
          if (op == null) that else if (me is Nt<*> && that is Nt<*>)
            when (op) {
              Ops.sum -> Nt(me.value.toDouble() + that.value.toDouble())
              Ops.sub -> Nt(me.value.toDouble() - that.value.toDouble())
              Ops.prod -> Nt(me.value.toDouble() * that.value.toDouble())
              Ops.ratio -> Nt(me.value.toDouble() / that.value.toDouble())
              else -> TODO()
            } else Ex(op, null, me, that)
}

open class Nt<T: Number>(val value: T): Ex<OO, OO, OO>() {
  override fun toString() = value.toString()
}

val x by V1()
val y by V2()
val z by V3()

open class V1 internal constructor(name: String = "v1"): Vr<XX, OO, OO>(name) {
  override fun getValue(n: Nothing?, property: KProperty<*>) = V1(property.name)
}

open class V2 internal constructor(name: String = "v2"): Vr<OO, XX, OO>(name) {
  override fun getValue(n: Nothing?, property: KProperty<*>) = V2(property.name)
}

open class V3 internal constructor(name: String = "v3"): Vr<OO, OO, XX>(name) {
  override fun getValue(n: Nothing?, property: KProperty<*>) = V3(property.name)
}

class V1Bnd<N: Number> internal constructor(vr: V1, value: N): VrB<N>(vr, value)
class V2Bnd<N: Number> internal constructor(vr: V2, value: N): VrB<N>(vr, value)
class V3Bnd<N: Number> internal constructor(vr: V3, value: N): VrB<N>(vr, value)

infix fun <N: Number> V1.to(n: N) = V1Bnd(this, n)
infix fun <N: Number> V2.to(n: N) = V2Bnd(this, n)
infix fun <N: Number> V3.to(n: N) = V3Bnd(this, n)

open class VrB<N: Number>(open val vr: Vr<*, *, *>, val value: N)
sealed class Vr<R: XO, S: XO, T: XO>(override val name: String): Ex<R, S, T>() {
  override fun <T: Number, Q> inv(vararg bnds: VrB<T>): Q =
    bnds.associate { it.vr::class to it.value }
      .let { it[this::class]?.let { Nt(it) } ?: this } as Q

  override fun toString() = name
}