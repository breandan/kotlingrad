package edu.umontreal.kotlingrad.math.calculus

import edu.umontreal.kotlingrad.math.algebra.Field
import edu.umontreal.kotlingrad.math.algebra.Real
import edu.umontreal.kotlingrad.math.algebra.RealPrototype
import edu.umontreal.kotlingrad.math.functions.BinaryFunction
import edu.umontreal.kotlingrad.math.functions.Function
import edu.umontreal.kotlingrad.math.functions.UnaryFunction
import edu.umontreal.kotlingrad.math.functions.VectorFunction
import edu.umontreal.kotlingrad.math.operators.Inverse
import edu.umontreal.kotlingrad.math.types.*

abstract class RealFunctor<X: Real<X>>(val rfc: RealPrototype<X>): FieldFunctor<X>() {
  fun value(fnx: X) = Const(fnx, rfc)

  fun value(vararg fnx: X) = ConstVector(*fnx.mapTo(ArrayList(fnx.size)) { value(it) }.toTypedArray())

  fun zero(size: Int) = ConstVector(*Array(size) { zero })

  open fun variable() = Var(rfc)

  open fun variable(default: X) = Var(rfc, value = default)

  open fun variable(name: String) = Var(rfc, name = name)

  open fun variable(name: String, default: X) = Var(rfc, default, name)

  fun function(vararg fns: Function<X>) = VectorFunction(*fns)

  val zero = Zero(rfc)

  val one = One(rfc)

  fun cos(angle: Function<X>) = object: UnaryFunction<X>(angle) {
    override fun invoke(map: Map<Var<X>, X>): X = rfc.cos(arg(map))

    override fun diff(ind: Var<X>) = -(sin(arg) * arg.diff(ind))

    override fun toString() = "cos($arg)"
  }

  fun sin(angle: Function<X>): Function<X> = object: UnaryFunction<X>(angle) {
    override fun invoke(map: Map<Var<X>, X>): X = rfc.sin(arg(map))

    override fun diff(ind: Var<X>) = cos(arg) * arg.diff(ind)

    override fun toString(): String = "sin($arg)"
  }

  fun tan(angle: Function<X>): Function<X> = object: UnaryFunction<X>(angle) {
    override fun invoke(map: Map<Var<X>, X>): X = rfc.tan(arg(map))

    override fun diff(ind: Var<X>) = UnivariatePolynomialTerm(1, cos(arg), -2) * arg.diff(ind)

    override fun toString(): String = "tan($arg)"
  }

  fun exp(exponent: Function<X>): Function<X> = object: UnaryFunction<X>(exponent) {
    override fun invoke(map: Map<Var<X>, X>): X = rfc.exp(arg(map))

    override fun diff(ind: Var<X>) = exp(arg) * arg.diff(ind)

    override fun toString(): String = "exp($arg)"
  }

  fun log(logarithmand: Function<X>) = object: UnaryFunction<X>(logarithmand) {
    override fun invoke(map: Map<Var<X>, X>): X = rfc.log(arg(map))

    override fun diff(ind: Var<X>) = Inverse(arg) * arg.diff(ind)

    override fun toString(): String = "log₁₀($arg)"
  }

  fun pow(base: Function<X>, exponent: Const<X>): BinaryFunction<X> = object: BinaryFunction<X>(base, exponent) {
    override fun invoke(map: Map<Var<X>, X>): X = rfc.pow(lfn(map), rfn(map))

    override fun diff(ind: Var<X>) = rfn * this@RealFunctor.pow(lfn, this@RealFunctor.value(rfn() - rfc.one)) * lfn.diff(ind)

    override fun toString(): String = "pow($lfn, $rfn)"
  }

  fun sqrt(radicand: Function<X>): Function<X> = object: UnaryFunction<X>(radicand) {
    override fun invoke(map: Map<Var<X>, X>): X = rfc.sqrt(arg(map))

    override fun diff(ind: Var<X>) = sqrt(arg).inverse() / this@RealFunctor.value(rfc.one * 2L) * arg.diff(ind)

    override fun toString(): String = "√($arg)"
  }

  fun square(base: Function<X>) = object: UnaryFunction<X>(base) {
    override fun invoke(map: Map<Var<X>, X>): X = rfc.square(arg(map))

    override fun diff(ind: Var<X>) = arg * this@RealFunctor.value(rfc.one * 2L) * arg.diff(ind)

    override fun toString(): String = "($arg)²"
  }

  class IndVar<X: Field<X>> constructor(val variable: Var<X>)

  class Differential<X: Field<X>>(val function: Function<X>) {
    operator fun div(arg: IndVar<X>) = function.diff(arg.variable)
  }

  fun <X: Field<X>> d(function: Function<X>) = Differential(function)
  fun <X: Field<X>> d(arg: Var<X>) = IndVar(arg)
}