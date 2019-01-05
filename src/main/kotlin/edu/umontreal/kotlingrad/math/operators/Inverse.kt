package edu.umontreal.kotlingrad.math.operators

import edu.umontreal.kotlingrad.math.algebra.Field
import edu.umontreal.kotlingrad.math.functions.Function
import edu.umontreal.kotlingrad.math.functions.UnaryFunction
import edu.umontreal.kotlingrad.math.types.UnivariatePolynomialTerm
import edu.umontreal.kotlingrad.math.types.Var

class Inverse<X: Field<X>>(override val arg: Function<X>): UnaryFunction<X>(arg) {
  override fun invoke(map: Map<Var<X>, X>) = arg(map).inverse()

  override fun diff(ind: Var<X>) = UnivariatePolynomialTerm(-1L, arg, -2) * arg.diff(ind)

  override fun toString() = "$arg⁻¹"

  override fun inverse() = arg
}