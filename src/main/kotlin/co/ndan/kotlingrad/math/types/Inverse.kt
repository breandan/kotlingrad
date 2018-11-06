package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.calculus.Function

class Inverse<X : Field<X>>(override val fnx: Function<X>) : UnaryFunction<X>(fnx) {
  override val value: X
    get() = fnx.value.inverse()

  override fun differentiate(arg: Var<X>) = UnivariatePolynomialTerm(-1L, fnx, -2) * fnx.differentiate(arg)

  override fun toString() = "($fnx)^(-1)"

  override fun inverse() = fnx
}
