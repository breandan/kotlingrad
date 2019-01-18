package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.numerical.DoublePrecision
import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestTrigonometricDerivatives: StringSpec({
  with(DoublePrecision) {
    val x = variable("x")
    val y = variable("y")

    "d(sin(x)) / dx should be cos(x)" {
      assertAll(NumericalGenerator) { ẋ ->
        val f = sin(x)
        val `df∕dx` = d(f) / d(x)
        `df∕dx`(x to ẋ) shouldBeAbout cos(x)(x to ẋ)
      }
    }

    "d(cos(x)) / dx should be -sin(x)" {
      assertAll(NumericalGenerator) { ẋ ->
        val f = cos(x)
        val `df∕dx` = d(f) / d(x)
        `df∕dx`(x to ẋ) shouldBeAbout -sin(x)(x to ẋ)
      }
    }

    val z = y * (sin(x * y) - x)
    val `∂z∕∂x` = d(z) / d(x)
    val `∂z∕∂y` = d(z) / d(y)
    val `∂²z∕∂x²` = d(`∂z∕∂x`) / d(x)
    val `∂²z∕∂x∂y` = d(`∂z∕∂x`) / d(y)

    "z should be y * (sin(x * y) - x)" {
      assertAll(NumericalGenerator, NumericalGenerator) { ẋ, ẏ ->
        val numericalAnswer = ẏ * (kotlin.math.sin(ẋ * ẏ) - ẋ) + 0.0
        z(x to ẋ, y to ẏ) shouldBeAbout numericalAnswer
      }
    }

    "∂z/∂x should be y * (cos(x * y) * y - 1)" {
      assertAll(NumericalGenerator, NumericalGenerator) { ẋ, ẏ ->
        val manualDerivative = y * (cos(x * y) * y - 1)
        `∂z∕∂x`(x to ẋ, y to ẏ) shouldBeAbout manualDerivative(x to ẋ, y to ẏ)
      }
    }

    "∂z/∂y should be sin(x * y) - x + y * cos(x * y) * x" {
      assertAll(NumericalGenerator, NumericalGenerator) { ẋ, ẏ ->
        val manualDerivative = sin(x * y) - x + y * cos(x * y) * x
        `∂z∕∂y`(x to ẋ, y to ẏ) shouldBeAbout manualDerivative(x to ẋ, y to ẏ)
      }
    }

    "∂²z/∂x² should be -y * y * y * sin(x * y)" {
      assertAll(NumericalGenerator, NumericalGenerator) { ẋ, ẏ ->
        val manualDerivative = -pow(y, 3) * sin(x * y)
        `∂²z∕∂x²`(x to ẋ, y to ẏ) shouldBeAbout manualDerivative(x to ẋ, y to ẏ)
      }
    }

    "∂²z/∂x∂y should be cos(x * y) * y - 1 + y * (cos(x * y) - y * x * sin(x * y))" {
      assertAll(NumericalGenerator, NumericalGenerator) { ẋ, ẏ ->
        val manualDerivative = cos(x * y) * y - 1 + y * (cos(x * y) - y * x * sin(x * y))
        `∂²z∕∂x∂y`(x to ẋ, y to ẏ) shouldBeAbout manualDerivative(x to ẋ, y to ẏ)
      }
    }
  }
})