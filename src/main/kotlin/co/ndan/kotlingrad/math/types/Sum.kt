package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.calculus.Function

class Sum<X : Field<X>>(augend: Function<X>, addend: Function<X>) : BiFunction<X>(augend, addend) {
  override val value: X
    get() = lfn.value + rfn.value

  override fun differentiate(ind: Var<X>) =
    if (lfn === rfn) lfn.differentiate(ind) * 2L
    else lfn.differentiate(ind) + rfn.differentiate(ind)

  override fun toString() = "($lfn + $rfn)"
}