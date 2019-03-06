package edu.umontreal.kotlingrad.numerical

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.ScalarFun
import edu.umontreal.kotlingrad.functions.ScalarVar
import edu.umontreal.kotlingrad.functions.VectorFun
import java.math.BigDecimal
import edu.umontreal.kotlingrad.dependent.*
import edu.umontreal.kotlingrad.functions.ScalarConst

object BigDecimalPrecision: Protocol<BigDecimalReal, BigDecimal>() {
  override fun wrap(default: Number) = BigDecimalReal(BigDecimal(default.toDouble()))
}

object DoublePrecision: Protocol<DoubleReal, Double>() {
  override fun wrap(default: Number) = DoubleReal(default.toDouble())
}

sealed class Protocol<X: RealNumber<X, Y>, Y> where Y: Number, Y: Comparable<Y> {
  fun Var() = Var(wrap(0))
  fun Var(default: X) = ScalarVar(value = default)
  fun Var(name: String) = ScalarVar(wrap(0), name = name)
  fun Var(name: String, default: X) = ScalarVar(default, name)
  fun Var(default: Number) = Var(wrap(default))
  fun Var(name: String, default: Number) = ScalarVar(wrap(default), name)

  abstract fun wrap(default: Number): X

  fun <T: Field<T>> sin(angle: ScalarFun<T>) = angle.sin()
  fun <T: Field<T>> cos(angle: ScalarFun<T>) = angle.cos()
  fun <T: Field<T>> tan(angle: ScalarFun<T>) = angle.tan()
  fun <T: Field<T>> exp(exponent: ScalarFun<T>) = exponent.exp()
  fun <T: Field<T>> sqrt(radicand: ScalarFun<T>) = radicand.sqrt()

  class IndVar<X: Field<X>> constructor(val variable: ScalarVar<X>)

  class Differential<X: Field<X>>(private val scalarFun: ScalarFun<X>) {
    // TODO: make sure this notation works for arbitrary nested functions using the Chain rule
    infix operator fun div(arg: Differential<X>) = scalarFun.diff(arg.scalarFun.variables.first())
  }

  fun <X: Field<X>> d(scalarFun: ScalarFun<X>) = Differential(scalarFun)

  infix operator fun ScalarFun<X>.plus(number: Number) = this + wrap(number)
  infix operator fun Number.plus(fn: ScalarFun<X>) = fn.const(wrap(this)) + fn

  infix operator fun ScalarFun<X>.minus(number: Number) = this - wrap(number)
  infix operator fun Number.minus(fn: ScalarFun<X>) = fn.const(wrap(this)) - fn

  infix operator fun ScalarFun<X>.times(number: Number) = this * wrap(number)
  infix operator fun Number.times(fn: ScalarFun<X>) = fn.const(wrap(this)) * fn

  infix operator fun ScalarFun<X>.div(number: Number) = this / wrap(number)
  infix operator fun Number.div(fn: ScalarFun<X>) = fn.const(wrap(this)) / fn

  infix operator fun <Y: `100`> VectorFun<ScalarFun<X>, Y>.times(number: Number) = this * ScalarConst(wrap(number))
  infix operator fun <Y: `100`> Number.times(vector: VectorFun<ScalarFun<X>, Y>): VectorFun<ScalarFun<X>, Y> = vector * ScalarConst(wrap(this))

  @JvmName("prefixNumPowFun") fun pow(scalarFun: ScalarFun<X>, number: Number) = scalarFun.run { pow(const(wrap(number))) }
  @JvmName("prefixFunPowNum") fun pow(number: Number, scalarFun: ScalarFun<X>) = scalarFun.run { const(wrap(number)).pow(this) }
  @JvmName("infixNumPowFun") fun Number.pow(scalarFun: ScalarFun<X>) = scalarFun.const(wrap(this)).pow(scalarFun)
  @JvmName("infixFunPowNum") infix fun ScalarFun<X>.pow(number: Number) = pow(wrap(number))

  operator fun ScalarFun<X>.invoke(vararg number: Number) = this(variables.zip(number).toMap())
  operator fun ScalarFun<X>.invoke(pairs: Map<ScalarVar<X>, Number>) = this(pairs.map { (it.key to wrap(it.value)) }.toMap()).value
  operator fun ScalarFun<X>.invoke(vararg pairs: Pair<ScalarVar<X>, Number>) = this(pairs.map { (it.first to wrap(it.second)) }.toMap()).value
  operator fun Number.invoke(n: Number) = this
}