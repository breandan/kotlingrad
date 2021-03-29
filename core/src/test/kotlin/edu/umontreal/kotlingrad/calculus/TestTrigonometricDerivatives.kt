package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.*
import edu.umontreal.kotlingrad.api.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestTrigonometricDerivatives: StringSpec({
  val x by SVar(DReal)
  val y by SVar(DReal)

  "d(sin(x)) / dx should be cos(x)" {
    checkAll(DoubleGenerator) { ẋ ->
      val f = sin(x)
      val `df∕dx` = d(f) / d(x)
      `df∕dx`(ẋ) shouldBeAbout cos(x)(ẋ)
    }
  }

  "d(cos(x)) / dx should be -sin(x)" {
    checkAll(DoubleGenerator) { ẋ ->
      val f = cos(x)
      val `df∕dx` = d(f) / d(x)
      `df∕dx`(ẋ) shouldBeAbout -sin(x)(ẋ)
    }
  }

  val z = y * (sin(x * y) - x)
  val `∂z∕∂x` = d(z) / d(x)
  val `∂z∕∂y` = d(z) / d(y)
  val `∂²z∕∂x²` = d(`∂z∕∂x`) / d(x)
  val `∂²z∕∂x∂y` = d(`∂z∕∂x`) / d(y)

  "z should be y * (sin(x * y) - x)".config(enabled = false) {
    checkAll(DoubleGenerator, DoubleGenerator) { ẋ, ẏ ->
      val numericalAnswer = ẏ * (kotlin.math.sin(ẋ * ẏ) - ẋ) + 0.0
      z(ẋ, ẏ) shouldBeAbout numericalAnswer
    }
  }

  "∂z/∂x should be y * (cos(x * y) * y - 1)" {
    checkAll(DoubleGenerator, DoubleGenerator) { ẋ, ẏ ->
      val manualDerivative = y * (cos(x * y) * y - 1)
      `∂z∕∂x`(ẋ, ẏ) shouldBeAbout manualDerivative(ẋ, ẏ)
    }
  }

  "∂z/∂y should be sin(x * y) - x + y * cos(x * y) * x" {
    checkAll(DoubleGenerator, DoubleGenerator) { ẋ, ẏ ->
      val manualDerivative = sin(x * y) - x + y * cos(x * y) * x
      `∂z∕∂y`(ẋ, ẏ) shouldBeAbout manualDerivative(ẋ, ẏ)
    }
  }

  "∂²z/∂x² should be -y * y * y * sin(x * y)"
    .config(enabled = false) // TODO: Fix
    {
      checkAll(DoubleGenerator, DoubleGenerator) { ẋ, ẏ ->
        val manualDerivative = -y.pow(3) * sin(x * y)
        `∂²z∕∂x²`(ẋ, ẏ) shouldBeAbout manualDerivative(ẋ, ẏ)
      }
    }

  "∂²z/∂x∂y should be cos(x * y) * y - 1 + y * (cos(x * y) - y * x * sin(x * y))"
    .config(enabled = false)  // TODO: Fix
    {
      checkAll(DoubleGenerator, DoubleGenerator) { ẋ, ẏ ->
        val manualDerivative =
          cos(x * y) * y - 1 + y * (cos(x * y) - y * x * sin(x * y))
        `∂²z∕∂x∂y`(ẋ, ẏ) shouldBeAbout manualDerivative(ẋ, ẏ)
      }
    }
})