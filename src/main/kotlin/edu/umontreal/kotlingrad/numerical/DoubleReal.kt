package edu.umontreal.kotlingrad.numerical

import edu.umontreal.kotlingrad.algebra.Real
import java.lang.Double.NaN
import java.lang.Math.pow

class DoubleReal(number: Number = 0): Real<DoubleReal> {
  val dbl: Double =
    when (number) {
      NaN -> 1E-100
      else -> number.toDouble().coerceIn(-1E200..1E200)
    }

  override fun toString() = "${if (dbl % 1 == 0.0) dbl.toInt() else dbl}"

  override fun inverse() = DoubleReal(1 / dbl)

  override fun unaryMinus() = DoubleReal(-dbl)

  override fun plus(addend: DoubleReal) = DoubleReal(dbl + addend.dbl)
  operator fun plus(addend: Number) = DoubleReal(dbl + addend.toDouble())

  override fun minus(subtrahend: DoubleReal) = DoubleReal(dbl - subtrahend.dbl)
  operator fun minus(subtrahend: Number) = DoubleReal(dbl - subtrahend.toDouble())

  override fun times(multiplicand: DoubleReal) = DoubleReal(dbl * multiplicand.dbl)
  operator fun times(multiplicand: Number) = DoubleReal(dbl * multiplicand.toDouble())

  override fun div(divisor: DoubleReal) = DoubleReal(dbl / divisor.dbl)
  operator fun div(divisor: Number) = DoubleReal(dbl / divisor.toDouble())

  override fun pow(exponent: DoubleReal) = DoubleReal(pow(dbl, exponent.dbl))
  fun pow(exponent: Number) = DoubleReal(pow(dbl, exponent.toDouble()))
}