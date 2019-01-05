package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.math.calculus.DoubleFunctor
import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestGradient: StringSpec({
  with(DoubleFunctor) {
    val x = variable("x")
    val y = variable("x")

    val z = y * (sin(x * y) - x)

    val ε = 1E-15

    val `∇z` = z.grad()

    "test ∇z" {
      assertAll(DoubleGenerator, DoubleGenerator) { xVal, yVal ->
        val vals = mapOf(x to xVal, y to yVal)

        val `∂z∕∂x` = y * (cos(x * y) * y - one)
        val `∂z∕∂y` = sin(x * y) - x + y * cos(x * y) * x

        `∇z`[x]!!(vals).dbl shouldBe (`∂z∕∂x`(vals).dbl plusOrMinus ε)
        `∇z`[y]!!(vals).dbl shouldBe (`∂z∕∂y`(vals).dbl plusOrMinus ε)
      }
    }
  }
})