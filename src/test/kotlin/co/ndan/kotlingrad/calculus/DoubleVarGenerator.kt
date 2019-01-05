package co.ndan.kotlingrad.calculus

import co.ndan.kotlingrad.math.numerical.DoubleReal
import io.kotlintest.properties.Gen

object DoubleGenerator: Gen<DoubleReal> {
  override fun constants() = listOf(DoubleReal(0))
  override fun random(): Sequence<DoubleReal> = generateSequence { DoubleReal(Gen.double().random().first()) }
}