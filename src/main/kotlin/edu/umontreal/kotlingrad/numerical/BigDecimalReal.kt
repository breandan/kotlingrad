package edu.umontreal.kotlingrad.numerical

import ch.obermuhlner.math.big.BigDecimalMath.*
import edu.umontreal.kotlingrad.functions.Fun
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.math.BigDecimal.ZERO
import java.math.MathContext
import kotlin.math.E

class BigDecimalReal(number: Number, sigFigs: Int = 30): RealNumber<BigDecimalReal, BigDecimal>(when {
    number is BigDecimal -> number
    number.toDouble().isNaN() -> ZERO
    1E30 < number.toDouble() -> BigDecimal(1E30)
    -1E30 > number.toDouble() -> BigDecimal(1E30)
    else -> BigDecimal(number.toDouble() + 0.0)
  }) {
  override val proto = this
  override val zero by lazy { BigDecimalReal(ZERO) }
  override val one by lazy { BigDecimalReal(ONE) }
  override val two by lazy { BigDecimalReal(ONE + ONE) }
  override val e by lazy { BigDecimalReal(E) }
  val mc = MathContext(sigFigs)

  override fun sin() = BigDecimalReal(sin(value, mc))
  override fun cos() = BigDecimalReal(cos(value, mc))
  override fun tan() = BigDecimalReal(tan(value, mc))
  override fun log() = BigDecimalReal(log(value, mc))
  override fun exp() = BigDecimalReal(exp(value, mc))
  override fun sqrt() = BigDecimalReal(sqrt(value, mc))

  override fun toString() = value.toString()

  override fun inverse() = BigDecimalReal(ONE / value)

  override fun unaryMinus() = BigDecimalReal(-value)

  override fun plus(addend: Fun<BigDecimalReal>) = when (addend) {
    is BigDecimalReal -> BigDecimalReal(value + addend.value)
    else -> super.plus(addend)
  }
  infix operator fun plus(addend: Number) = BigDecimalReal(value + BigDecimal(addend.toDouble()))

  override fun minus(subtrahend: Fun<BigDecimalReal>) = when (subtrahend) {
    is BigDecimalReal -> BigDecimalReal(value - subtrahend.value)
    else -> super.minus(subtrahend)
  }
  infix operator fun minus(subtrahend: Number) = BigDecimalReal(value - BigDecimal(subtrahend.toDouble()))

  override fun times(multiplicand: Fun<BigDecimalReal>) = when (multiplicand) {
    is BigDecimalReal -> BigDecimalReal(value * multiplicand.value)
    else -> super.times(multiplicand)
  }
  infix operator fun times(multiplicand: Number) = BigDecimalReal(value * BigDecimal(multiplicand.toDouble()))

  override fun div(divisor: Fun<BigDecimalReal>) = when(divisor) {
    is BigDecimalReal -> BigDecimalReal(value / divisor.value)
    else -> super.div(divisor)
  }
  infix operator fun div(divisor: Number) = BigDecimalReal(value / BigDecimal(divisor.toDouble()))

  override fun pow(exp: Fun<BigDecimalReal>) = when(exp) {
    is BigDecimalReal -> BigDecimalReal(pow(value, exp.value, mc))
    else -> super.pow(exp)
  }
}