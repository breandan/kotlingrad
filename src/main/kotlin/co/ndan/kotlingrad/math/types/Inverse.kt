package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.calculus.Function

class Inverse<X: Field<X>>(override val arg: Function<X>): UnaryFunction<X>(arg) {
  override val value: X
    get() = arg.value.inverse()

  override fun differentiate(ind: Var<X>) = UnivariatePolynomialTerm(-1L, arg, -2) * arg.differentiate(ind)

  override fun toString() = "$arg⁻¹"

  override fun inverse() = arg
}