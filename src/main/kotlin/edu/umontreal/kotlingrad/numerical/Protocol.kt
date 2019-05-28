package edu.umontreal.kotlingrad.numerical

import edu.umontreal.kotlingrad.functions.ScalarFun
import edu.umontreal.kotlingrad.functions.ScalarVar
import java.math.BigDecimal

object BigDecimalPrecision: Protocol<BigDecimalReal, BigDecimal>() {
  override fun wrap(default: Number) = BigDecimalReal(BigDecimal(default.toDouble()))
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

  abstract fun wrap(default: Number): X

  fun <T: ScalarFun<T>> sin(angle: ScalarFun<T>) = angle.sin()
  fun <T: ScalarFun<T>> cos(angle: ScalarFun<T>) = angle.cos()
  fun <T: ScalarFun<T>> tan(angle: ScalarFun<T>) = angle.tan()
  fun <T: ScalarFun<T>> exp(exponent: ScalarFun<T>) = exponent.exp()
  fun <T: ScalarFun<T>> sqrt(radicand: ScalarFun<T>) = radicand.sqrt()

  class IndVar<X: ScalarFun<X>> constructor(val variable: ScalarVar<X>)

  class Differential<X: ScalarFun<X>>(private val scalarFun: ScalarFun<X>) {
    // TODO: make sure this notation works for arbitrary nested functions using the Chain rule
    infix operator fun div(arg: Differential<X>) = scalarFun.diff(arg.scalarFun.variables.first())
  }

  fun <X: ScalarFun<X>> d(scalarFun: ScalarFun<X>) = Differential(scalarFun)

  infix operator fun ScalarFun<X>.plus(number: Number) = this + wrap(number)
  infix operator fun Number.plus(fn: ScalarFun<X>) = wrap(this) + fn

  infix operator fun ScalarFun<X>.minus(number: Number) = this - wrap(number)
  infix operator fun Number.minus(fn: ScalarFun<X>) = wrap(this) - fn

  infix operator fun ScalarFun<X>.times(number: Number) = this * wrap(number)
  infix operator fun Number.times(fn: ScalarFun<X>) = wrap(this) * fn

  infix operator fun ScalarFun<X>.div(number: Number) = this / wrap(number)
  infix operator fun Number.div(fn: ScalarFun<X>) = wrap(this) / fn

//  fun <Y: `100`> fill(length: Nat<Y>, n: Number) = VectorConst(length, (0 until length.i).map { ScalarConst(wrap(n)) })
//
//  infix operator fun <Y: `100`> VectorFun<ScalarFun<X>, Y>.times(number: Number) = this * fill(length, number)
//  infix operator fun <Y: `100`> Number.times(vector: VectorFun<ScalarFun<X>, Y>) = fill(vector.length, this) * vector
//
//  fun <Y: `100`> fill(length: Nat<Y>, s: ScalarFun<X>) = VectorConst(length, (0 until length.i).map { s })
//  infix operator fun <F: `100`> ScalarFun<X>.plus(addend: VectorFun<ScalarFun<X>, F>) = fill(addend.length, this) + addend
//  infix operator fun <F: `100`> ScalarFun<X>.minus(subtrahend: VectorFun<ScalarFun<X>, F>) = fill(subtrahend.length, this) - subtrahend
//  infix operator fun <F: `100`> ScalarFun<X>.times(multiplicand: VectorFun<ScalarFun<X>, F>) = fill(multiplicand.length, this) * multiplicand

  @JvmName("prefixNumPowFun") fun pow(scalarFun: ScalarFun<X>, number: Number) = scalarFun.run { pow(wrap(number)) }
  @JvmName("prefixFunPowNum") fun pow(number: Number, scalarFun: ScalarFun<X>) = scalarFun.run { wrap(number) }
  @JvmName("infixNumPowFun") fun Number.pow(scalarFun: ScalarFun<X>) = wrap(this).pow(scalarFun)
  @JvmName("infixFunPowNum") infix fun ScalarFun<X>.pow(number: Number) = pow(wrap(number))

  operator fun ScalarFun<X>.invoke(vararg number: Number) = this(variables.zip(number).toMap())
  operator fun ScalarFun<X>.invoke(vararg number: X) = this(variables.zip(number).toMap())
  operator fun ScalarFun<X>.invoke(pairs: Map<ScalarVar<X>, Number>) = this(pairs.map { (it.key to wrap(it.value)) }.toMap())
  operator fun ScalarFun<X>.invoke(vararg pairs: Pair<ScalarVar<X>, Number>) = this(pairs.map { (it.first to wrap(it.second)) }.toMap())
  operator fun Number.invoke(n: Number) = this

//  operator fun <F: `100`> VectorFun<X, F>.invoke(vararg number: Number) = this(variables.zip(number).toMap())
//  operator fun <F: `100`> VectorFun<X, F>.invoke(pairs: Map<ScalarVar<X>, Number>) = this(pairs.map { (it.key to wrap(it.value)) }.toMap()).value
//  operator fun <F: `100`> VectorFun<X, F>.invoke(vararg pairs: Pair<ScalarVar<X>, Number>) = this(pairs.map { (it.first to wrap(it.second)) }.toMap()).value
}