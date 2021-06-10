package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.*
import edu.umontreal.kotlingrad.api.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll
import kotlin.math.*

@kotlin.time.ExperimentalTime
@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestFiniteDifferences: StringSpec({
  val dx = 1E-20
  val x by SVar(DReal)

  "sin should be (sin(x + dx) - sin(x)) / dx".config(enabled = false) {
    checkAll(DoubleGenerator) { ẋ ->
      val f = sin(x)
      val `df∕dx` = d(f) / d(x)
      `df∕dx`(ẋ) shouldBeAbout (sin(ẋ + dx) - sin(ẋ)) / dx
    }
  }

  "cos should be (cos(x + dx) - cos(x)) / dx".config(enabled = false) {
    checkAll(DoubleGenerator) { ẋ ->
      val f = cos(x)
      val `df∕dx` = d(f) / d(x)
      `df∕dx`(ẋ) shouldBeAbout ((cos(ẋ + dx) - cos(ẋ)) / dx)
    }
  }

  "test composition".config(enabled = false) {
    checkAll(DoubleGenerator) { ẋ ->
      val f = sin(pow(x, 2))
      val `df∕dx` = d(f) / d(x)

      val eps = ẋ + dx

      `df∕dx`(ẋ) shouldBeAbout (sin(eps * eps) - sin(ẋ * ẋ)) / dx
    }
  }
})