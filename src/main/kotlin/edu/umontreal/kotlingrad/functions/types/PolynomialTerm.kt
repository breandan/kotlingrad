package edu.umontreal.kotlingrad.functions.types

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.Function
import edu.umontreal.kotlingrad.functions.TernaryFunction

class PolynomialTerm<X: Field<X>>(
    protected var coefficient: Function<X>,
    val arg: Function<X>,
    var exponent: Function<X>
): TernaryFunction<X>(coefficient, arg, exponent) {
  override fun invoke(map: Map<Var<X>, X>): X = coefficient(map) * arg.invoke(map).pow(exponent(map))

  override fun diff(ind: Var<X>) = when {
    exponent is One -> (arg * coefficient).diff(ind)
    // TODO: Generalize this to true functions instead of constants...
    else -> PolynomialTerm(coefficient * exponent, arg, Const((exponent - one)(/*TODO: ONLY WORKS FOR CONSTANTS! FIX THIS!*/), variables.first().prototype)) * arg.diff(ind)
  }

  override fun toString() = "($coefficient * $arg${superscript(exponent)})"

  override fun inverse() = PolynomialTerm(coefficient, arg, -exponent)

  override fun unaryMinus() = PolynomialTerm(-coefficient, arg, exponent)

  private fun superscript(exponent: Function<X>) =
      if (exponent == one) "" else exponent.toString()
          .replace(".", "⋅")
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