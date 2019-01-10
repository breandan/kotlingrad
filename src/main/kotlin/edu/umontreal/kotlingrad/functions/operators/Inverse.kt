package edu.umontreal.kotlingrad.functions.operators

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.Function
import edu.umontreal.kotlingrad.functions.UnaryFunction
import edu.umontreal.kotlingrad.functions.types.Const
import edu.umontreal.kotlingrad.functions.types.PolynomialTerm
import edu.umontreal.kotlingrad.functions.types.Var

class Inverse<X: Field<X>>(val arg: Function<X>): UnaryFunction<X>(arg) {
  override fun invoke(map: Map<Var<X>, X>) = arg(map).inverse()

  override fun diff(ind: Var<X>) =
      PolynomialTerm(-one, arg, Const((-one - one)(), variables.first().prototype)) * arg.diff(ind)

  override fun toString() = "$arg⁻¹"

  override fun inverse() = arg
}