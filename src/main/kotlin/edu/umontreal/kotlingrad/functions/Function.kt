package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.Group

interface Function<X: Group<X>> {
  operator fun invoke(map: Map<Var<X>, X> = emptyMap()): X = when (this) {
    is Const -> value
    is Var -> map.getOrElse(this) { value }
    is Product -> multiplicator(map) * multiplicand(map)
    is Sum -> augend(map) + addend(map)
    else -> throw Exception("Unknown")
  }

  val variables: Set<Var<X>>
  operator fun invoke(vararg pair: Pair<Var<X>, X>) = invoke(pair.toMap())
}

interface Const<X: Group<X>>: Function<X> {
  val value: X
}

interface Sum<X: Group<X>>: Function<X> {
  val augend: Function<X>
  val addend: Function<X>
}

interface Product<X: Group<X>>: Function<X> {
  val multiplicator: Function<X>
  val multiplicand: Function<X>
}

interface Var<X: Group<X>>: Function<X> {
  val value: X
}
