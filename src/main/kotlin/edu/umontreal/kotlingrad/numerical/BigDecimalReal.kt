package edu.umontreal.kotlingrad.numerical

import ch.obermuhlner.math.big.BigDecimalMath.pow
import java.math.BigDecimal

class BigDecimalReal(number: Number = 0): Real<BigDecimalReal, BigDecimal> {
  override val value: BigDecimal =
    when {
      number.toDouble().isNaN() -> BigDecimal.ZERO
      1E20 < number.toDouble() -> BigDecimal(1E20)
      -1E20 > number.toDouble() -> BigDecimal(1E20)
      else -> BigDecimal(number.toDouble() + 0.0)
    }

  override fun toString() = value.toString()

  override fun inverse() = BigDecimalReal(BigDecimal.ONE / value)

  override fun unaryMinus() = BigDecimalReal(-value)

  override fun plus(addend: BigDecimalReal) = BigDecimalReal(value + addend.value)
  operator fun plus(addend: Number) = BigDecimalReal(value + BigDecimal(addend.toDouble()))

  override fun minus(subtrahend: BigDecimalReal) = BigDecimalReal(value - subtrahend.value)
  operator fun minus(subtrahend: Number) = BigDecimalReal(value - BigDecimal(subtrahend.toDouble()))

  override fun times(multiplicand: BigDecimalReal) = BigDecimalReal(value * multiplicand.value)
  operator fun times(multiplicand: Number) = BigDecimalReal(value * BigDecimal(multiplicand.toDouble()))

  override fun div(divisor: BigDecimalReal) = BigDecimalReal(value / divisor.value)
  operator fun div(divisor: Number) = BigDecimalReal(value / BigDecimal(divisor.toDouble()))

  //TODO: Fix this
  override fun pow(exponent: BigDecimalReal) = BigDecimalReal(pow(value, exponent.value, ProtoBigDecimal.mc))
  fun pow(exponent: Int) = BigDecimalReal(value.pow(exponent))
}