package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.Real

class Double(val dbl: kotlin.Double = 0.0) : Real<Double> {
  override fun toString() = dbl.toString()

  override fun inverse() = Double(1.0 / dbl)

  override fun unaryMinus() = Double(-dbl)

  override fun plus(addend: Double) = Double(this.dbl + addend.dbl)

  override fun minus(subtrahend: Double) = Double(this.dbl - subtrahend.dbl)

  override fun times(multiplicand: Double) = Double(dbl * multiplicand.dbl)

  override fun div(divisor: Double) = Double(dbl / divisor.dbl)

  operator fun plus(addend: kotlin.Double) = Double(dbl + addend)

  operator fun minus(subtrahend: kotlin.Double) = Double(dbl - subtrahend)

  fun prod(multiplicand: kotlin.Double) = Double(dbl * multiplicand)

  operator fun div(divisor: kotlin.Double) = Double(dbl / divisor)

  fun pow(exponent: kotlin.Double) = Double(Math.pow(dbl, exponent))

  override fun pow(exponent: Int) = Double(Math.pow(dbl, exponent.toDouble()))

  override fun times(multiplicand: Long) = Double(dbl * multiplicand)
}
