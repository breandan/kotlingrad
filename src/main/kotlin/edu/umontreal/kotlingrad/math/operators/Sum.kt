package edu.umontreal.kotlingrad.math.operators

import edu.umontreal.kotlingrad.math.algebra.Field
import edu.umontreal.kotlingrad.math.functions.BinaryFunction
import edu.umontreal.kotlingrad.math.functions.Function
import edu.umontreal.kotlingrad.math.types.Var
import java.util.function.BiFunction

class Sum<X: Field<X>>(augend: Function<X>, addend: Function<X>): BinaryFunction<X>(augend, addend), BiFunction<Function<X>, Function<X>, Sum<X>> {
  override fun apply(t: Function<X>, u: Function<X>): Sum<X> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun invoke(map: Map<Var<X>, X>) = lfn(map) + rfn(map)
  // Some operations are inherently parallelizable. TODO: Explore how to parallelize these with FP...


  override fun diff(ind: Var<X>) =
      if (lfn === rfn) lfn.diff(ind) * 2L
      else lfn.diff(ind) + rfn.diff(ind)

  override fun toString() = "($lfn + $rfn)"
}