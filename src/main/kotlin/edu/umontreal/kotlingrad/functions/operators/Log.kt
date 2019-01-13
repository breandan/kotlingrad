package edu.umontreal.kotlingrad.functions.operators

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.algebra.FieldPrototype
import edu.umontreal.kotlingrad.functions.Function
import edu.umontreal.kotlingrad.functions.UnaryFunction
import edu.umontreal.kotlingrad.functions.types.Var

class Log<X: Field<X>>(
  val logarithmand: Function<X>
): UnaryFunction<X>(logarithmand) {
  override fun invoke(map: Map<Var<X>, X>): X = prototype.log(logarithmand(map))

  override fun diff(ind: Var<X>) = Inverse(logarithmand) * logarithmand.diff(ind)

  override fun toString(): String = "log₁₀($logarithmand)"
}