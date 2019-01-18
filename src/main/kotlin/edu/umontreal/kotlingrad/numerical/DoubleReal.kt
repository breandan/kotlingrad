package edu.umontreal.kotlingrad.numerical

import java.lang.Double.NaN
import java.lang.Math.pow

class DoubleReal(number: Number = 0): RealNumber<DoubleReal, Double>(when (number) {
  is Double -> number
  NaN -> 1E-100
  else -> number.toDouble().coerceIn(-1E200..1E200)
}), Comparable<Double> {
  override fun toString() = "${if (value % 1 == 0.0) value.toInt() else value}"

  override fun inverse() = DoubleReal(1 / value)

  override fun unaryMinus() = DoubleReal(-value)

  override fun plus(addend: DoubleReal) = DoubleReal(value + addend.value)
  operator fun plus(addend: Number) = DoubleReal(value + addend.toDouble())

  override fun minus(subtrahend: DoubleReal) = DoubleReal(value - subtrahend.value)
  operator fun minus(subtrahend: Number) = DoubleReal(value - subtrahend.toDouble())

  override fun times(multiplicand: DoubleReal) = DoubleReal(value * multiplicand.value)
  operator fun times(multiplicand: Number) = DoubleReal(value * multiplicand.toDouble())

  override fun div(divisor: DoubleReal) = DoubleReal(value / divisor.value)
  operator fun div(divisor: Number) = DoubleReal(value / divisor.toDouble())

  override fun pow(exp: DoubleReal) = DoubleReal(pow(value, exp.value))
  fun pow(exponent: Number) = DoubleReal(pow(value, exponent.toDouble()))
}