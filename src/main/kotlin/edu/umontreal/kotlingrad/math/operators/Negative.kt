package edu.umontreal.kotlingrad.math.operators

import edu.umontreal.kotlingrad.math.algebra.Field
import edu.umontreal.kotlingrad.math.functions.Function
import edu.umontreal.kotlingrad.math.functions.UnaryFunction
import edu.umontreal.kotlingrad.math.types.Var

class Negative<X: Field<X>>(override val arg: Function<X>): UnaryFunction<X>(arg) {
  override fun invoke(map: Map<Var<X>, X>) = -arg(map)


  override fun diff(ind: Var<X>) = -arg.diff(ind)

  override fun toString() = "-$arg"

  override fun unaryMinus() = arg
}