package co.ndan.kotlingrad.calculus

import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import co.ndan.kotlingrad.math.calculus.DoubleFunctor
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TestPartialDerivatives : StringSpec({
  with(DoubleFunctor) {
    "∂x / ∂y should be 0" {
      assertAll(DoubleVarGenerator, DoubleVarGenerator) { x, y ->
        (d(cos(x)) / d(y)).value.dbl + 0.0 shouldBe 0.0
      }
    }

    "∂(x + y) / ∂x should be 1" {
      assertAll(DoubleVarGenerator, DoubleVarGenerator) { x, y ->
        (d(x + y) / d(x)).value.dbl shouldBe 1.0
      }
    }

    "∂(x + y + x) / ∂x should be 2" {
      assertAll(DoubleVarGenerator, DoubleVarGenerator) { x, y ->
        (d(x + y + x) / d(x)).value.dbl shouldBe 2.0
      }
    }

    "∂(yx) / ∂x should be y" {
      assertAll(DoubleVarGenerator, DoubleVarGenerator) { x, y ->
        (d(y * x) / d(x)).value.dbl shouldBe y.value.dbl
      }
    }

    "∂(yx + xx + yy) / ∂x should be y + 2x" {
      assertAll(DoubleVarGenerator, DoubleVarGenerator) { x, y ->
        (d(y * x + x * x + y * y) / d(x)).value.dbl shouldBe (y + x * 2).value.dbl
      }
    }
  }
})