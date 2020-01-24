package edu.umontreal.kotlingrad.experimental

import ch.obermuhlner.math.big.BigDecimalMath
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.E

class BDReal(number: Number, sigFigs: Int = 30): RealNumber<BDReal, BigDecimal>(when {
  number is BigDecimal -> number
  number.toDouble().isNaN() -> BigDecimal.ZERO
  1E30 < number.toDouble() -> BigDecimal(1E30)
  -1E30 > number.toDouble() -> BigDecimal(1E30)
  else -> BigDecimal(number.toDouble() + 0.0)
}) {
  override val ZERO by lazy { BDReal(BigDecimal.ZERO) }
  override val ONE by lazy { BDReal(BigDecimal.ONE) }
  override val TWO by lazy { BDReal(BigDecimal(2)) }
  override val E by lazy { BDReal(BigDecimal(Math.E)) }
  val mc = MathContext(sigFigs)

  override fun sin() = BDReal(BigDecimalMath.sin(value, mc))
  override fun cos() = BDReal(BigDecimalMath.cos(value, mc))
  override fun tan() = BDReal(BigDecimalMath.tan(value, mc))
  override fun ln() = BDReal(BigDecimalMath.log(value, mc))
  override fun sqrt() = BDReal(BigDecimalMath.sqrt(value, mc))

  override fun toString() = value.toString()

  override fun unaryMinus() = BDReal(-value)

  override fun plus(addend: SFun<BDReal>) = when (addend) {
    is BDReal -> BDReal(value + addend.value)
    else -> super.plus(addend)
  }
  infix operator fun plus(addend: Number) =
    BDReal(value + BigDecimal(addend.toDouble()))

  override fun minus(subtrahend: SFun<BDReal>) = when (subtrahend) {
    is BDReal -> BDReal(value - subtrahend.value)
    else -> super.minus(subtrahend)
  }
  
  infix operator fun minus(subtrahend: Number) =
    BDReal(value - BigDecimal(subtrahend.toDouble()))

  override fun times(multiplicand: SFun<BDReal>) = when (multiplicand) {
    is BDReal -> BDReal(value * multiplicand.value)
    else -> super.times(multiplicand)
  }
  infix operator fun times(multiplicand: Number) =
    BDReal(value * BigDecimal(multiplicand.toDouble()))

  override fun div(divisor: SFun<BDReal>) = when(divisor) {
    is BDReal -> BDReal(value / divisor.value)
    else -> super.div(divisor)
  }
  infix operator fun div(divisor: Number) =
    BDReal(value / BigDecimal(divisor.toDouble()))

  override fun pow(exp: SFun<BDReal>) = when(exp) {
    is BDReal -> BDReal(BigDecimalMath.pow(value, exp.value, mc))
    else -> super.pow(exp)
  }
}

