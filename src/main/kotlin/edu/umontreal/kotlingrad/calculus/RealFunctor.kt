package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.Function
import edu.umontreal.kotlingrad.functions.Function.*
import edu.umontreal.kotlingrad.numerical.FieldPrototype
import edu.umontreal.kotlingrad.numerical.RealNumber

abstract class RealFunctor<X: RealNumber<X, Y>, Y>(val rfc: FieldPrototype<X>): FieldFunctor<X>() where Y: Number, Y: Comparable<Y> {
  fun variable() = Var(rfc)

  fun variable(default: X) = Var(rfc, value = default)

  fun variable(name: String) = Var(rfc, name = name)

  fun variable(name: String, default: X) = Var(rfc, default, name)

  fun <T: Field<T>> sin(angle: Function<T>) = Sine(angle)
  fun <T: Field<T>> cos(angle: Function<T>) = Cosine(angle)
  fun <T: Field<T>> tan(angle: Function<T>) = Tangent(angle)
  fun <T: Field<T>> exp(exponent: Function<T>) = Exp(exponent)
  fun <T: Field<T>> sqrt(radicand: Function<T>) = SquareRoot(radicand)

  private infix operator fun Function<X>.plus(addend: X) = this + Const(addend)
  private infix operator fun Function<X>.minus(subtrahend: X) = this - Const(subtrahend)
  private infix operator fun Function<X>.times(multiplicand: X) = this * Const(multiplicand)
  private infix operator fun Function<X>.div(divisor: X) = this / Const(divisor)

  class IndVar<X: Field<X>> constructor(val variable: Var<X>)

  class Differential<X: Field<X>>(val function: Function<X>) {
    // TODO: make sure this notation works for arbitrary nested functions using the Chain rule
    infix operator fun div(arg: Differential<X>) = function.diff(arg.function.variables.first())
  }

  fun <X: Field<X>> d(function: Function<X>) = Differential(function)

  infix operator fun Function<X>.plus(number: Number) = this + rfc(number)
  infix operator fun Number.plus(fn: Function<X>) = fn + rfc(this)

  infix operator fun Function<X>.minus(number: Number) = this - rfc(number)
  infix operator fun Number.minus(fn: Function<X>) = fn - rfc(this)

  infix operator fun Function<X>.times(number: Number) = this * rfc(number)
  infix operator fun Number.times(fn: Function<X>) = fn * rfc(this)

  infix operator fun Function<X>.div(number: Number) = this / rfc(number)
  infix operator fun Number.div(fn: Function<X>) = fn.inverse() * rfc(this)

  // TODO Make this an extension function?
  fun pow(function: Function<X>, number: Number) = function.pow(Const(rfc(number)))
//  fun Function<DoubleReal>.pow(number: Number) = pow(Const(DoubleReal(number)))

  fun variable(default: Number) = Var(rfc, rfc(default))

  fun variable(name: String, default: Number) = Var(rfc, rfc(default), name)

  infix operator fun Function<X>.invoke(pairs: Map<Var<X>, Number>) =
    this(pairs.map { (it.key to rfc(it.value)) }.toMap()).value

  operator fun Function<X>.invoke(vararg pairs: Pair<Var<X>, Number>) =
    this(pairs.map { (it.first to rfc(it.second)) }.toMap()).value

  operator fun Function<X>.invoke(vararg number: Number) =
    this(variables.zip(number).toMap())

  operator fun Number.invoke(d: Double) = this
}