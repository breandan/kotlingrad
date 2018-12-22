package co.ndan.kotlingrad.math.calculus

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.functions.Function
import co.ndan.kotlingrad.math.types.Var

open class Differential<X: Field<X>> private constructor(val function: Function<X>) {
  operator fun div(arg: IndVar<X>) = function.differentiate(arg.variable)

  class IndVar<X: Field<X>> constructor(val variable: Var<X>)

  companion object {
    fun <X: Field<X>> d(function: Function<X>) = Differential(function)
    fun <X: Field<X>> d(arg: Var<X>) = IndVar(arg)
  }
}