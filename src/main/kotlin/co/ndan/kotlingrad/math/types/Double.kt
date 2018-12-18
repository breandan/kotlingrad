package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.Real
import java.lang.Math.pow

class Double(number: Number = 0): Real<Double> {
  val dbl: kotlin.Double = number.toDouble() + 0.0

  override fun toString() = dbl.toString()

  override fun inverse() = Double(1.0 / dbl)

  override fun unaryMinus() = Double(-dbl)

  override fun plus(addend: Double) = Double(dbl + addend.dbl)
  operator fun plus(addend: Number) = Double(dbl + addend.toDouble())

  override fun minus(subtrahend: Double) = Double(dbl - subtrahend.dbl)
  operator fun minus(subtrahend: Number) = Double(dbl - subtrahend.toDouble())

  override fun times(multiplicand: Double) = Double(dbl * multiplicand.dbl)
  operator fun times(multiplicand: Number) = Double(dbl * multiplicand.toDouble())
  override fun times(multiplicand: Long) = Double(dbl * multiplicand)

  override fun div(divisor: Double) = Double(dbl / divisor.dbl)
  operator fun div(divisor: Number) = Double(dbl / divisor.toDouble())

  override fun pow(exponent: Int) = Double(pow(dbl, exponent.toDouble()))
  fun pow(exponent: Number) = Double(pow(dbl, exponent.toDouble()))
}