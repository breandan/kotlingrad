package co.ndan.kotlingrad.calculus

import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import co.ndan.kotlingrad.math.calculus.DoubleFunctor
import co.ndan.kotlingrad.math.numerical.Double
import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestTrigonometricDerivatives : StringSpec({
  with(DoubleFunctor) {
    "d(sin(x)) / dx should be cos(x)" {
      assertAll(DoubleVarGenerator) {
        (d(sin(it)) / d(it))().dbl shouldBe cos(it)().dbl
      }
    }

    "d(cos(x)) / dx should be -sin(x)" {
      assertAll(DoubleVarGenerator) {
        (d(cos(it)) / d(it))().dbl shouldBe (-sin(it))().dbl
      }
    }

    val x = variable("x", Double(1.0))
    val y = variable("y", Double(1.0))

    val z = y * (sin(x * y) - x)
    val `∂z∕∂x` = d(z) / d(x)
    val `∂z∕∂y` = d(z) / d(y)
    val `∂²z∕∂x²` = d(`∂z∕∂x`) / d(x)
    val `∂²z∕∂x∂y` = d(`∂z∕∂x`) / d(y)
    val one = variable("c", Double(1.0))

    val epsilon = 1E-10

    "test z" {
      assertAll { kx: kotlin.Double, ky: kotlin.Double ->
        x.value = Double(kx)
        y.value = Double(ky)

        val numericalAnswer = ky * (kotlin.math.sin(kx * ky) - kx) + 0.0

        z().dbl shouldBe numericalAnswer
      }
    }

    "test ∂z/∂x" {
      assertAll(DoubleVarGenerator, DoubleVarGenerator) { cx, cy ->
        x.value = Double(cx.value.dbl)
        y.value = Double(cy.value.dbl)

        `∂z∕∂x`().dbl shouldBe ((cy * (cos(cx * cy) * cy - one))().dbl plusOrMinus epsilon)
      }
    }

    "test ∂z/∂y" {
      assertAll(DoubleVarGenerator, DoubleVarGenerator) { cx, cy ->
        x.value = Double(cx.value.dbl)
        y.value = Double(cy.value.dbl)

        `∂z∕∂y`().dbl shouldBe ((sin(cx * cy) - cx + cy * cos(cx * cy) * cx)().dbl plusOrMinus epsilon)
      }
    }

    "test ∂²z/∂x²" {
      assertAll(DoubleVarGenerator, DoubleVarGenerator) { cx, cy ->
        x.value = Double(cx.value.dbl)
        y.value = Double(cy.value.dbl)

        `∂²z∕∂x²`().dbl shouldBe ((-cy * cy * cy * sin(cx * cy))().dbl plusOrMinus epsilon)
      }
    }

    "test ∂²z/∂x∂y" {
      assertAll(DoubleVarGenerator, DoubleVarGenerator) { cx, cy ->
        x.value = Double(cx.value.dbl)
        y.value = Double(cy.value.dbl)

        `∂²z∕∂x∂y`().dbl shouldBe ((cos(cx * cy) * cy - one + cy * (cos(cx * cy) - cy * cx * sin(cx * cy)))().dbl plusOrMinus epsilon)
      }
    }
  }
})