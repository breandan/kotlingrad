package ai.hypergraph.kotlingrad.calculus

import ai.hypergraph.kotlingrad.*
import ai.hypergraph.kotlingrad.api.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestGradient: StringSpec({
  val ε = 1E-15
  val x by SVar(DReal)
  val y by SVar(DReal)

  val z = y * (sin(x * y) - x)
  println(z)
  val `∇z` = z.grad()

  "test ∇z" {
    checkAll(DoubleGenerator, DoubleGenerator) { ẋ: Double, ẏ: Double ->
      val vals = arrayOf(x to ẋ, y to ẏ)

      val `∂z∕∂x` = y * (cos(x * y) * y - 1)
      val `∂z∕∂y` = sin(x * y) - x + y * cos(x * y) * x

      `∇z`[x]!!(*vals) shouldBeAbout `∂z∕∂x`(*vals)
      `∇z`[y]!!(*vals) shouldBeAbout `∂z∕∂y`(*vals)
    }
  }
})