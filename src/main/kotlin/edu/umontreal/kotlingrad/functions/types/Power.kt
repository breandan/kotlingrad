package edu.umontreal.kotlingrad.functions.types

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.BinaryFunction
import edu.umontreal.kotlingrad.functions.Function

class Power<X: Field<X>>(
  val base: Function<X>,
  var exponent: Function<X>
): BinaryFunction<X>(base, exponent) {
  override fun invoke(map: Map<Var<X>, X>): X = base.invoke(map).pow(exponent(map))

  override fun diff(ind: Var<X>) = when {
    exponent is One -> base.diff(ind)
    // TODO: Generalize this to true functions instead of constants...
    else ->
      exponent * Power(base, Const((exponent - one)(/*TODO: ONLY WORKS FOR CONSTANTS! FIX THIS!*/), variables.first().prototype)) * base.diff(ind)
  }

  override fun toString() = "($base$exponent)"

  override fun inverse() = Power(base, -exponent)

  override fun unaryMinus() = Power(base, exponent)

  private fun superscript(exponent: Function<X>) =
    if (exponent is Const)
      if (exponent == one) ""
      else exponent.toString()
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
    else "^($exponent)"
}