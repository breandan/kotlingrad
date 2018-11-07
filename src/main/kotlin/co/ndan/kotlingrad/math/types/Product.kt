package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.calculus.Function

class Product<X : Field<X>>(multiplicator: Function<X>, multiplicand: Function<X>) : BiFunction<X>(multiplicator, multiplicand) {
  override val value: X
    get() = lfn.value * rfn.value

  override fun differentiate(ind: Var<X>) =
    if (lfn === rfn) lfn.differentiate(ind) * rfn * 2L
    else lfn.differentiate(ind) * rfn + lfn * rfn.differentiate(ind)

  override fun toString() = "($lfn * $rfn)"
}