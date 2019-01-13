package edu.umontreal.kotlingrad.numerical

import edu.umontreal.kotlingrad.algebra.FieldPrototype
import java.lang.Math.*

// TODO: Specialize to Ints
object ProtoInteger: FieldPrototype<DoubleReal> {
  override val zero = DoubleReal(0.0)
  override val one = DoubleReal(1.0)

  override fun cos(x: DoubleReal) = DoubleReal(cos(x.dbl))

  override fun sin(x: DoubleReal) = DoubleReal(sin(x.dbl))

  override fun tan(x: DoubleReal) = DoubleReal(tan(x.dbl))

  override fun exp(x: DoubleReal) = DoubleReal(exp(x.dbl))

  override fun log(x: DoubleReal) = DoubleReal(log(x.dbl))

  override fun pow(x: DoubleReal, y: DoubleReal) = DoubleReal(pow(x.dbl, y.dbl))

  override fun sqrt(x: DoubleReal) = DoubleReal(sqrt(x.dbl))
}