package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.calculus.Function

class Product<X : Field<X>>(multiplicator: Function<X>, multiplicand: Function<X>) : BiFunction<X>(multiplicator, multiplicand) {
  override val value: X
    get() = lfn.value * rfn.value

  override fun differentiate(arg: Var<X>) =
    if (lfn === rfn) lfn.differentiate(arg) * rfn * 2L
    else lfn.differentiate(arg) * rfn + lfn * rfn.differentiate(arg)

  override fun toString() = "($lfn * $rfn)"
}
