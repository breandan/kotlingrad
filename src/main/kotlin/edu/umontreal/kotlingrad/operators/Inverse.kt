package edu.umontreal.kotlingrad.operators

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.Function
import edu.umontreal.kotlingrad.functions.UnaryFunction
import edu.umontreal.kotlingrad.types.UnivariatePolynomialTerm
import edu.umontreal.kotlingrad.types.Var

class Inverse<X: Field<X>>(override val arg: Function<X>): UnaryFunction<X>(arg) {
  override fun invoke(map: Map<Var<X>, X>) = arg(map).inverse()

  override fun diff(ind: Var<X>) = UnivariatePolynomialTerm(-1L, arg, -2) * arg.diff(ind)

  override fun toString() = "$arg⁻¹"

  override fun inverse() = arg
}