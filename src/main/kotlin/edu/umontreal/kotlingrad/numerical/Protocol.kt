package edu.umontreal.kotlingrad.numerical

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.Fun
import edu.umontreal.kotlingrad.functions.Fun.Var

abstract class Protocol<X: RealNumber<X, Y>, Y> where Y: Number, Y: Comparable<Y> {
  fun Var() = Var(wrap(0))
  fun Var(default: X) = Var(value = default)
  fun Var(name: String) = Var(wrap(0), name = name)
  fun Var(name: String, default: X) = Var(default, name)
  fun Var(default: Number) = Var(wrap(default))
  fun Var(name: String, default: Number) = Var(wrap(default), name)

  abstract fun wrap(default: Number): X

  fun <T: Field<T>> sin(angle: Fun<T>) = angle.sin()
  fun <T: Field<T>> cos(angle: Fun<T>) = angle.cos()
  fun <T: Field<T>> tan(angle: Fun<T>) = angle.tan()
  fun <T: Field<T>> exp(exponent: Fun<T>) = exponent.exp()
  fun <T: Field<T>> sqrt(radicand: Fun<T>) = radicand.sqrt()

  class IndVar<X: Field<X>> constructor(val variable: Var<X>)

  class Differential<X: Field<X>>(private val `fun`: Fun<X>) {
    // TODO: make sure this notation works for arbitrary nested functions using the Chain rule
    infix operator fun div(arg: Differential<X>) = `fun`.diff(arg.`fun`.variables.first())
  }

  fun <X: Field<X>> d(`fun`: Fun<X>) = Differential(`fun`)

  infix operator fun Fun<X>.plus(number: Number) = this + const(wrap(number))
  infix operator fun Number.plus(fn: Fun<X>) = fn.const(wrap(this)) + fn

  infix operator fun Fun<X>.minus(number: Number) = this - const(wrap(number))
  infix operator fun Number.minus(fn: Fun<X>) = fn.const(wrap(this)) - fn

  infix operator fun Fun<X>.times(number: Number) = this * const(wrap(number))
  infix operator fun Number.times(fn: Fun<X>) = fn.const(wrap(this)) * fn

  infix operator fun Fun<X>.div(number: Number) = this / const(wrap(number))
  infix operator fun Number.div(fn: Fun<X>) = fn.const(wrap(this)) / fn

  @JvmName("prefixNumPowFun") fun pow(`fun`: Fun<X>, number: Number) = `fun`.run { pow(const(wrap(number))) }
  @JvmName("prefixFunPowNum") fun pow(number: Number, `fun`: Fun<X>) = `fun`.run { const(wrap(number)).pow(this) }
  @JvmName("infixNumPowFun") fun Number.pow(`fun`: Fun<X>) = `fun`.const(wrap(this)).pow(`fun`)
  @JvmName("infixFunPowNum") infix fun Fun<X>.pow(number: Number) = pow(const(wrap(number)))

  operator fun Fun<X>.invoke(vararg number: Number) = this(variables.zip(number).toMap())
  operator fun Fun<X>.invoke(pairs: Map<Var<X>, Number>) = this(pairs.map { (it.key to wrap(it.value)) }.toMap()).value
  operator fun Fun<X>.invoke(vararg pairs: Pair<Var<X>, Number>) = this(pairs.map { (it.first to wrap(it.second)) }.toMap()).value
  operator fun Number.invoke(n: Number) = this
}