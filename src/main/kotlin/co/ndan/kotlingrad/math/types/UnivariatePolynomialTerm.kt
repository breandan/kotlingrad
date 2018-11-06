package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.calculus.Function

class UnivariatePolynomialTerm<X : Field<X>>(protected var coefficient: Long,
                                             override val fnx: Function<X>,
                                             protected var exponent: Int) : UnaryFunction<X>(fnx) {
  override val value: X
    get() = fnx.value.pow(exponent) * coefficient

  override fun differentiate(arg: Var<X>) = UnivariatePolynomialTerm(coefficient * exponent, fnx, exponent - 1) * fnx.differentiate(arg)

  override fun toString() = "$coefficient$fnx^$exponent"

  override fun inverse() = UnivariatePolynomialTerm(coefficient, fnx, -exponent)

  override fun unaryMinus() = UnivariatePolynomialTerm(-coefficient, fnx, exponent)
}
