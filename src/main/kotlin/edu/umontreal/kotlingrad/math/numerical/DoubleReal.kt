package edu.umontreal.kotlingrad.math.numerical

import edu.umontreal.kotlingrad.math.algebra.Real
import java.lang.Math.pow

class DoubleReal(number: Number = 0): Real<DoubleReal> {
  val dbl: kotlin.Double = number.toDouble() + 0.0

  override fun toString() = dbl.toString()

  override fun inverse() = DoubleReal(1.0 / dbl)

  override fun unaryMinus() = DoubleReal(-dbl)

  override fun plus(addend: DoubleReal) = DoubleReal(dbl + addend.dbl)
  operator fun plus(addend: Number) = DoubleReal(dbl + addend.toDouble())

  override fun minus(subtrahend: DoubleReal) = DoubleReal(dbl - subtrahend.dbl)
  operator fun minus(subtrahend: Number) = DoubleReal(dbl - subtrahend.toDouble())

  override fun times(multiplicand: DoubleReal) = DoubleReal(dbl * multiplicand.dbl)
  operator fun times(multiplicand: Number) = DoubleReal(dbl * multiplicand.toDouble())
  override fun times(multiplicand: Long) = DoubleReal(dbl * multiplicand)

  override fun div(divisor: DoubleReal) = DoubleReal(dbl / divisor.dbl)
  operator fun div(divisor: Number) = DoubleReal(dbl / divisor.toDouble())

  override fun pow(exponent: Int) = DoubleReal(pow(dbl, exponent.toDouble()))
  fun pow(exponent: Number) = DoubleReal(pow(dbl, exponent.toDouble()))
}