package edu.umontreal.kotlingrad.evaluation

import edu.umontreal.kotlingrad.calculus.DoubleGenerator
import edu.umontreal.kotlingrad.experimental.DoublePrecision
import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters")
class TestArithmetic: StringSpec({
  with(DoublePrecision) {
    "test addition" {
      DoubleGenerator.assertAll { ẋ, ẏ ->
        (x + y).invoke(x to ẋ, y to ẏ) shouldBeAbout ẏ + ẋ
      }
    }

    "test subtraction" {
      DoubleGenerator.assertAll { ẋ, ẏ ->
        (x - y).invoke(x to ẋ, y to ẏ) shouldBeAbout ẋ - ẏ
      }
    }

    "test exponentiation" {
      DoubleGenerator.assertAll { ẏ ->
        (y pow 3)(y to ẏ) shouldBeAbout (y * y * y)(y to ẏ)
      }
    }

    "test unary minus" {
      DoubleGenerator.assertAll { ẋ, ẏ ->
        (-y + x)(x to ẋ, y to ẏ) shouldBeAbout ẋ - ẏ
      }
    }

    "test multiplication" {
      DoubleGenerator.assertAll { ẋ, ẏ ->
        (x * y)(x to ẋ, y to ẏ) shouldBeAbout ẋ * ẏ
      }
    }

    "test multiplication with numerical type" {
      DoubleGenerator.assertAll { ẋ, ẏ ->
        (x * 2)(x to ẋ) shouldBeAbout ẋ * 2
      }
    }

    "test division" {
      DoubleGenerator.assertAll { ẋ, ẏ ->
        (x / y)(x to ẋ, y to ẏ) shouldBeAbout x(x to ẋ).asDouble() / y(y to ẏ).asDouble()
      }
    }

    "test inverse" {
      DoubleGenerator.assertAll { ẋ, ẏ ->
        val f = x * 1 / y
        val g = x / y
        f(x to ẋ, y to ẏ) shouldBeAbout g(x to ẋ, y to ẏ)
      }
    }

    "test associativity" {
      DoubleGenerator.assertAll { ẋ, ẏ, ż ->
        val f = x * (y * z)
        val g = (x * y) * z
        f(x to ẋ, y to ẏ, z to ż) shouldBeAbout g(x to ẋ, y to ẏ, z to ż)
      }
    }

    "test commutativity" {
      DoubleGenerator.assertAll { ẋ, ẏ, ż ->
        val f = x * y * z
        val g = z * y * x
        f(x to ẋ, y to ẏ, z to ż) shouldBeAbout g(x to ẋ, y to ẏ, z to ż)
      }
    }

    "test distributivity" {
      DoubleGenerator.assertAll { ẋ, ẏ, ż ->
        val f = x * (y + z)
        val g = x * y + x * z
        f(x to ẋ, y to ẏ, z to ż) shouldBeAbout g(x to ẋ, y to ẏ, z to ż)
      }
    }
  }
})