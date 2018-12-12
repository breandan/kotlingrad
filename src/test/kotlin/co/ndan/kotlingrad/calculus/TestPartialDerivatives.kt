package co.ndan.kotlingrad.calculus

import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import co.ndan.kotlingrad.math.calculus.DoubleFunctor.cos
import co.ndan.kotlingrad.math.types.Double
import co.ndan.kotlingrad.math.types.Var
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TestPartialDerivatives: StringSpec({
  "∂x / ∂y should be 0" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x: Var<Double>, y: Var<Double> ->
      (d(cos(x)) / d(y)).value.dbl + 0.0 shouldBe 0.0
    }
  }


  "∂(x + y) / ∂x should be 1" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x: Var<Double>, y: Var<Double> ->
      (d(x + y) / d(x)).value.dbl shouldBe 1.0
    }
  }

  "∂(x + y + x) / ∂x should be 2" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x: Var<Double>, y: Var<Double> ->
      (d(x + y + x) / d(x)).value.dbl shouldBe 2.0
    }
  }
})