package edu.umontreal.kotlingrad.numerical

import edu.umontreal.kotlingrad.algebra.Real
import java.math.BigDecimal

class BigDecimalReal(number: Number = 0): Real<BigDecimalReal> {
  val dbl: BigDecimal =
      when {
        number.toDouble().isNaN() -> BigDecimal.ZERO
        1E20 < number.toDouble() -> BigDecimal(1E20)
        -1E20 > number.toDouble() -> BigDecimal(1E20)
        else -> BigDecimal(number.toDouble() + 0.0)
      }

  override fun toString() = dbl.toString()

  override fun inverse() = BigDecimalReal(BigDecimal.ONE / dbl)

  override fun unaryMinus() = BigDecimalReal(-dbl)

  override fun plus(addend: BigDecimalReal) = BigDecimalReal(dbl + addend.dbl)
  operator fun plus(addend: Number) = BigDecimalReal(dbl + BigDecimal(addend.toDouble()))

  override fun minus(subtrahend: BigDecimalReal) = BigDecimalReal(dbl - subtrahend.dbl)
  operator fun minus(subtrahend: Number) = BigDecimalReal(dbl - BigDecimal(subtrahend.toDouble()))

  override fun times(multiplicand: BigDecimalReal) = BigDecimalReal(dbl * multiplicand.dbl)
  operator fun times(multiplicand: Number) = BigDecimalReal(dbl * BigDecimal(multiplicand.toDouble()))
  override fun times(multiplicand: Long) = BigDecimalReal(dbl * BigDecimal(multiplicand))

  override fun div(divisor: BigDecimalReal) = BigDecimalReal(dbl / divisor.dbl)
  operator fun div(divisor: Number) = BigDecimalReal(dbl / BigDecimal(divisor.toDouble()))

  override fun pow(exponent: Int) = BigDecimalReal(dbl.pow(exponent))
}