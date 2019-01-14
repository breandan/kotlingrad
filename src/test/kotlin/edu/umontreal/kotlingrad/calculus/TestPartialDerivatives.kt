package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestPartialDerivatives: StringSpec({
  with(DoubleFunctor) {
    val x = variable("x")
    val y = variable("y")

    "∂x / ∂y should be 0" {
      assertAll(NumericalGenerator, NumericalGenerator) { ẋ, ẏ ->
        val f = cos(x)
        val `∂f∕∂y` = d(f) / d(y)
        `∂f∕∂y`(x to ẋ, y to ẏ) + 0.0 shouldBeAbout 0.0
      }
    }

    "∂(x + y) / ∂x should be 1" {
      assertAll(NumericalGenerator, NumericalGenerator) { ẋ, ẏ ->
        val f = x + y
        val `∂f∕∂x` = d(f) / d(x)
        `∂f∕∂x`(x to ẋ, y to ẏ) shouldBeAbout 1.0
      }
    }

    "∂(x + y + x) / ∂x should be 2" {
      assertAll(NumericalGenerator, NumericalGenerator) { ẋ, ẏ ->
        val f = x + y + x
        val `∂f∕∂x` = d(f) / d(x)
        `∂f∕∂x`(x to ẋ, y to ẏ) shouldBeAbout 2.0
      }
    }

    "∂(yx) / ∂x should be y" {
      assertAll(NumericalGenerator, NumericalGenerator) { ẋ, ẏ ->
        val f = y * x
        val `∂f∕∂x` = d(f) / d(x)
        `∂f∕∂x`(x to ẋ, y to ẏ) shouldBeAbout ẏ
      }
    }

    "∂(yx + xx + yy) / ∂x should be y + 2x" {
      assertAll(NumericalGenerator, NumericalGenerator) { ẋ, ẏ ->
        val f = y * x + pow(x, 2) + pow(y, 2)
        val `∂f∕∂x` = d(f) / d(x)
        `∂f∕∂x`(x to ẋ, y to ẏ) shouldBeAbout (y + x * 2)(x to ẋ, y to ẏ)
      }
    }
  }
})