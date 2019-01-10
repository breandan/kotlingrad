package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.algebra.Real
import edu.umontreal.kotlingrad.algebra.RealPrototype
import edu.umontreal.kotlingrad.functions.Function
import edu.umontreal.kotlingrad.functions.TernaryFunction
import edu.umontreal.kotlingrad.functions.UnaryFunction
import edu.umontreal.kotlingrad.functions.operators.Inverse
import edu.umontreal.kotlingrad.functions.types.*

abstract class RealFunctor<X: Real<X>>(val rfc: RealPrototype<X>): FieldFunctor<X>() {
  fun value(fnx: X) = Const(fnx, rfc)

  fun value(vararg fnx: X) = ConstVector(*fnx.mapTo(ArrayList(fnx.size)) { value(it) }.toTypedArray())

//  fun zero(size: Int) = ConstVector(*Array(size) { zero })
//  fun function(vararg fns: Function<X>) = VectorFunction(*fns)

  fun variable() = Var(rfc)

  fun variable(default: X) = Var(rfc, value = default)

  fun variable(name: String) = Var(rfc, name = name)

  fun variable(name: String, default: X) = Var(rfc, default, name)

  val zero = Zero(rfc)

  val one = One(rfc)

  fun cos(angle: Function<X>) = object: UnaryFunction<X>(angle) {
    override fun invoke(map: Map<Var<X>, X>): X = rfc.cos(angle(map))

    override fun diff(ind: Var<X>) = -(sin(angle) * angle.diff(ind))

    override fun toString() = "cos($angle)"
  }

  fun sin(angle: Function<X>): Function<X> = object: UnaryFunction<X>(angle) {
    override fun invoke(map: Map<Var<X>, X>): X = rfc.sin(angle(map))

    override fun diff(ind: Var<X>) = cos(angle) * angle.diff(ind)

    override fun toString(): String = "sin($angle)"
  }

  fun tan(angle: Function<X>): Function<X> = object: UnaryFunction<X>(angle) {
    override fun invoke(map: Map<Var<X>, X>): X = rfc.tan(angle(map))

    override fun diff(ind: Var<X>) = PolynomialTerm(one, cos(angle), -two) * angle.diff(ind)

    override fun toString(): String = "tan($angle)"
  }

  fun exp(exponent: Function<X>): Function<X> = object: UnaryFunction<X>(exponent) {
    override fun invoke(map: Map<Var<X>, X>): X = rfc.exp(exponent(map))

    override fun diff(ind: Var<X>) = exp(exponent) * exponent.diff(ind)

    override fun toString(): String = "exp($exponent)"
  }

  fun log(logarithmand: Function<X>) = object: UnaryFunction<X>(logarithmand) {
    override fun invoke(map: Map<Var<X>, X>): X = rfc.log(logarithmand(map))

    override fun diff(ind: Var<X>) = Inverse(logarithmand) * logarithmand.diff(ind)

    override fun toString(): String = "log₁₀($logarithmand)"
  }

  // TODO: Allow functions in exponent
  fun pow(base: Function<X>, exponent: Const<X>): TernaryFunction<X> = PolynomialTerm(one, base, exponent)

  fun sqrt(radicand: Function<X>): Function<X> = object: UnaryFunction<X>(radicand) {
    override fun invoke(map: Map<Var<X>, X>): X = rfc.sqrt(radicand(map))

    override fun diff(ind: Var<X>) = sqrt(radicand).inverse() / this@RealFunctor.value(rfc.one + rfc.one) * radicand.diff(ind)

    override fun toString(): String = "√($radicand)"
  }

  fun square(base: Function<X>) = object: UnaryFunction<X>(base) {
    override fun invoke(map: Map<Var<X>, X>): X = rfc.square(base(map))

    override fun diff(ind: Var<X>) = base * this@RealFunctor.value(rfc.one + rfc.one) * base.diff(ind)

    override fun toString(): String = "($base)²"
  }

  class IndVar<X: Field<X>> constructor(val variable: Var<X>)

  class Differential<X: Field<X>>(val function: Function<X>) {
    operator fun div(arg: IndVar<X>) = function.diff(arg.variable)
  }

  fun <X: Field<X>> d(function: Function<X>) = Differential(function)
  fun <X: Field<X>> d(arg: Var<X>) = IndVar(arg)
}