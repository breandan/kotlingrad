package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.algebra.Real
import edu.umontreal.kotlingrad.functions.*
import edu.umontreal.kotlingrad.functions.Function
import edu.umontreal.kotlingrad.numerical.FieldPrototype
import edu.umontreal.kotlingrad.functions.types.*

abstract class RealFunctor<X: Real<X>>(val rfc: FieldPrototype<X>): FieldFunctor<X>() {
  fun value(fnx: X) = Const(fnx)

  fun value(vararg fnx: X) = ConstVector(*fnx.mapTo(ArrayList(fnx.size)) { value(it) }.toTypedArray())

//  fun zero(size: Int) = ConstVector(*Array(size) { zero })
//  fun function(vararg fns: Function<X>) = VectorFunction(*fns)

  fun variable() = Var(rfc)

  fun variable(default: X) = Var(rfc, value = default)

  fun variable(name: String) = Var(rfc, name = name)

  fun variable(name: String, default: X) = Var(rfc, default, name)

  fun cos(angle: Function<X>) = Cosine(angle)

  fun sin(angle: Function<X>) = Sine(angle)

  fun tan(angle: Function<X>) = Tangent(angle)

  fun exp(exponent: Function<X>) = Exp(exponent)
  
  fun pow(base: Function<X>, exponent: Function<X>) = Power(base, exponent)

  fun sqrt(radicand: Function<X>): Function<X> = SquareRoot(radicand)

  class IndVar<X: Field<X>> constructor(val variable: Var<X>)

  class Differential<X: Field<X>>(val function: Function<X>) {
    operator fun div(arg: IndVar<X>) = function.diff(arg.variable)
  }

  fun <X: Field<X>> d(function: Function<X>) = Differential(function)
  fun <X: Field<X>> d(arg: Var<X>) = IndVar(arg)
}