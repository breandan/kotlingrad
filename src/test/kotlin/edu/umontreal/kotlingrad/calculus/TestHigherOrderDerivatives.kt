package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestHigherOrderDerivatives: StringSpec({
  with(DoubleFunctor) {
    val x = variable("x")

    "d²x² / dx² should be 0" {
      assertAll(NumericalGenerator) { xVal: Double ->
        val f = x*x
        val `d²f∕dx²` = d(d(f) / d(x)) / d(x)
        `d²f∕dx²`(x to xVal) shouldBeAbout 2.0
      }
    }

    "d²(x² + x) / dx² should be 2" {
      assertAll(NumericalGenerator) { xVal: Double ->
        val f = pow(x, 2) + x
        println(f)
        val `df∕dx` = d(f) / d(x)
        println(`df∕dx`)
        val `d²f∕dx²` = d(`df∕dx`) / d(x)
        println(`d²f∕dx²`)
        `d²f∕dx²`(x to xVal) shouldBeAbout 2.0
      }
    }

    "d²(x² + x²) / dx² should be 4" {
      assertAll(NumericalGenerator) { xVal: Double ->
        val f = pow(x, 2) + pow(x, 2)
        val `d²f∕dx²` = d(d(f) / d(x)) / d(x)
        `d²f∕dx²`(x to xVal) shouldBeAbout 4.0
      }
    }

    "d³(x³) / dx² should be 6x" {
      assertAll(NumericalGenerator) { xVal: Double ->
        val f = pow(x, 3)
        val `d³f∕dx²` = d(d(f) / d(x)) / d(x)
        `d³f∕dx²`(x to xVal) shouldBeAbout (x * 6)(x to xVal)
      }
    }
  }
})
