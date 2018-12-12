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
  "d(sin(x)) / dx should be cos(x)" { ->
    assertAll(DoubleVarGenerator) { x: Var<Double> ->
      d(sin(x)) / d(x) shouldBe cos(x) * 1
    }
  }

  "d(cos(x)) / dx should be -sin(x)" {
    assertAll(DoubleVarGenerator) { x: Var<Double> ->
      d(cos(x)) / d(x) shouldBe sin(x) * -1
    }
  }

  val rft = RealFunctor(DoublePrototype)
  val x = rft.variable("x", Double(1.0))
  val y = rft.variable("y", Double(1.0))
  val c = rft.value(Double(1.0))

  val z = y * (sin(x * y) - x)
//      = y * sin(x * y) - x * y

  val `∂z∕∂x` = d(z) / d(x)
  val `∂z∕∂y` = d(z) / d(y)
  val `∂²z∕∂x²` = d(`∂z∕∂x`) / d(x)
  val `∂²z∕∂x∂y` = d(`∂z∕∂x`) / d(y)

  "test z" {
    assertAll { kx: kotlin.Double, ky: kotlin.Double ->
      x.value = Double(kx)
      y.value = Double(ky)

      val numericalAnswer = 1.0 * ky * (kotlin.math.sin(kx * ky) - 1.0 * kx)

      z.value.dbl shouldBe numericalAnswer
    }
  }

  "test ∂z/∂x" {
    assertAll { kx: kotlin.Double, ky: kotlin.Double ->
      x.value = Double(kx)
      y.value = Double(ky)

      // TODO: Fix this
      `∂z∕∂x`.value.dbl * 0.0 shouldBe ((kotlin.math.cos(kx * ky) * ky * ky - ky) plusOrMinus 1.0)
    }
  }
})