package co.ndan.kotlingrad.math.operators

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.functions.BinaryFunction
import co.ndan.kotlingrad.math.functions.Function
import co.ndan.kotlingrad.math.types.Var

class Sum<X: Field<X>>(augend: Function<X>, addend: Function<X>): BinaryFunction<X>(augend, addend) {
  override fun invoke(map: Map<Var<X>, X>) = lfn(map) + rfn(map)
  // Some operations are inherently parallelizable. TODO: Explore how to parallelize these with FP...


  override fun differentiate(ind: Var<X>) =
      if (lfn === rfn) lfn.differentiate(ind) * 2L
      else lfn.differentiate(ind) + rfn.differentiate(ind)

  override fun toString() = "($lfn + $rfn)"
}