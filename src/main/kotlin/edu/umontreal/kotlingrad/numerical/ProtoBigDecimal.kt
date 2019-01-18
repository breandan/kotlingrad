package edu.umontreal.kotlingrad.numerical

import ch.obermuhlner.math.big.BigDecimalMath.*
import java.math.MathContext
import kotlin.math.E

object ProtoBigDecimal: FieldPrototype<BigDecimalReal> {
  override fun invoke(number: Number) = BigDecimalReal(number)

  override val zero = BigDecimalReal(0.0)
  override val one = BigDecimalReal(1.0)
  override val e = BigDecimalReal(E)
  val mc = MathContext(10)

  override fun cos(x: BigDecimalReal) = BigDecimalReal(cos(x.value, mc))

  override fun sin(x: BigDecimalReal) = BigDecimalReal(sin(x.value, mc))

  override fun tan(x: BigDecimalReal) = BigDecimalReal(tan(x.value, mc))

  override fun exp(x: BigDecimalReal) = BigDecimalReal(exp(x.value, mc))

  override fun log(x: BigDecimalReal) = BigDecimalReal(log(x.value, mc))

  override fun pow(x: BigDecimalReal, y: BigDecimalReal) = BigDecimalReal(pow(x.value, y.value, mc))

  override fun sqrt(x: BigDecimalReal) = BigDecimalReal(sqrt(x.value, mc))
}