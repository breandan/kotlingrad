package edu.umontreal.kotlingrad.functions.operators

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.BinaryFunction
import edu.umontreal.kotlingrad.functions.Function
import edu.umontreal.kotlingrad.functions.types.Var

class Product<X: Field<X>>(
    val multiplicator: Function<X>,
    val multiplicand: Function<X>
): BinaryFunction<X>(multiplicator, multiplicand) {
  override fun invoke(map: Map<Var<X>, X>) = multiplicator(map) * multiplicand(map)

  // Product rule: d(u*v)/dx = du/dx * v + u * dv/dx
  override fun diff(ind: Var<X>) =
      if (multiplicator === multiplicand) multiplicator.diff(ind) * multiplicand * two
      else multiplicator.diff(ind) * multiplicand + multiplicator * multiplicand.diff(ind)

  override fun toString() = "($multiplicator * $multiplicand)"
}