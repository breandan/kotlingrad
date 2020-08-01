package edu.umontreal.kotlingrad.calculus


import edu.umontreal.kotlingrad.*
import edu.umontreal.kotlingrad.experimental.*
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestSimpleDerivatives: StringSpec({
  val x by SVar(DReal)
  "dx / dx should be 1" {
    DoubleGenerator.assertAll { ẋ ->
      val f = x * 1
      val `∂f∕∂x` = d(f) / d(x)
      `∂f∕∂x`(ẋ) shouldBeAbout 1
    }
  }

  "d(2x) / dx should be 2" {
    DoubleGenerator.assertAll { ẋ ->
      val f = x * 2
      val `df∕dx` = d(f) / d(x)
      `df∕dx`(ẋ) shouldBeAbout 2
    }
  }

  "d(x + x) / dx should be 2" {
    DoubleGenerator.assertAll { ẋ ->
      val f = x + x
      val `df∕dx` = d(f) / d(x)
      `df∕dx`(ẋ) shouldBeAbout 2
    }
  }
})