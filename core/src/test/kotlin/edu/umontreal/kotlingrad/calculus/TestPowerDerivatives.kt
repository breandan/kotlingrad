package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.*
import edu.umontreal.kotlingrad.api.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestPowerDerivatives: StringSpec({
  val x by SVar(DReal)
  "d(1/x) / dx should be -1/x²" {
    checkAll(DoubleGenerator(0)) { ẋ ->
      val f1 = 1 / x
      val f2 = pow(x, -1)
      val manualDeriv = -1 / pow(x, 2)
      val `df1∕dx` = d(f1) / d(x)
      val `df2∕dx` = d(f2) / d(x)
      `df1∕dx`(ẋ) shouldBeAbout `df2∕dx`(ẋ)
      `df1∕dx`(ẋ) shouldBeAbout manualDeriv(ẋ)
      `df2∕dx`(ẋ) shouldBeAbout manualDeriv(ẋ)
    }
  }

  "d(√x) / dx should be 1/(2√x)" {
    checkAll(DoubleGenerator) { ẋ ->
      val f1 = sqrt(x)
      val f2 = pow(x, 0.5)
      val manualDeriv = 1 / (2 * sqrt(x))
      val `df1∕dx` = d(f1) / d(x)
      val `df2∕dx` = d(f2) / d(x)
      `df1∕dx`(ẋ) shouldBeAbout `df2∕dx`(ẋ)
      `df1∕dx`(ẋ) shouldBeAbout manualDeriv(ẋ)
      `df2∕dx`(ẋ) shouldBeAbout manualDeriv(ẋ)
    }
  }

  "d(x¹) / dx should be 1".config(enabled = false) {
    checkAll(DoubleGenerator) { ẋ ->
      val f1 = x
      val f2 = pow(x, 1)
      val manualDeriv = 1
      val `df1∕dx` = d(f1) / d(x)
      val `df2∕dx` = d(f2) / d(x)
      `df1∕dx`(ẋ) shouldBeAbout `df2∕dx`(ẋ)
      `df1∕dx`(ẋ) shouldBeAbout manualDeriv(ẋ)
      `df2∕dx`(ẋ) shouldBeAbout manualDeriv(ẋ)
    }
  }

  "d(x⁰) / dx should be 0".config(enabled = false) {
    checkAll(DoubleGenerator(0)) { ẋ ->
      val f1 = x / x
      val f2 = pow(x, 0)
      val manualDeriv = 0
      val `df1∕dx` = d(f1) / d(x)
      val `df2∕dx` = d(f2) / d(x)
      `df1∕dx`(ẋ) shouldBeAbout `df2∕dx`(ẋ)
      `df1∕dx`(ẋ) shouldBeAbout manualDeriv(ẋ)
      `df2∕dx`(ẋ) shouldBeAbout manualDeriv(ẋ)
    }
  }
})