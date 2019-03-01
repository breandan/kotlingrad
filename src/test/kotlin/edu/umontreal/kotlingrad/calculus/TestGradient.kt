package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.numerical.DoublePrecision
import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestGradient: StringSpec({
  with(DoublePrecision) {
    val ε = 1E-15
    val x = Var("x")
    val y = Var("x")

    val z = y * (sin(x * y) - x)
    val `∇z` = z.grad()

    "test ∇z" {
      NumericalGenerator.assertAll { ẋ, ẏ ->
        val vals = mapOf(x to ẋ, y to ẏ)

        val `∂z∕∂x` = y * (cos(x * y) * y - 1)
        val `∂z∕∂y` = sin(x * y) - x + y * cos(x * y) * x

        `∇z`[x]!!(vals) shouldBeAbout `∂z∕∂x`(vals)
        `∇z`[y]!!(vals) shouldBeAbout `∂z∕∂y`(vals)
      }
    }
  }
})