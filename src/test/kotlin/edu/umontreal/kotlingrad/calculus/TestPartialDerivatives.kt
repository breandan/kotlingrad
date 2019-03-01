package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.numerical.DoublePrecision
import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestPartialDerivatives: StringSpec({
  with(DoublePrecision) {
    val x = Var("x")
    val y = Var("y")

    "∂x / ∂y should be 0" {
      NumericalGenerator.assertAll { ẋ, ẏ ->
        val f = cos(x)
        val `∂f∕∂y` = d(f) / d(y)
        `∂f∕∂y`(x to ẋ, y to ẏ) shouldBeAbout 0
      }
    }

    "∂(x + y) / ∂x should be 1" {
      NumericalGenerator.assertAll { ẋ, ẏ ->
        val f = x + y
        val `∂f∕∂x` = d(f) / d(x)
        `∂f∕∂x`(x to ẋ, y to ẏ) shouldBeAbout 1
      }
    }

    "∂(x + y + x) / ∂x should be 2" {
      NumericalGenerator.assertAll { ẋ, ẏ ->
        val f = x + y + x
        val `∂f∕∂x` = d(f) / d(x)
        `∂f∕∂x`(x to ẋ, y to ẏ) shouldBeAbout 2
      }
    }

    "∂(yx) / ∂x should be y" {
      NumericalGenerator.assertAll { ẋ, ẏ ->
        val f = y * x
        val `∂f∕∂x` = d(f) / d(x)
        `∂f∕∂x`(x to ẋ, y to ẏ) shouldBeAbout ẏ
      }
    }

    "∂(yx + xx + yy) / ∂x should be y + 2x" {
      NumericalGenerator.assertAll { ẋ, ẏ ->
        val f = y * x + x * x + y * y
        val `∂f∕∂x` = d(f) / d(x)
        `∂f∕∂x`(x to ẋ, y to ẏ) shouldBeAbout (y + x * 2)(x to ẋ, y to ẏ)
      }
    }
  }
})