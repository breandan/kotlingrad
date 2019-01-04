package co.ndan.kotlingrad.calculus


import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import co.ndan.kotlingrad.math.numerical.Double
import co.ndan.kotlingrad.math.types.Variable
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TestSimpleDerivatives: StringSpec({
  val x = Variable("x", Double(0))
  val y = Variable("y", Double(0))

  "dx / dx should be 1" {
    assertAll(DoubleGenerator) { xt ->
      val f = x * 1
      val `∂f_∂x` = d(f) / d(x)
      `∂f_∂x`(x to xt).dbl + 0.0 shouldBe 1.0
    }
  }

  "d(2x) / dx should be 2" {
    assertAll(DoubleGenerator) { xt ->
      val f = x * 2
      val `∂f_∂x` = d(f) / d(x)
      `∂f_∂x`(x to xt).dbl shouldBe 2.0
    }
  }

  "d(x + x) / dx should be 2" {
    assertAll(DoubleGenerator) { xt ->
      val f = x + x
      val `∂f_∂x` = d(f) / d(x)
      `∂f_∂x`(x to xt).dbl shouldBe 2.0
    }
  }
})