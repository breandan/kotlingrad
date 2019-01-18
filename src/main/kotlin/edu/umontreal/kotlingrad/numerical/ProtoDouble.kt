package edu.umontreal.kotlingrad.numerical

import kotlin.math.*

object ProtoDouble: FieldPrototype<DoubleReal> {
  override val zero = DoubleReal(0.0)
  override val one = DoubleReal(1.0)
  override val e = DoubleReal(E)

  override fun invoke(number: Number) = DoubleReal(number)

  override fun cos(x: DoubleReal) = DoubleReal(cos(x.value))

  override fun sin(x: DoubleReal) = DoubleReal(sin(x.value))

  override fun tan(x: DoubleReal) = DoubleReal(tan(x.value))

  override fun exp(x: DoubleReal) = DoubleReal(exp(x.value))

  override fun log(x: DoubleReal) = DoubleReal(ln(x.value))

  override fun sqrt(x: DoubleReal) = DoubleReal(sqrt(x.value))

  override fun pow(x: DoubleReal, y: DoubleReal) = DoubleReal(x.value.pow(y.value))
}