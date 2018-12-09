package co.ndan.kotlingrad.calculus

import co.ndan.kotlingrad.math.algebra.DoublePrototype
import co.ndan.kotlingrad.math.calculus.RealFunctor
import co.ndan.kotlingrad.math.types.Double
import co.ndan.kotlingrad.math.types.Var
import io.kotlintest.properties.Gen

object DoubleVarGenerator: Gen<Var<Double>> {
  private val rft = RealFunctor(DoublePrototype)
  override fun constants() = listOf(rft.variable("x", Double(0)))
  override fun random(): Sequence<Var<Double>> = generateSequence {
    rft.variable("x", Double(Gen.double().random().first()))
  }
}