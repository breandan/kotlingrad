package co.ndan.kotlingrad.calculus

import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import co.ndan.kotlingrad.math.types.Double
import co.ndan.kotlingrad.math.calculus.DoubleFunctor.sin
import co.ndan.kotlingrad.math.calculus.DoubleFunctor.cos
import co.ndan.kotlingrad.math.types.Var
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TestTrig: StringSpec({
  "d(sin(x)) / dx should be cos(x)" {
    assertAll(DoubleVarGenerator) { x: Var<Double> ->
      d(sin(x)) / d(x) shouldBe cos(x) * 1
    }
  }

  "d(cos(x)) / dx should be -sin(x)" {
    assertAll(DoubleVarGenerator) { x: Var<Double> ->
      d(cos(x)) / d(x) shouldBe sin(x) * -1
    }
  }
})