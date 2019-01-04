package co.ndan.kotlingrad.calculus

import co.ndan.kotlingrad.math.numerical.Double
import io.kotlintest.properties.Gen

object DoubleGenerator: Gen<Double> {
  override fun constants() = listOf(Double(0))
  override fun random(): Sequence<Double> = generateSequence { Double(Gen.double().random().first()) }
}