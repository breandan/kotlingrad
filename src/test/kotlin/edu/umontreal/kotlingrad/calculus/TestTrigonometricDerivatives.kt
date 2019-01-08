package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.numerical.DoubleReal
import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlin.math.sin

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestTrigonometricDerivatives: StringSpec({
  with(DoubleFunctor) {
    val x = variable("x")
    val y = variable("y")

    "d(sin(x)) / dx should be cos(x)" {
      assertAll(DoubleRealGenerator) { xVal ->
        val f = sin(x)
        val df_dx = d(f) / d(x)
        df_dx(x to xVal).dbl shouldBe cos(x)(x to xVal).dbl
      }
    }

    "d(cos(x)) / dx should be -sin(x)" {
      assertAll(DoubleRealGenerator) { xVal ->
        val f = cos(x)
        val df_dx = d(f) / d(x)
        df_dx(x to xVal).dbl shouldBe -sin(x)(x to xVal).dbl + 0
      }
    }

    val z = y * (sin(x * y) - x)
    val `∂z∕∂x` = d(z) / d(x)
    val `∂z∕∂y` = d(z) / d(y)
    val `∂²z∕∂x²` = d(`∂z∕∂x`) / d(x)
    val `∂²z∕∂x∂y` = d(`∂z∕∂x`) / d(y)

    val ε = 1E-15

    "test z" {
      assertAll { kx: kotlin.Double, ky: kotlin.Double ->
        val numericalAnswer = ky * (sin(kx * ky) - kx) + 0.0
        z(x to DoubleReal(kx), y to DoubleReal(ky)).dbl shouldBe numericalAnswer
      }
    }

    "test ∂z/∂x" {
      assertAll(DoubleRealGenerator, DoubleRealGenerator) { xVal, yVal ->
        val manualDerivative = y * (cos(x * y) * y - one)
        `∂z∕∂x`(x to xVal, y to yVal).dbl shouldBe (manualDerivative(x to xVal, y to yVal).dbl plusOrMinus ε)
      }
    }

    "test ∂z/∂y" {
      assertAll(DoubleRealGenerator, DoubleRealGenerator) { xVal, yVal ->
        val manualDerivative = (sin(x * y) - x + y * cos(x * y) * x)
        `∂z∕∂y`(x to xVal, y to yVal).dbl shouldBe (manualDerivative(x to xVal, y to yVal).dbl plusOrMinus ε)
      }
    }

    "test ∂²z/∂x²" {
      assertAll(DoubleRealGenerator, DoubleRealGenerator) { xVal, yVal ->
        val manualDerivative = (-y * y * y * sin(x * y))
        `∂²z∕∂x²`(x to xVal, y to yVal).dbl shouldBe (manualDerivative(x to xVal, y to yVal).dbl plusOrMinus ε)
      }
    }

    "test ∂²z/∂x∂y" {
      assertAll(DoubleRealGenerator, DoubleRealGenerator) { xVal, yVal ->
        val manualDerivative = (cos(x * y) * y - one + y * (cos(x * y) - y * x * sin(x * y)))
        `∂²z∕∂x∂y`(x to xVal, y to yVal).dbl shouldBe (manualDerivative(x to xVal, y to yVal).dbl plusOrMinus ε)
      }
    }
  }
})