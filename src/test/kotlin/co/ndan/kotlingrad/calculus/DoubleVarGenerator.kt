package co.ndan.kotlingrad.calculus

import co.ndan.kotlingrad.math.algebra.DoublePrototype
import co.ndan.kotlingrad.math.numerical.Double
import co.ndan.kotlingrad.math.types.Var
import io.kotlintest.properties.Gen

object DoubleVarGenerator: Gen<Var<Double>> {
  override fun constants() = listOf(Var("", Double(0), DoublePrototype))
  override fun random(): Sequence<Var<Double>> = generateSequence {
    Var("", Double(Gen.double().random().first()), DoublePrototype)
  }
}