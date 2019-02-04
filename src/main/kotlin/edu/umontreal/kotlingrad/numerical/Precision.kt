package edu.umontreal.kotlingrad.numerical

import java.math.BigDecimal

object BigDecimalPrecision: Protocol<BigDecimalReal, BigDecimal>() {
  override fun wrap(default: Number) = BigDecimalReal(BigDecimal(default.toDouble()))
}

object DoublePrecision: Protocol<DoubleReal, Double>() {
  override fun wrap(default: Number) = DoubleReal(default.toDouble())
}