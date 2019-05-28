package edu.umontreal.kotlingrad.numerical

import edu.umontreal.kotlingrad.functions.ScalarFun
import java.lang.Double.NaN
import java.lang.Math.E
import java.lang.Math.pow

class DoubleReal(number: Number = 0.0): RealNumber<DoubleReal, Double>(when (number) {
  is Double -> number
  NaN -> 1E-100
  else -> number.toDouble().coerceIn(-1E200..1E200)
}) {
  override fun compareTo(other: Double) = value.compareTo(other)

  override val one by lazy { DoubleReal(1.0) }
  override val zero by lazy { DoubleReal(0.0) }
  override val e by lazy { DoubleReal(E) }

  override fun sin() = DoubleReal(Math.sin(value))
  override fun cos() = DoubleReal(Math.cos(value))
  override fun tan() = DoubleReal(Math.tan(value))
  override fun log() = DoubleReal(Math.log(value))
  override fun exp() = DoubleReal(Math.exp(value))
  override fun sqrt() = DoubleReal(Math.sqrt(value))

  override fun toString() = "${if (value % 1 == 0.0) value.toInt() else value}"

  override fun inverse() = DoubleReal(1 / value)

  override fun unaryMinus() = DoubleReal(-value)

  override fun plus(addend: ScalarFun<DoubleReal>) = when (addend) {
    is DoubleReal -> DoubleReal(value + addend.value)
    else -> super.plus(addend)
  }
  infix operator fun plus(addend: Number) = DoubleReal(value + addend.toDouble())

  override fun minus(subtrahend: ScalarFun<DoubleReal>) = when (subtrahend) {
    is DoubleReal -> DoubleReal(value - subtrahend.value)
    else -> super.plus(subtrahend)
  }
  infix operator fun minus(subtrahend: Number) = DoubleReal(value - subtrahend.toDouble())

  override fun times(multiplicand: ScalarFun<DoubleReal>) = when (multiplicand) {
    is DoubleReal -> DoubleReal(value * multiplicand.value)
    else -> super.plus(multiplicand)
  }
  infix operator fun times(multiplicand: Number) = DoubleReal(value * multiplicand.toDouble())

  override fun div(divisor: ScalarFun<DoubleReal>) = when(divisor) {
    is DoubleReal -> DoubleReal(value / divisor.value)
    else -> super.div(divisor)
  }
  infix operator fun div(divisor: Number) = DoubleReal(value / divisor.toDouble())

  override fun pow(exp: ScalarFun<DoubleReal>) = when(exp) {
    is DoubleReal -> DoubleReal(pow(value, exp.value))
    else -> super.div(exp)
  }
  fun pow(exponent: Number) = DoubleReal(pow(value, exponent.toDouble()))
}