package edu.umontreal.kotlingrad.numerical

import edu.umontreal.kotlingrad.functions.Fun
import edu.umontreal.kotlingrad.functions.ScalarVar
import java.math.BigDecimal

object BigDecimalPrecision: Protocol<BigDecimalReal, BigDecimal>() {
  override fun wrap(default: Number) = BigDecimalReal(BigDecimal(default.toDouble()), 30)
}

object DoublePrecision: Protocol<DoubleReal, Double>() {
  override fun wrap(default: Number) = DoubleReal(default.toDouble())
}

@Suppress("FunctionName")
sealed class Protocol<X: RealNumber<X, Y>, Y> where Y: Number, Y: Comparable<Y> {
  fun Var() = Var(wrap(0))
  fun Var(default: X) = ScalarVar(value = default)
  fun Var(name: String) = ScalarVar(wrap(0), name = name)
  fun Var(name: String, default: X) = ScalarVar(default, name)
  fun Var(default: Number) = Var(wrap(default))
  fun Var(name: String, default: Number) = ScalarVar(wrap(default), name)

  val x = Var("X")
  val y = Var("Y")
  val z = Var("Z")

  abstract fun wrap(default: Number): X

  fun <T: Fun<T>> sin(angle: Fun<T>) = angle.sin()
  fun <T: Fun<T>> cos(angle: Fun<T>) = angle.cos()
  fun <T: Fun<T>> tan(angle: Fun<T>) = angle.tan()
  fun <T: Fun<T>> exp(exponent: Fun<T>) = exponent.exp()
  fun <T: Fun<T>> sqrt(radicand: Fun<T>) = radicand.sqrt()

  class IndVar<X: Fun<X>> constructor(val variable: ScalarVar<X>)

  class Differential<X: Fun<X>>(private val fn: Fun<X>) {
    // TODO: make sure this notation works for arbitrary nested functions using the Chain rule
    infix operator fun div(arg: Differential<X>) = fn.diff(arg.fn.variables.first())
  }

  fun <X: Fun<X>> d(`fun`: Fun<X>) = Differential(`fun`)

  infix operator fun Fun<X>.plus(number: Number) = this + wrap(number)
  infix operator fun Number.plus(fn: Fun<X>) = wrap(this) + fn

  infix operator fun Fun<X>.minus(number: Number) = this - wrap(number)
  infix operator fun Number.minus(fn: Fun<X>) = wrap(this) - fn

  infix operator fun Fun<X>.times(number: Number) = this * wrap(number)
  infix operator fun Number.times(fn: Fun<X>) = wrap(this) * fn

  infix operator fun Fun<X>.div(number: Number) = this / wrap(number)
  infix operator fun Number.div(fn: Fun<X>) = wrap(this) / fn

//  fun <Y: D100> fill(length: Nat<Y>, n: Number) = VectorConst(length, (0 until length.i).map { ScalarConst(wrap(n)) })
//
//  infix operator fun <Y: D100> VectorFun<ScalarFun<X>, Y>.times(number: Number) = this * fill(length, number)
//  infix operator fun <Y: D100> Number.times(vector: VectorFun<ScalarFun<X>, Y>) = fill(vector.length, this) * vector
//
//  fun <Y: D100> fill(length: Nat<Y>, s: ScalarFun<X>) = VectorConst(length, (0 until length.i).map { s })
//  infix operator fun <F: D100> ScalarFun<X>.plus(addend: VectorFun<ScalarFun<X>, F>) = fill(addend.length, this) + addend
//  infix operator fun <F: D100> ScalarFun<X>.minus(subtrahend: VectorFun<ScalarFun<X>, F>) = fill(subtrahend.length, this) - subtrahend
//  infix operator fun <F: D100> ScalarFun<X>.times(multiplicand: VectorFun<ScalarFun<X>, F>) = fill(multiplicand.length, this) * multiplicand

  @JvmName("prefixNumPowFun") fun pow(`fun`: Fun<X>, number: Number) = `fun`.pow(wrap(number))
  @JvmName("prefixFunPowNum") fun pow(number: Number, `fun`: Fun<X>) = wrap(number).pow(`fun`)
  @JvmName("infixNumPowFun") fun Number.pow(`fun`: Fun<X>) = wrap(this).pow(`fun`)
  @JvmName("infixFunPowNum") infix fun Fun<X>.pow(number: Number) = pow(wrap(number))

  operator fun Fun<X>.invoke(vararg number: Number) = this(variables.zip(number).toMap())
  operator fun Fun<X>.invoke(vararg number: X) = this(variables.zip(number).toMap()).proto.value
  operator fun Fun<X>.invoke(vararg subs: Fun<X>) = this(variables.zip(subs).toMap())
  @JvmName("numInvoke") operator fun Fun<X>.invoke(pairs: Map<ScalarVar<X>, Number>) =
    this(pairs.map { (it.key to wrap(it.value)) }.toMap()).proto.value
  @JvmName("numInvoke") operator fun Fun<X>.invoke(vararg pairs: Pair<ScalarVar<X>, Number>) =
    this(pairs.map { (it.first to wrap(it.second)) }.toMap()).proto.value
  @JvmName("subInvoke") operator fun Fun<X>.invoke(vararg pairs: Pair<ScalarVar<X>, Fun<X>>) =
    this(pairs.map { it.first to it.second }.toMap())
  operator fun Number.invoke(n: Number) = this

  fun Fun<X>.eval() = invoke(variables.map { Pair(it, it.value) }.toMap()).proto.value

//  operator fun <F: D100> VectorFun<X, F>.invoke(vararg number: Number) = this(variables.zip(number).toMap())
//  operator fun <F: D100> VectorFun<X, F>.invoke(pairs: Map<ScalarVar<X>, Number>) = this(pairs.map { (it.key to wrap(it.value)) }.toMap()).value
//  operator fun <F: D100> VectorFun<X, F>.invoke(vararg pairs: Pair<ScalarVar<X>, Number>) = this(pairs.map { (it.first to wrap(it.second)) }.toMap()).value
}