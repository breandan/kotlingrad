package edu.umontreal.kotlingrad.calculus


import edu.umontreal.kotlingrad.math.calculus.DoubleFunctor
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestHigherOrderDerivatives: StringSpec({
  with(DoubleFunctor) {
    val x = variable("x")

    "d²x² / dx² should be 0" {
      assertAll(DoubleGenerator) { xVal ->
        val f = x * x
        val `d²f_dx²` = d(d(f) / d(x)) / d(x)
        `d²f_dx²`(x to xVal).dbl shouldBe 2.0
      }
    }

    "d²(x² + x) / dx² should be 2" {
      assertAll(DoubleGenerator) { xVal ->
        val f = x * x + x
        val `d²f_dx²` = d(d(f) / d(x)) / d(x)
        `d²f_dx²`(x to xVal).dbl shouldBe 2.0
      }
    }

    "d²(x² + x²) / dx² should be 4" {
      assertAll(DoubleGenerator) { xVal ->
        val f = x * x + x * x
        val `d²f_dx²` = d(d(f) / d(x)) / d(x)
        `d²f_dx²`(x to xVal).dbl shouldBe 4.0
      }
    }

    "d³(x³) / dx² should be 6x" {
      assertAll(DoubleGenerator) { xVal ->
        val f = x * x * x
        val `d³f_dx²` = d(d(f) / d(x)) / d(x)
        `d³f_dx²`(x to xVal).dbl shouldBe (x * 6)(x to xVal).dbl
      }
    }
  }
})
