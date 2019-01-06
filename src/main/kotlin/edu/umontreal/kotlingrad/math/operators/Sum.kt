package edu.umontreal.kotlingrad.math.operators

import edu.umontreal.kotlingrad.math.algebra.Field
import edu.umontreal.kotlingrad.math.functions.BinaryFunction
import edu.umontreal.kotlingrad.math.functions.Function
import edu.umontreal.kotlingrad.math.types.Var

class Sum<X: Field<X>>(
    augend: Function<X>,
    addend: Function<X>
): BinaryFunction<X>(augend, addend) {
  // Some operations are inherently parallelizable. TODO: Explore how to parallelize these with FP...
  override fun invoke(map: Map<Var<X>, X>) = lfn(map) + rfn(map)

  override fun diff(ind: Var<X>) = if (lfn === rfn) lfn.diff(ind) * 2L else lfn.diff(ind) + rfn.diff(ind)

  override fun toString() = "($lfn + $rfn)"
}