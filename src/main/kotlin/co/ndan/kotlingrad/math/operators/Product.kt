package co.ndan.kotlingrad.math.operators

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.functions.BinaryFunction
import co.ndan.kotlingrad.math.functions.Function
import co.ndan.kotlingrad.math.types.Var

class Product<X: Field<X>>(multiplicator: Function<X>, multiplicand: Function<X>): BinaryFunction<X>(multiplicator, multiplicand) {
  override val value: X
    get() = lfn.value * rfn.value

  // Product rule: d(u*v)/dx = du/dx * v + u * dv/dx
  override fun differentiate(ind: Var<X>) =
    if (lfn === rfn) lfn.differentiate(ind) * rfn * 2L
    else lfn.differentiate(ind) * rfn + lfn * rfn.differentiate(ind)

  override fun toString() = "($lfn * $rfn)"
}