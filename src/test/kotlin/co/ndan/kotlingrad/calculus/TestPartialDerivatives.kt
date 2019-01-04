package co.ndan.kotlingrad.calculus

import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import co.ndan.kotlingrad.math.calculus.DoubleFunctor
import co.ndan.kotlingrad.math.numerical.Double
import co.ndan.kotlingrad.math.types.Variable
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TestPartialDerivatives: StringSpec({
  val x = Variable("x", Double(0))
  val y = Variable("y", Double(0))

  with(DoubleFunctor) {
    "∂x / ∂y should be 0" {
      assertAll(DoubleGenerator, DoubleGenerator) { xt, yt ->
        val f = cos(x)
        val `∂f_∂y` = d(f) / d(y)
        `∂f_∂y`(x to xt, y to yt).dbl + 0.0 shouldBe 0.0
      }
    }

    "∂(x + y) / ∂x should be 1" {
      assertAll(DoubleGenerator, DoubleGenerator) { xt, yt ->
        val f = x + y
        val `∂f_∂x` = d(f) / d(x)
        `∂f_∂x`(x to xt, y to yt).dbl shouldBe 1.0
      }
    }

    "∂(x + y + x) / ∂x should be 2" {
      assertAll(DoubleGenerator, DoubleGenerator) { xt, yt ->
        val f = x + y + x
        val `∂f_∂x` = d(f) / d(x)
        `∂f_∂x`(x to xt, y to yt).dbl shouldBe 2.0
      }
    }

    "∂(yx) / ∂x should be y" {
      assertAll(DoubleGenerator, DoubleGenerator) { xt, yt ->
        val f = y * x
        val `∂f_∂x` = d(f) / d(x)
        `∂f_∂x`(x to xt, y to yt).dbl shouldBe yt.dbl
      }
    }

    "∂(yx + xx + yy) / ∂x should be y + 2x" {
      assertAll(DoubleGenerator, DoubleGenerator) { xt, yt ->
        val f = y * x + x * x + y * y
        val `∂f_∂x` = d(f) / d(x)
        `∂f_∂x`(x to xt, y to yt).dbl shouldBe (y + x * 2)(x to xt, y to yt).dbl
      }
    }
  }
})