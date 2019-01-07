package edu.umontreal.kotlingrad.calculus


import edu.umontreal.kotlingrad.math.calculus.DoubleFunctor
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestSimpleDerivatives: StringSpec({
  with(DoubleFunctor) {
    val x = variable("x")
    val y = variable("y")

    "dx / dx should be 1" {
      assertAll(DoubleRealGenerator) { xVal ->
        val f = x * 1
        val `∂f_∂x` = d(f) / d(x)
        `∂f_∂x`(x to xVal).dbl + 0.0 shouldBe 1.0
      }
    }

    "d(2x) / dx should be 2" {
      assertAll(DoubleRealGenerator) { xVal ->
        val f = x * 2
        val `∂f_∂x` = d(f) / d(x)
        `∂f_∂x`(x to xVal).dbl shouldBe 2.0
      }
    }

    "d(x + x) / dx should be 2" {
      assertAll(DoubleRealGenerator) { xVal ->
        val f = x + x
        val `∂f_∂x` = d(f) / d(x)
        `∂f_∂x`(x to xVal).dbl shouldBe 2.0
      }
    }
  }
})