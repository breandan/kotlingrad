package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.*
import edu.umontreal.kotlingrad.api.*
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestGradient: StringSpec({
  val ε = 1E-15
  val x by SVar(DReal)
  val y by SVar(DReal)

  val z = y * (sin(x * y) - x)
  val `∇z` = z.grad()

  "test ∇z" {
    DoubleGenerator.assertAll { ẋ, ẏ ->
      val vals = arrayOf(x to ẋ, y to ẏ)

      val `∂z∕∂x` = y * (cos(x * y) * y - 1)
      val `∂z∕∂y` = sin(x * y) - x + y * cos(x * y) * x

      `∇z`[x]!!(*vals) shouldBeAbout `∂z∕∂x`(*vals)
      `∇z`[y]!!(*vals) shouldBeAbout `∂z∕∂y`(*vals)
    }
  }
})