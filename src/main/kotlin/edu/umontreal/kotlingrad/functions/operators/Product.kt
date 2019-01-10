package edu.umontreal.kotlingrad.functions.operators

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.BinaryFunction
import edu.umontreal.kotlingrad.functions.Function
import edu.umontreal.kotlingrad.functions.types.Var

class Product<X: Field<X>>(
    multiplicator: Function<X>,
    multiplicand: Function<X>
): BinaryFunction<X>(multiplicator, multiplicand) {
  override fun invoke(map: Map<Var<X>, X>) = lfn(map) * rfn(map)

  // Product rule: d(u*v)/dx = du/dx * v + u * dv/dx
  override fun diff(ind: Var<X>) =
      if (lfn === rfn) lfn.diff(ind) * rfn * (one + one) else lfn.diff(ind) * rfn + lfn * rfn.diff(ind)

  override fun toString() = "($lfn * $rfn)"
}