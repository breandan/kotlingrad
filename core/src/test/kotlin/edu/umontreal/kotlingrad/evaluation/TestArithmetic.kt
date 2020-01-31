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
        (y pow 3)(ẏ) shouldBeAbout (y * y * y)(ẏ)
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
      DoubleGenerator(0).assertAll { ẋ, ẏ ->
        (x / y)(x to ẋ, y to ẏ) shouldBeAbout ẋ / ẏ
      }
    }

    "test inverse" {
      DoubleGenerator(0).assertAll { ẋ, ẏ ->
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

    "test compositional associativity" {
      DoubleGenerator(0, positive = false, expRange = 0..5).assertAll { ẋ, ẏ, ż ->
        val f = 4 * z + x
        val g = 3 * y + z
        val h = 2 * x + y
        val fogoho = f(x to g, y to h)
        val fo_goh = f(x to g(y to h))
        val fog_oh = f(x to g)(y to h)

        fogoho(x to ẋ, y to ẏ, z to ż) shouldBeAbout fo_goh(x to ẋ, y to ẏ, z to ż)
        fo_goh(x to ẋ, y to ẏ, z to ż) shouldBeAbout fog_oh(x to ẋ, y to ẏ, z to ż)
        fog_oh(x to ẋ, y to ẏ, z to ż) shouldBeAbout fogoho(x to ẋ, y to ẏ, z to ż)
      }
    }
  }
})