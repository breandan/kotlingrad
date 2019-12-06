package edu.umontreal.kotlingrad.evaluation

import edu.umontreal.kotlingrad.calculus.DoubleGenerator
import edu.umontreal.kotlingrad.numerical.DoublePrecision
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
        (x - y)(x to ẋ, y to ẏ) shouldBeAbout ẋ - ẏ
      }
    }

    "test exponentiation" {
      DoubleGenerator.assertAll { ẋ, ẏ ->
        pow(y, 3)(ẋ) shouldBeAbout (y * y * y)(ẋ)
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
        (x * 2)(ẋ) shouldBeAbout ẋ * 2
      }
    }

    "test division" {
      DoubleGenerator.assertAll { ẋ, ẏ ->
        val values = mapOf(x to ẋ, y to ẏ)
        (x / y)(values) shouldBeAbout x(values) / y(values)
      }
    }

    "test inverse" {
      DoubleGenerator.assertAll { ẋ, ẏ ->
        val values = mapOf(x to ẋ, y to ẏ)
        (x * y.inverse())(values) shouldBeAbout (x / y)(values)
      }
    }

    "test associativity" {
      DoubleGenerator.assertAll { ẋ, ẏ, ż ->
        val values = mapOf(x to ẋ, y to ẏ, z to ż)
        (x * (y * z))(values) shouldBeAbout ((x * y) * z)(values)
      }
    }

    "test commutativity" {
      DoubleGenerator.assertAll { ẋ, ẏ, ż ->
        val values = mapOf(x to ẋ, y to ẏ, z to ż)
        (x * y * z)(values) shouldBeAbout (z * y * x)(values)
      }
    }

    "test distributivity" {
      DoubleGenerator.assertAll { ẋ, ẏ, ż ->
        val values = mapOf(x to ẋ, y to ẏ, z to ż)
        (x * (y + z))(values) shouldBeAbout (x * y + x * z)(values)
      }
    }
  }
})