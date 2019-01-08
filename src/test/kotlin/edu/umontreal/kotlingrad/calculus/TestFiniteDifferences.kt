package edu.umontreal.kotlingrad.calculus

import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlin.math.cos
import kotlin.math.sin

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestFiniteDifferences: StringSpec({
  val ε = 1E-6
  val dx = 1E-8

  with(DoubleFunctor) {
    val x = variable("x")

    "test sin" {
      assertAll(DoubleRealGenerator) { xVal ->
        val f = sin(x)
        val df_dx = d(f) / d(x)
        df_dx(x to xVal).dbl shouldBe (((sin(xVal.dbl + dx) - sin(xVal.dbl)) / dx) plusOrMinus ε)
      }
    }

    "test cos" {
      assertAll(DoubleRealGenerator) { xVal ->
        val f = cos(x)
        val df_dx = d(f) / d(x)
        df_dx(x to xVal).dbl shouldBe (((cos(xVal.dbl + dx) - cos(xVal.dbl)) / dx) plusOrMinus ε)
      }
    }

    "test composition" {
      assertAll(DoubleRealGenerator) { xVal ->
        val f = sin(pow(x, 2))
        val df_dx = d(f) / d(x)

        val xdx = xVal.dbl + dx

        df_dx(x to xVal).dbl shouldBe (((sin(xdx * xdx) - sin(xVal.dbl * xVal.dbl)) / dx) plusOrMinus ε)
      }
    }
  }
})