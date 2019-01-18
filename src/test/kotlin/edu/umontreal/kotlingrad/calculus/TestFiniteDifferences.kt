package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.numerical.DoublePrecision
import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec
import kotlin.math.cos
import kotlin.math.sin

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestFiniteDifferences: StringSpec({
  val dx = 1E-8

  with(DoublePrecision) {
    val x = variable("x")

    "sin should be (sin(x + dx) - sin(x)) / dx" {
      assertAll(NumericalGenerator) { ẋ ->
        val f = sin(x)
        val `df∕dx` = d(f) / d(x)
        `df∕dx`(ẋ) shouldBeAbout (sin(ẋ + dx) - sin(ẋ)) / dx
      }
    }

    "cos should be (cos(x + dx) - cos(x)) / dx" {
      assertAll(NumericalGenerator) { ẋ ->
        val f = cos(x)
        val `df∕dx` = d(f) / d(x)
        `df∕dx`(ẋ) shouldBeAbout ((cos(ẋ + dx) - cos(ẋ)) / dx)
      }
    }

    "test composition" {
      assertAll(NumericalGenerator) { ẋ ->
        val f = sin(pow(x, 2))
        val `df∕dx` = d(f) / d(x)

        val xdx = ẋ + dx

        `df∕dx`(ẋ) shouldBeAbout (sin(xdx * xdx) - sin(ẋ * ẋ)) / dx
      }
    }
  }
})