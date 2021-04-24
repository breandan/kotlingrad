package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.*
import edu.umontreal.kotlingrad.api.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestHigherOrderDerivatives: StringSpec({
  val x by SVar(DReal)

  fun dgen() = DoubleGenerator(expRange = -1..1)

  "d²x² / dx² should be 2" {
    checkAll(dgen(), dgen(), dgen()) { ẋ, ẏ, ż ->
      val f = x * x
      val `d²f∕dx²` = d(d(f) / d(x)) / d(x)
      `d²f∕dx²`(x to ẋ) shouldBeAbout 2
    }
  }

  "d²(x² + x) / dx² should be 2" {
    checkAll(dgen(), dgen(), dgen()) { ẋ, ẏ, ż ->
      val f = x.pow(2) + x
      val `df∕dx` = d(f) / d(x)
      val `d²f∕dx²` = d(`df∕dx`) / d(x)
      `d²f∕dx²`(x to ẋ) shouldBeAbout 2
    }
  }

  "d²x³ / dx² should be 6x" {
    checkAll(dgen(), dgen(), dgen()) { ẋ, ẏ, ż ->
      val f = x.pow(3)
      val `d²f∕dx²` = d(d(f) / d(x)) / d(x)
      `d²f∕dx²`(x to ẋ) shouldBeAbout (x * 6)(x to ẋ)
    }
  }

  "d²(sin(x * cos(x * sin(x * cos(x))))) / dx² should be about -13.81" {
    val f = sin(x * cos(x * sin(x * cos(x))))
    val `d²f∕dx²` = d(d(f) / d(x)) / d(x)

    `d²f∕dx²`(x to 2.0) shouldBeAbout -13.81831095
  }
})