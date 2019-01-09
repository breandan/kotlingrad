package edu.umontreal.kotlingrad.evaluation

import edu.umontreal.kotlingrad.calculus.DoubleFunctor
import edu.umontreal.kotlingrad.calculus.NumericalGenerator
import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

class TestArithmetic: StringSpec({
  with(DoubleFunctor) {
    val x = variable("x")
    val y = variable("y")
    val z = variable("z")

    "test addition" {
      assertAll(NumericalGenerator, NumericalGenerator) { xVal, yVal ->
        (x + y).invoke(x to xVal, y to yVal) shouldBeAbout yVal + xVal
      }
    }

    "test subtraction" {
      assertAll(NumericalGenerator, NumericalGenerator) { xVal, yVal ->
        (x - y)(x to xVal, y to yVal) shouldBeAbout xVal - yVal
      }
    }

    "test unary minus" {
      assertAll(NumericalGenerator, NumericalGenerator) { xVal, yVal ->
        (-y + x)(x to xVal, y to yVal) shouldBeAbout xVal - yVal
      }
    }

    "test multiplication" {
      assertAll(NumericalGenerator, NumericalGenerator) { xVal, yVal ->
        (x * y)(x to xVal, y to yVal) shouldBeAbout xVal * yVal
      }
    }

    "test multiplication with numerical type" {
      assertAll(NumericalGenerator) { xVal: Double ->
        (x * 2)(x to xVal) shouldBeAbout xVal * 2
      }
    }

    "test division" {
      assertAll(NumericalGenerator, NumericalGenerator) { xVal, yVal ->
        val values = mapOf(x to xVal, y to yVal)
        (x / y)(values) shouldBeAbout x(values) / y(values)
      }
    }

    "test inverse" {
      assertAll(NumericalGenerator, NumericalGenerator(0)) { xVal, yVal ->
        val values = mapOf(x to xVal, y to yVal)
        (x * y.inverse())(values) shouldBeAbout (x / y)(values)
      }
    }

    "test associativity" {
      assertAll(NumericalGenerator, NumericalGenerator, NumericalGenerator) { xVal, yVal, zVal ->
        val values = mapOf(x to xVal, y to yVal, z to zVal)
        (x * (y * z))(values) shouldBeAbout ((x * y) * z)(values)
      }
    }

    "test commutativity" {
      assertAll(NumericalGenerator, NumericalGenerator, NumericalGenerator) { xVal, yVal, zVal ->
        val values = mapOf(x to xVal, y to yVal, z to zVal)
        (x * y * z)(values) shouldBeAbout (z * y * x)(values)
      }
    }

    "test distributivity" {
      assertAll(NumericalGenerator, NumericalGenerator, NumericalGenerator) { xVal, yVal, zVal ->
        val values = mapOf(x to xVal, y to yVal, z to zVal)
        (x * (y + z))(values) shouldBeAbout (x * y + x * z)(values)
      }
    }
  }
})