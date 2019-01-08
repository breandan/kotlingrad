package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.numerical.DoubleReal
import io.kotlintest.properties.Gen

object DoubleRealGenerator: Gen<DoubleReal> {
  override fun constants() = listOf(DoubleReal(0))
  override fun random(): Sequence<DoubleReal> = generateSequence { DoubleReal(Gen.double().random().first()) }
}