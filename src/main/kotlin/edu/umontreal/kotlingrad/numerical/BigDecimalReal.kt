package edu.umontreal.kotlingrad.numerical

import ch.obermuhlner.math.big.BigDecimalMath.*
import java.lang.Math.E
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.math.BigDecimal.ZERO
import java.math.MathContext

class BigDecimalReal(number: Number = ZERO):
  RealNumber<BigDecimalReal, BigDecimal>(when {
    number is BigDecimal -> number
    number.toDouble().isNaN() -> ZERO
    1E20 < number.toDouble() -> BigDecimal(1E20)
    -1E20 > number.toDouble() -> BigDecimal(1E20)
    else -> BigDecimal(number.toDouble() + 0.0)
  }) {
  override val zero by lazy { BigDecimalReal(0.0) }
  override val one by lazy { BigDecimalReal(1.0) }
  override val e by lazy { BigDecimalReal(E) }
  val mc = MathContext(10)

  override fun sin() = BigDecimalReal(sin(value, mc))
  override fun cos() = BigDecimalReal(cos(value, mc))
  override fun tan() = BigDecimalReal(tan(value, mc))
  override fun log() = BigDecimalReal(log(value, mc))
  override fun exp() = BigDecimalReal(exp(value, mc))
  override fun sqrt() = BigDecimalReal(sqrt(value, mc))

  override fun toString() = value.toString()

  override fun inverse() = BigDecimalReal(ONE / value)

  override fun unaryMinus() = BigDecimalReal(-value)

  override fun plus(addend: BigDecimalReal) = BigDecimalReal(value + addend.value)
  infix operator fun plus(addend: Number) = BigDecimalReal(value + BigDecimal(addend.toDouble()))

  override fun minus(subtrahend: BigDecimalReal) = BigDecimalReal(value - subtrahend.value)
  infix operator fun minus(subtrahend: Number) = BigDecimalReal(value - BigDecimal(subtrahend.toDouble()))

  override fun times(multiplicand: BigDecimalReal) = BigDecimalReal(value * multiplicand.value)
  infix operator fun times(multiplicand: Number) = BigDecimalReal(value * BigDecimal(multiplicand.toDouble()))

  override fun div(divisor: BigDecimalReal) = BigDecimalReal(value / divisor.value)
  infix operator fun div(divisor: Number) = BigDecimalReal(value / BigDecimal(divisor.toDouble()))

  //TODO: Fix this
  override fun pow(exp: BigDecimalReal) = BigDecimalReal(pow(value, exp.value, mc))
}