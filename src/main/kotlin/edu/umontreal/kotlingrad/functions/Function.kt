package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.Group

interface Function<X: Function<X>>: Group<Function<X>> {
  operator fun invoke(map: Map<Var<X>, X> = emptyMap()): Function<X> = when (this) {
    is Const -> this
    is Var -> map.getOrElse(this) { value }
    is Product -> multiplicator(map) * multiplicand(map)
    is Sum -> augend(map) + addend(map)
    else -> throw Exception("Unknown type")
  }

  val variables: Set<Var<X>>
  operator fun invoke(vararg pair: Pair<Var<X>, X>) = invoke(pair.toMap())
}

interface Const<X: Function<X>>: Function<X>

interface Sum<X: Function<X>>: Function<X> {
  val augend: Function<X>
  val addend: Function<X>
}

interface Product<X: Function<X>>: Function<X> {
  val multiplicator: Function<X>
  val multiplicand: Function<X>
}

interface Var<X: Function<X>>: Function<X> {
  val value: X
}
