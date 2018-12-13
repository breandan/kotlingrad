package co.ndan.kotlingrad.calculus

import co.ndan.kotlingrad.math.algebra.DoublePrototype
import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import co.ndan.kotlingrad.math.calculus.DoubleFunctor.cos
import co.ndan.kotlingrad.math.calculus.DoubleFunctor.sin
import co.ndan.kotlingrad.math.calculus.RealFunctor
import co.ndan.kotlingrad.math.types.Double
import co.ndan.kotlingrad.math.types.Var
import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters")
class TestTrigonometricDerivatives: StringSpec({
  "d(sin(x)) / dx should be cos(x)" {
    assertAll(DoubleVarGenerator) { x: Var<Double> ->
      (d(sin(x)) / d(x)).value.dbl shouldBe (cos(x) * 1).value.dbl
    }
  }

  "d(cos(x)) / dx should be -sin(x)" {
    assertAll(DoubleVarGenerator) { x: Var<Double> ->
      (d(cos(x)) / d(x)).value.dbl shouldBe (sin(x) * -1).value.dbl
    }
  }

  val rft = RealFunctor(DoublePrototype)
  val x = rft.variable("x", Double(1.0))
  val y = rft.variable("y", Double(1.0))

  val z = y * (rft.sin(x * y) - x)
  val `∂z∕∂x` = d(z) / d(x)
  val `∂z∕∂y` = d(z) / d(y)
  val `∂²z∕∂x²` = d(`∂z∕∂x`) / d(x)
  val `∂²z∕∂x∂y` = d(`∂z∕∂x`) / d(y)

  val epsilon = 1E-20

  "test z" {
    assertAll { kx: kotlin.Double, ky: kotlin.Double ->
      x.value = Double(kx)
      y.value = Double(ky)

      val numericalAnswer = ky * (kotlin.math.sin(kx * ky) - kx) + 0.0

      z.value.dbl shouldBe numericalAnswer
    }
  }

  "test ∂z/∂x" {
    assertAll { cx: kotlin.Double, cy: kotlin.Double ->
      if (cx.isFinite() && cy.isFinite() && !cx.isNaN() && cy.isNaN()) {
        x.value = Double(cx)
        y.value = Double(cy)

        `∂z∕∂x`.value.dbl shouldBe ((cy * (kotlin.math.cos(cx * cy) * cy - 1)) plusOrMinus epsilon)
      } else 1.0 shouldBe 1.0
    }
  }

  "test ∂z/∂y" {
    assertAll { cx: kotlin.Double, cy: kotlin.Double ->
      if (cx.isFinite() && cy.isFinite() && !cx.isNaN() && cy.isNaN()) {
        x.value = Double(cx)
        y.value = Double(cy)

        `∂z∕∂y`.value.dbl shouldBe ((kotlin.math.sin(cx * cy) - cx + cy * kotlin.math.cos(cx * cy) * cx) plusOrMinus epsilon)
      } else 1.0 shouldBe 1.0
    }
  }

  "test ∂²z/∂x²" {
    assertAll { cx: kotlin.Double, cy: kotlin.Double ->
      if (cx.isFinite() && cy.isFinite() && !cx.isNaN() && cy.isNaN()) {
        x.value = Double(cx)
        y.value = Double(cy)

        `∂z∕∂x`.value.dbl shouldBe -cy * cy * cy * (kotlin.math.sin(cx * cy)) + 0.0
      } else
        1.0 shouldBe 1.0
    }
  }

  "test ∂²z/∂x∂y" {
    assertAll { cx: kotlin.Double, cy: kotlin.Double ->
      if (cx.isFinite() && cy.isFinite() && !cx.isNaN() && cy.isNaN()) {
        x.value = Double(cx)
        y.value = Double(cy)

        `∂²z∕∂x∂y`.value.dbl shouldBe ((kotlin.math.cos(cx * cy) - cy * kotlin.math.sin(cx * cy) - 1) plusOrMinus epsilon)
      } else
        1.0 shouldBe 1.0
    }
  }
})