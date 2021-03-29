package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.*
import edu.umontreal.kotlingrad.api.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestPartialDerivatives: StringSpec({
  val x by SVar(DReal)
  val y by SVar(DReal)

  "∂x / ∂y should be 0" {
    checkAll(DoubleGenerator, DoubleGenerator) { ẋ, ẏ ->
      val f = cos(x)
      val `∂f∕∂y` = d(f) / d(y)
      `∂f∕∂y`(ẋ, ẏ) shouldBeAbout 0
    }
  }

  "∂(x + y) / ∂x should be 1" {
    checkAll(DoubleGenerator, DoubleGenerator) { ẋ, ẏ ->
      val f = x + y
      val `∂f∕∂x` = d(f) / d(x)
      `∂f∕∂x`(ẋ, ẏ) shouldBeAbout 1
    }
  }

  "∂(x + y + x) / ∂x should be 2" {
    checkAll(DoubleGenerator, DoubleGenerator) { ẋ, ẏ ->
      val f = x + y + x
      val `∂f∕∂x` = d(f) / d(x)
      `∂f∕∂x`(ẋ, ẏ) shouldBeAbout 2
    }
  }

  "∂(yx) / ∂x should be y" {
    checkAll(DoubleGenerator, DoubleGenerator) { ẋ, ẏ ->
      val f = y * x
      val `∂f∕∂x` = d(f) / d(x)
      `∂f∕∂x`(ẋ, ẏ) shouldBeAbout ẏ
    }
  }

  "∂(yx + xx + yy) / ∂x should be y + 2x" {
    checkAll(DoubleGenerator, DoubleGenerator) { ẋ, ẏ ->
      val f = y * x + x * x + y * y
      val `∂f∕∂x` = d(f) / d(x)
      `∂f∕∂x`(ẋ, ẏ) shouldBeAbout (y + x * 2)(ẋ, ẏ)
    }
  }
})