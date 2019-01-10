package edu.umontreal.kotlingrad.functions.operators

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.Function
import edu.umontreal.kotlingrad.functions.UnaryFunction
import edu.umontreal.kotlingrad.functions.types.Var

class Negative<X: Field<X>>(val arg: Function<X>): UnaryFunction<X>(arg) {
  override fun invoke(map: Map<Var<X>, X>) = -arg(map)

  override fun diff(ind: Var<X>) = -arg.diff(ind)

  override fun toString() = "-$arg"

  override fun unaryMinus() = arg
}