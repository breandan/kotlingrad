package co.ndan.kotlingrad.calculus


import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TestSimpleDerivatives: StringSpec({
  "dx / dx should be 1" {
    assertAll(DoubleVarGenerator) { x ->
      (d(x * 1) / d(x)).value.dbl shouldBe 1.0
    }
  }

  "d(2x) / dx should be 2" {
    assertAll(DoubleVarGenerator) { x ->
      (d(x * 2) / d(x)).value.dbl shouldBe 2.0
    }
  }

  "d(x + x) / dx should be 2" {
    assertAll(DoubleVarGenerator) { x ->
      (d(x + x) / d(x)).value.dbl shouldBe 2.0
    }
  }
})