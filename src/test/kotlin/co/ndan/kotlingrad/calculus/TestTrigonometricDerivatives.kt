package co.ndan.kotlingrad.calculus

import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import co.ndan.kotlingrad.math.calculus.DoubleFunctor
import co.ndan.kotlingrad.math.numerical.DoubleReal
import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestTrigonometricDerivatives: StringSpec({
  with(DoubleFunctor) {
    val x = variable("x", DoubleReal(1.0))
    val y = variable("y", DoubleReal(1.0))

    "d(sin(x)) / dx should be cos(x)" {
      assertAll(DoubleGenerator) { xt ->
        val f = sin(x)
        val df_dx = d(f) / d(x)
        df_dx(x to xt).dbl shouldBe cos(x)(x to xt).dbl
      }
    }

    "d(cos(x)) / dx should be -sin(x)" {
      assertAll(DoubleGenerator) { xt ->
        val f = cos(x)
        val df_dx = d(f) / d(x)
        df_dx(x to xt).dbl shouldBe -sin(x)(x to xt).dbl + 0
      }
    }

    val z = y * (sin(x * y) - x)
    val `∂z∕∂x` = d(z) / d(x)
    val `∂z∕∂y` = d(z) / d(y)
    val `∂²z∕∂x²` = d(`∂z∕∂x`) / d(x)
    val `∂²z∕∂x∂y` = d(`∂z∕∂x`) / d(y)
    val one = variable("c", DoubleReal(1.0))

    val epsilon = 1E-10

    "test z" {
      assertAll { kx: kotlin.Double, ky: kotlin.Double ->
        val numericalAnswer = ky * (kotlin.math.sin(kx * ky) - kx) + 0.0

        z(x to DoubleReal(kx), y to DoubleReal(ky)).dbl shouldBe numericalAnswer
      }
    }

    "test ∂z/∂x" {
      assertAll(DoubleGenerator, DoubleGenerator) { xt, yt ->
        val cx = variable("cx", xt)
        val cy = variable("cy", yt)
        `∂z∕∂x`(x to xt, y to yt).dbl shouldBe ((cy * (cos(cx * cy) * cy - one))().dbl plusOrMinus epsilon)
      }
    }

    "test ∂z/∂y" {
      assertAll(DoubleGenerator, DoubleGenerator) { xt, yt ->
        val cx = variable("cx", xt)
        val cy = variable("cy", yt)
        `∂z∕∂y`(x to xt, y to yt).dbl shouldBe ((sin(cx * cy) - cx + cy * cos(cx * cy) * cx)().dbl plusOrMinus epsilon)
      }
    }

    "test ∂²z/∂x²" {
      assertAll(DoubleGenerator, DoubleGenerator) { xt, yt ->
        val cx = variable("cx", xt)
        val cy = variable("cy", yt)
        `∂²z∕∂x²`(x to xt, y to yt).dbl shouldBe ((-cy * cy * cy * sin(cx * cy))().dbl plusOrMinus epsilon)
      }
    }

    "test ∂²z/∂x∂y" {
      assertAll(DoubleGenerator, DoubleGenerator) { xt, yt ->
        val cx = variable("cx", xt)
        val cy = variable("cy", yt)
        `∂²z∕∂x∂y`(x to xt, y to yt).dbl shouldBe ((cos(cx * cy) * cy - one + cy * (cos(cx * cy) - cy * cx * sin(cx * cy)))().dbl plusOrMinus epsilon)
      }
    }
  }
})