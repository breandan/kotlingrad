package edu.umontreal.kotlingrad.evaluation

import edu.umontreal.kotlingrad.calculus.DoubleFunctor
import edu.umontreal.kotlingrad.calculus.NumericalGenerator
import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters")
class TestArithmetic: StringSpec({
  with(DoubleFunctor) {
    val x = variable("x")
    val y = variable("y")
    val z = variable("z")

    "test addition" {
      assertAll(NumericalGenerator, NumericalGenerator) { ẋ, ẏ ->
        (x + y).invoke(x to ẋ, y to ẏ) shouldBeAbout ẏ + ẋ
      }
    }

    "test subtraction" {
      assertAll(NumericalGenerator, NumericalGenerator) { ẋ, ẏ ->
        (x - y)(x to ẋ, y to ẏ) shouldBeAbout ẋ - ẏ
      }
    }

    "test exponentiation" {
      assertAll(NumericalGenerator) { ẋ ->
        pow(y, 3)(ẋ) shouldBeAbout (y * y * y)(ẋ)
      }
    }

    "test unary minus" {
      assertAll(NumericalGenerator, NumericalGenerator) { ẋ, ẏ ->
        (-y + x)(x to ẋ, y to ẏ) shouldBeAbout ẋ - ẏ
      }
    }

    "test multiplication" {
      assertAll(NumericalGenerator, NumericalGenerator) { ẋ, ẏ ->
        (x * y)(x to ẋ, y to ẏ) shouldBeAbout ẋ * ẏ
      }
    }

    "test multiplication with numerical type" {
      assertAll(NumericalGenerator) { ẋ: Double ->
        (x * 2)(x to ẋ) shouldBeAbout ẋ * 2
      }
    }

    "test division" {
      assertAll(NumericalGenerator, NumericalGenerator) { ẋ, ẏ ->
        val values = mapOf(x to ẋ, y to ẏ)
        (x / y)(values) shouldBeAbout x(values) / y(values)
      }
    }

    "test inverse" {
      assertAll(NumericalGenerator, NumericalGenerator(0)) { ẋ, ẏ ->
        val values = mapOf(x to ẋ, y to ẏ)
        (x * y.inverse())(values) shouldBeAbout (x / y)(values)
      }
    }

    "test associativity" {
      assertAll(NumericalGenerator, NumericalGenerator, NumericalGenerator) { ẋ, ẏ, zVal ->
        val values = mapOf(x to ẋ, y to ẏ, z to zVal)
        (x * (y * z))(values) shouldBeAbout ((x * y) * z)(values)
      }
    }

    "test commutativity" {
      assertAll(NumericalGenerator, NumericalGenerator, NumericalGenerator) { ẋ, ẏ, zVal ->
        val values = mapOf(x to ẋ, y to ẏ, z to zVal)
        (x * y * z)(values) shouldBeAbout (z * y * x)(values)
      }
    }

    "test distributivity" {
      assertAll(NumericalGenerator, NumericalGenerator, NumericalGenerator) { ẋ, ẏ, zVal ->
        val values = mapOf(x to ẋ, y to ẏ, z to zVal)
        (x * (y + z))(values) shouldBeAbout (x * y + x * z)(values)
      }
    }
  }
})