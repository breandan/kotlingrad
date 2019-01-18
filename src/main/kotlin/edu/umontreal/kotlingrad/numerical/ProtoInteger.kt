package edu.umontreal.kotlingrad.numerical

import java.lang.Math.*

// TODO: Specialize to Ints
object ProtoInteger: FieldPrototype<DoubleReal> {
  override fun invoke(number: Number) = DoubleReal(number)

  override val zero = DoubleReal(0.0)
  override val one = DoubleReal(1.0)
  override val e = DoubleReal(E)

  override fun cos(x: DoubleReal) = DoubleReal(cos(x.value))

  override fun sin(x: DoubleReal) = DoubleReal(sin(x.value))

  override fun tan(x: DoubleReal) = DoubleReal(tan(x.value))

  override fun exp(x: DoubleReal) = DoubleReal(exp(x.value))

  override fun log(x: DoubleReal) = DoubleReal(log(x.value))

  override fun pow(x: DoubleReal, y: DoubleReal) = DoubleReal(pow(x.value, y.value))

  override fun sqrt(x: DoubleReal) = DoubleReal(sqrt(x.value))
}