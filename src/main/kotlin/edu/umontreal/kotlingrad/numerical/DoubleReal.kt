package edu.umontreal.kotlingrad.numerical

import edu.umontreal.kotlingrad.functions.ScalarFun
import java.lang.Double.NaN
import kotlin.math.E
import kotlin.math.*

class DoubleReal(number: Number = 0.0): RealNumber<DoubleReal, Double>(when (number) {
  is Double -> number
  NaN -> 1E-100
  else -> number.toDouble().coerceIn(-1E200..1E200)
}) {
  override val proto = this
  override val zero by lazy { DoubleReal(0.0) }
  override val one by lazy { DoubleReal(1.0) }
  override val two by lazy { DoubleReal(2.0) }
  override val e by lazy { DoubleReal(E) }

  override fun sin() = DoubleReal(sin(value))
  override fun cos() = DoubleReal(cos(value))
  override fun tan() = DoubleReal(tan(value))
  override fun log() = DoubleReal(ln(value))
  override fun exp() = DoubleReal(exp(value))
  override fun sqrt() = DoubleReal(sqrt(value))

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
    else -> super.minus(subtrahend)
  }
  infix operator fun minus(subtrahend: Number) = DoubleReal(value - subtrahend.toDouble())

  override fun times(multiplicand: ScalarFun<DoubleReal>) = when (multiplicand) {
    is DoubleReal -> DoubleReal(value * multiplicand.value)
    else -> super.times(multiplicand)
  }
  infix operator fun times(multiplicand: Number) = DoubleReal(value * multiplicand.toDouble())

  override fun div(divisor: ScalarFun<DoubleReal>) = when(divisor) {
    is DoubleReal -> DoubleReal(value / divisor.value)
    else -> super.div(divisor)
  }
  infix operator fun div(divisor: Number) = DoubleReal(value / divisor.toDouble())

  override fun pow(exp: ScalarFun<DoubleReal>) = when(exp) {
    is DoubleReal -> DoubleReal(value.pow(exp.value))
    else -> super.pow(exp)
  }
  infix fun pow(exponent: Number) = DoubleReal(value.pow(exponent.toDouble()))
}