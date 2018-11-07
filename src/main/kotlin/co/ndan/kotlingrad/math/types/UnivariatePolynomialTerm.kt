package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.calculus.Function

class UnivariatePolynomialTerm<X : Field<X>>(protected var coefficient: Long,
                                             override val arg: Function<X>,
                                             protected var exponent: Int) : UnaryFunction<X>(arg) {
  override val value: X
    get() = arg.value.pow(exponent) * coefficient

  override fun differentiate(ind: Var<X>) = UnivariatePolynomialTerm(coefficient * exponent, arg, exponent - 1) * arg.differentiate(ind)

  override fun toString() = "$coefficient$arg^$exponent"

  override fun inverse() = UnivariatePolynomialTerm(coefficient, arg, -exponent)

  override fun unaryMinus() = UnivariatePolynomialTerm(-coefficient, arg, exponent)
}