package edu.umontreal.kotlingrad.calculus


import edu.umontreal.kotlingrad.numerical.DoublePrecision
import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestSimpleDerivatives: StringSpec({
  with(DoublePrecision) {
    val x = variable("x")
    val y = variable("y")

    "dx / dx should be 1" {
      assertAll(NumericalGenerator) { ẋ ->
        val f = x * 1
        val `∂f∕∂x` = d(f) / d(x)
        `∂f∕∂x`(ẋ) shouldBeAbout 1
      }
    }

    "d(2x) / dx should be 2" {
      assertAll(NumericalGenerator) { ẋ ->
        val f = x * 2
        val `df∕dx` = d(f) / d(x)
        `df∕dx`(ẋ) shouldBeAbout 2
      }
    }

    "d(x + x) / dx should be 2" {
      assertAll(NumericalGenerator) { ẋ ->
        val f = x + x
        val `df∕dx` = d(f) / d(x)
        `df∕dx`(ẋ) shouldBeAbout 2
      }
    }

    "d(√x) / dx should be 1/(2√x)" {
      assertAll(NumericalGenerator) { ẋ ->
        val f1 = sqrt(x)
        val f2 = pow(x, 0.5)
        val `df1∕dx` = d(f1) / d(x)
        val `df2∕dx` = d(f2) / d(x)
        `df1∕dx`(ẋ) shouldBeAbout `df2∕dx`(ẋ)
      }
    }
  }
})