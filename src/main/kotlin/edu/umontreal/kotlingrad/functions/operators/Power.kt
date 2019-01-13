package edu.umontreal.kotlingrad.functions.operators

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.BinaryFunction
import edu.umontreal.kotlingrad.functions.Function
import edu.umontreal.kotlingrad.functions.types.Const
import edu.umontreal.kotlingrad.functions.types.One
import edu.umontreal.kotlingrad.functions.types.Var

class Power<X: Field<X>>(
  private val base: Function<X>,
  var exponent: Function<X>
): BinaryFunction<X>(base, exponent) {
  override fun invoke(map: Map<Var<X>, X>): X = base.invoke(map).pow(exponent(map))

  override fun diff(ind: Var<X>) = when (exponent) {
    is One -> base.diff(ind)
    is Const -> exponent * Power(base, Const((exponent - one)())) * base.diff(ind)
    else -> this * (exponent * base.ln()).diff(ind)
  }

  override fun toString() = "($base${superscript(exponent)})"

  override fun inverse() = Power(base, -exponent)

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
    else
      "^($exponent)"
}