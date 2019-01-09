package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec
import kotlin.math.cos
import kotlin.math.sin

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestFiniteDifferences: StringSpec({
  val dx = 1E-8

  with(DoubleFunctor) {
    val x = variable("x")

    "test sin" {
      assertAll(NumericalGenerator) { xVal ->
        val f = sin(x)
        val df_dx = d(f) / d(x)
        df_dx(x to xVal) shouldBeAbout (sin(xVal + dx) - sin(xVal)) / dx
      }
    }

    "test cos" {
      assertAll(NumericalGenerator) { xVal ->
        val f = cos(x)
        val df_dx = d(f) / d(x)
        df_dx(x to xVal) shouldBeAbout ((cos(xVal + dx) - cos(xVal)) / dx)
      }
    }

    "test composition" {
      assertAll(NumericalGenerator) { xVal ->
        val f = sin(pow(x, 2))
        val df_dx = d(f) / d(x)

        val xdx = xVal + dx

        df_dx(x to xVal) shouldBeAbout (sin(xdx * xdx) - sin(xVal * xVal)) / dx
      }
    }
  }
})