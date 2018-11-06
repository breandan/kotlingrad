package co.ndan.kotlingrad.math.algebra

import co.ndan.kotlingrad.math.types.Double

object DoublePrototype : RealPrototype<Double> {
  override val zero = Double(0.0)
  override val one = Double(1.0)

  override fun cos(x: Double) = Double(Math.cos(x.dbl))

  override fun sin(x: Double) = Double(Math.sin(x.dbl))

  override fun tan(x: Double) = Double(Math.tan(x.dbl))

  override fun exp(x: Double) = Double(Math.exp(x.dbl))

  override fun log(x: Double) = Double(Math.log(x.dbl))

  override fun pow(x: Double, y: Double) = Double(Math.pow(x.dbl, y.dbl))

  override fun sqrt(x: Double) = Double(Math.sqrt(x.dbl))

  override fun square(x: Double) = Double(x.dbl * x.dbl)
}