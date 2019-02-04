package edu.umontreal.kotlingrad.numerical

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.calculus.FieldFunctor
import edu.umontreal.kotlingrad.functions.Function
import edu.umontreal.kotlingrad.functions.Function.*

abstract class Protocol<X: RealNumber<X, Y>, Y>: FieldFunctor<X>() where Y: Number, Y: Comparable<Y> {
  fun variable() = Var(wrap(0))
  fun variable(default: X) = Var(value = default)
  fun variable(name: String) = Var(wrap(0), name = name)
  fun variable(name: String, default: X) = Var(default, name)
  fun variable(default: Number) = Var(wrap(default))
  fun variable(name: String, default: Number) = Var(wrap(default), name)

  abstract fun wrap(default: Number): X

  fun <T: Field<T>> sin(angle: Function<T>) = angle.sin()
  fun <T: Field<T>> cos(angle: Function<T>) = angle.cos()
  fun <T: Field<T>> tan(angle: Function<T>) = angle.tan()
  fun <T: Field<T>> exp(exponent: Function<T>) = exponent.exp()
  fun <T: Field<T>> sqrt(radicand: Function<T>) = radicand.sqrt()

  class IndVar<X: Field<X>> constructor(val variable: Var<X>)

  class Differential<X: Field<X>>(private val function: Function<X>) {
    // TODO: make sure this notation works for arbitrary nested functions using the Chain rule
    infix operator fun div(arg: Differential<X>) = function.diff(arg.function.variables.first())
  }

  fun <X: Field<X>> d(function: Function<X>) = Differential(function)

  infix operator fun Function<X>.plus(number: Number) = this + const(wrap(number))
  infix operator fun Number.plus(fn: Function<X>) = fn.const(wrap(this)) + fn

  infix operator fun Function<X>.minus(number: Number) = this - const(wrap(number))
  infix operator fun Number.minus(fn: Function<X>) = fn.const(wrap(this)) - fn

  infix operator fun Function<X>.times(number: Number) = this * const(wrap(number))
  infix operator fun Number.times(fn: Function<X>) = fn.const(wrap(this)) * fn

  infix operator fun Function<X>.div(number: Number) = this / const(wrap(number))
  infix operator fun Number.div(fn: Function<X>) = fn.const(wrap(this)) / fn

  @JvmName("prefixNumPowFun") fun pow(function: Function<X>, number: Number) = function.run { pow(const(wrap(number))) }
  @JvmName("prefixFunPowNum") fun pow(number: Number, function: Function<X>) = function.run { const(wrap(number)).pow(this) }
  @JvmName("infixNumPowFun") fun Number.pow(function: Function<X>) = function.const(wrap(this)).pow(function)
  @JvmName("infixFunPowNum") infix fun Function<X>.pow(number: Number) = pow(const(wrap(number)))

  operator fun Function<X>.invoke(vararg number: Number) = this(variables.zip(number).toMap())
  operator fun Function<X>.invoke(pairs: Map<Var<X>, Number>) = this(pairs.map { (it.key to wrap(it.value)) }.toMap()).value
  operator fun Function<X>.invoke(vararg pairs: Pair<Var<X>, Number>) = this(pairs.map { (it.first to wrap(it.second)) }.toMap()).value
  operator fun Number.invoke(n: Number) = this
}