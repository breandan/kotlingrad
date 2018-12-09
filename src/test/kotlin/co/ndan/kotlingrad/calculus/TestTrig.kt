package co.ndan.kotlingrad.calculus

import co.ndan.kotlingrad.math.algebra.DoublePrototype
import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import co.ndan.kotlingrad.math.calculus.RealFunctor
import co.ndan.kotlingrad.math.types.Double
import co.ndan.kotlingrad.math.types.Var
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TestTrig: StringSpec({
  val rft = RealFunctor(DoublePrototype)
  "d(sin(x)) / dx should be cos(x)" {
    assertAll(DoubleVarGenerator) { x: Var<Double> ->
      d(rft.sin(x)) / d(x) shouldBe rft.cos(x) * 1
    }
  }

  "d(cos(x)) / dx should be -sin(x)" {
    assertAll(DoubleVarGenerator) { x: Var<Double> ->
      d(rft.cos(x)) / d(x) shouldBe rft.sin(x) * -1
    }
  }
})