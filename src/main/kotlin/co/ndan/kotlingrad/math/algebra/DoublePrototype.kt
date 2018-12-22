package co.ndan.kotlingrad.math.algebra

import co.ndan.kotlingrad.math.numerical.Double
import java.lang.Math.*

object DoublePrototype: RealPrototype<Double> {
  override val zero = Double(0.0)
  override val one = Double(1.0)

  override fun cos(x: Double) = Double(cos(x.dbl))

  override fun sin(x: Double) = Double(sin(x.dbl))

  override fun tan(x: Double) = Double(tan(x.dbl))

  override fun exp(x: Double) = Double(exp(x.dbl))

  override fun log(x: Double) = Double(log(x.dbl))

  override fun pow(x: Double, y: Double) = Double(pow(x.dbl, y.dbl))

  override fun sqrt(x: Double) = Double(sqrt(x.dbl))

  override fun square(x: Double) = x * x
}