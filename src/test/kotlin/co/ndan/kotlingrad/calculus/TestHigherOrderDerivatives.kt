package co.ndan.kotlingrad.calculus


import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TestHigherOrderDerivatives: StringSpec({
  "d²x² / dx² should be 0" {
    assertAll(DoubleVarGenerator) { x ->
      (d(d(x * x) / d(x)) / d(x)).value.dbl shouldBe 2.0
    }
  }

  "d²(x² + x) / dx² should be 2" {
    assertAll(DoubleVarGenerator) { x ->
      val fn = x * x + x
      (d(d(fn) / d(x)) / d(x)).value.dbl shouldBe 2.0
    }
  }

  "d²(x² + x²) / dx² should be 4" {
    assertAll(DoubleVarGenerator) { x ->
      (d(d(x * x + x * x) / d(x)) / d(x)).value.dbl shouldBe 4.0
    }
  }

  "d³(x³) / dx² should be 6x" {
    assertAll(DoubleVarGenerator) { x ->
      (d(d(x * x * x) / d(x)) / d(x)).value.dbl shouldBe (x * 6).value.dbl
    }
  }
})
