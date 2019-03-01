package edu.umontreal.kotlingrad.evaluation

import edu.umontreal.kotlingrad.calculus.NumericalGenerator
import edu.umontreal.kotlingrad.numerical.DoublePrecision
import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters")
class TestArithmetic: StringSpec({
  with(DoublePrecision) {
    val x = Var("x")
    val y = Var("y")
    val z = Var("z")

    "test addition" {
      NumericalGenerator.assertAll { ẋ, ẏ ->
        (x + y).invoke(x to ẋ, y to ẏ) shouldBeAbout ẏ + ẋ
      }
    }

    "test subtraction" {
      NumericalGenerator.assertAll { ẋ, ẏ ->
        (x - y)(x to ẋ, y to ẏ) shouldBeAbout ẋ - ẏ
      }
    }

    "test exponentiation" {
      NumericalGenerator.assertAll { ẋ, ẏ ->
        pow(y, 3)(ẋ) shouldBeAbout (y * y * y)(ẋ)
      }
    }

    "test unary minus" {
      NumericalGenerator.assertAll { ẋ, ẏ ->
        (-y + x)(x to ẋ, y to ẏ) shouldBeAbout ẋ - ẏ
      }
    }

    "test multiplication" {
      NumericalGenerator.assertAll { ẋ, ẏ ->
        (x * y)(x to ẋ, y to ẏ) shouldBeAbout ẋ * ẏ
      }
    }

    "test multiplication with numerical type" {
      NumericalGenerator.assertAll { ẋ, ẏ ->
        (x * 2)(ẋ) shouldBeAbout ẋ * 2
      }
    }

    "test division" {
      NumericalGenerator.assertAll { ẋ, ẏ ->
        val values = mapOf(x to ẋ, y to ẏ)
        (x / y)(values) shouldBeAbout x(values) / y(values)
      }
    }

    "test inverse" {
      NumericalGenerator.assertAll { ẋ, ẏ ->
        val values = mapOf(x to ẋ, y to ẏ)
        (x * y.inverse())(values) shouldBeAbout (x / y)(values)
      }
    }

    "test associativity" {
      NumericalGenerator.assertAll { ẋ, ẏ, ż ->
        val values = mapOf(x to ẋ, y to ẏ, z to ż)
        (x * (y * z))(values) shouldBeAbout ((x * y) * z)(values)
      }
    }

    "test commutativity" {
      NumericalGenerator.assertAll { ẋ, ẏ, ż ->
        val values = mapOf(x to ẋ, y to ẏ, z to ż)
        (x * y * z)(values) shouldBeAbout (z * y * x)(values)
      }
    }

    "test distributivity" {
      NumericalGenerator.assertAll { ẋ, ẏ, ż ->
        val values = mapOf(x to ẋ, y to ẏ, z to ż)
        (x * (y + z))(values) shouldBeAbout (x * y + x * z)(values)
      }
    }
  }
})