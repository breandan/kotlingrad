package co.ndan.kotlingrad.calculus

import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import co.ndan.kotlingrad.math.calculus.DoubleFunctor
import co.ndan.kotlingrad.math.calculus.DoubleFunctor.variable
import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlin.math.cos
import kotlin.math.sin

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestFiniteDifferences: StringSpec({
  val epsilon = 1E-6
  val dx = 1E-8
  val x = variable("x")

  with(DoubleFunctor) {
    "test sin" {
      assertAll(DoubleGenerator) { xt ->
        val f = sin(x)
        val df_dx = d(f) / d(x)
        df_dx(x to xt).dbl shouldBe (((sin(xt.dbl + dx) - sin(xt.dbl)) / dx) plusOrMinus epsilon)
      }
    }

    "test cos" {
      assertAll(DoubleGenerator) { xt ->
        val f = cos(x)
        val df_dx = d(f) / d(x)
        df_dx(x to xt).dbl shouldBe (((cos(xt.dbl + dx) - cos(xt.dbl)) / dx) plusOrMinus epsilon)
      }
    }

    "test composition" {
      assertAll(DoubleGenerator) { xt ->
        val f = sin(x * x)
        val df_dx = d(f) / d(x)

        val xdx = xt.dbl + dx

        df_dx(x to xt).dbl shouldBe (((sin(xdx * xdx) - sin(xt.dbl * xt.dbl)) / dx) plusOrMinus epsilon)
      }
    }
  }
})