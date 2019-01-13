package edu.umontreal.kotlingrad.numerical

import ch.obermuhlner.math.big.BigDecimalMath.pow
import edu.umontreal.kotlingrad.algebra.Real
import java.math.BigDecimal

class BigDecimalReal(number: Number = 0): Real<BigDecimalReal> {
  val bg: BigDecimal =
    when {
      number.toDouble().isNaN() -> BigDecimal.ZERO
      1E20 < number.toDouble() -> BigDecimal(1E20)
      -1E20 > number.toDouble() -> BigDecimal(1E20)
      else -> BigDecimal(number.toDouble() + 0.0)
    }

  override fun toString() = bg.toString()

  override fun inverse() = BigDecimalReal(BigDecimal.ONE / bg)

  override fun unaryMinus() = BigDecimalReal(-bg)

  override fun plus(addend: BigDecimalReal) = BigDecimalReal(bg + addend.bg)
  operator fun plus(addend: Number) = BigDecimalReal(bg + BigDecimal(addend.toDouble()))

  override fun minus(subtrahend: BigDecimalReal) = BigDecimalReal(bg - subtrahend.bg)
  operator fun minus(subtrahend: Number) = BigDecimalReal(bg - BigDecimal(subtrahend.toDouble()))

  override fun times(multiplicand: BigDecimalReal) = BigDecimalReal(bg * multiplicand.bg)
  operator fun times(multiplicand: Number) = BigDecimalReal(bg * BigDecimal(multiplicand.toDouble()))

  override fun div(divisor: BigDecimalReal) = BigDecimalReal(bg / divisor.bg)
  operator fun div(divisor: Number) = BigDecimalReal(bg / BigDecimal(divisor.toDouble()))

  //TODO: Fix this
  override fun pow(exponent: BigDecimalReal) = BigDecimalReal(pow(bg, exponent.bg, ProtoBigDecimal.mc))
}