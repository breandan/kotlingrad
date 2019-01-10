package edu.umontreal.kotlingrad.functions.operators

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.BinaryFunction
import edu.umontreal.kotlingrad.functions.Function
import edu.umontreal.kotlingrad.functions.types.Var

class Sum<X: Field<X>>(
    val augend: Function<X>,
    val addend: Function<X>
): BinaryFunction<X>(augend, addend) {
  // Some operations are inherently parallelizable. TODO: Explore how to parallelize these with FP...
  override fun invoke(map: Map<Var<X>, X>) = augend(map) + addend(map)

  override fun diff(ind: Var<X>) = if (augend === addend) augend.diff(ind) * two else augend.diff(ind) + addend.diff(ind)

  override fun toString() = "($augend + $addend)"
}