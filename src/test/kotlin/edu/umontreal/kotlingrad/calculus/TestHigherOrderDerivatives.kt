package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.numerical.DoublePrecision
import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestHigherOrderDerivatives: StringSpec({
  with(DoublePrecision) {
    val x = Var("x")

    "d²x² / dx² should be 0" {
      NumericalGenerator.assertAll { ẋ: Double ->
        val f = x * x
        val `d²f∕dx²` = d(d(f) / d(x)) / d(x)
        `d²f∕dx²`(x to ẋ) shouldBeAbout 2
      }
    }

    "d²(x² + x) / dx² should be 2" {
      NumericalGenerator.assertAll { ẋ: Double ->
        val f = pow(x, 2) + x
        val `df∕dx` = d(f) / d(x)
        val `d²f∕dx²` = d(`df∕dx`) / d(x)
        `d²f∕dx²`(x to ẋ) shouldBeAbout 2
      }
    }

    "d²(x² + x²) / dx² should be 4" {
      NumericalGenerator.assertAll { ẋ: Double ->
        val f = pow(x, 2) + pow(x, 2)
        val `d²f∕dx²` = d(d(f) / d(x)) / d(x)
        `d²f∕dx²`(x to ẋ) shouldBeAbout 4
      }
    }

    "d³(x³) / dx² should be 6x" {
      NumericalGenerator.assertAll { ẋ: Double ->
        val f = pow(x, 3)
        val `d³f∕dx²` = d(d(f) / d(x)) / d(x)
        `d³f∕dx²`(x to ẋ) shouldBeAbout (x * 6)(x to ẋ)
      }
    }
  }
})
