package edu.umontreal.kotlingrad.types

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.Function
import edu.umontreal.kotlingrad.functions.UnaryFunction

class UnivariatePolynomialTerm<X: Field<X>>(
    protected var coefficient: Long,
    override val arg: Function<X>,
    protected var exponent: Int
): UnaryFunction<X>(arg) {
  override fun invoke(map: Map<Var<X>, X>): X = arg(map).pow(exponent) * coefficient

  override fun diff(ind: Var<X>) = UnivariatePolynomialTerm(coefficient * exponent, arg, exponent - 1) * arg.diff(ind)

  override fun toString() = "($coefficient * $arg${superscript(exponent)})"

  override fun inverse() = UnivariatePolynomialTerm(coefficient, arg, -exponent)

  override fun unaryMinus() = UnivariatePolynomialTerm(-coefficient, arg, exponent)

  private fun superscript(exponent: Int) = exponent.toString()
      .replace("-", "⁻")
      .replace("0", "⁰")
      .replace("1", "¹")
      .replace("2", "²")
      .replace("3", "³")
      .replace("4", "⁴")
      .replace("5", "⁵")
      .replace("6", "⁶")
      .replace("7", "⁷")
      .replace("8", "⁸")
      .replace("9", "⁹")
}