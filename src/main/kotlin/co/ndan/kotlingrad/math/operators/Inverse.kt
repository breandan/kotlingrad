package co.ndan.kotlingrad.math.operators

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.functions.Function
import co.ndan.kotlingrad.math.functions.UnaryFunction
import co.ndan.kotlingrad.math.types.UnivariatePolynomialTerm
import co.ndan.kotlingrad.math.types.Var

class Inverse<X: Field<X>>(override val arg: Function<X>): UnaryFunction<X>(arg) {
  override fun invoke(map: Map<Var<X>, X>) = arg(map).inverse()

  override fun differentiate(ind: Var<X>) = UnivariatePolynomialTerm(-1L, arg, -2) * arg.differentiate(ind)

  override fun toString() = "$arg⁻¹"

  override fun inverse() = arg
}