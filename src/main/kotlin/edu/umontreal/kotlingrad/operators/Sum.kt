package edu.umontreal.kotlingrad.operators

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.BinaryFunction
import edu.umontreal.kotlingrad.functions.Function
import edu.umontreal.kotlingrad.types.Var

class Sum<X: Field<X>>(
    augend: Function<X>,
    addend: Function<X>
): BinaryFunction<X>(augend, addend) {
  // Some operations are inherently parallelizable. TODO: Explore how to parallelize these with FP...
  override fun invoke(map: Map<Var<X>, X>) = lfn(map) + rfn(map)

  override fun diff(ind: Var<X>) = if (lfn === rfn) lfn.diff(ind) * 2L else lfn.diff(ind) + rfn.diff(ind)

  override fun toString() = "($lfn + $rfn)"
}