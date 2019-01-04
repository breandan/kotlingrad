package co.ndan.kotlingrad.calculus

import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import co.ndan.kotlingrad.math.calculus.DoubleFunctor
import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlin.math.cos
import kotlin.math.sin

class TestFiniteDifferences: StringSpec({
  val epsilon = 1E-6
  val dx = 1E-8

  with(DoubleFunctor) {
    "test sin" {
      assertAll(DoubleVarGenerator) { x ->
        val dblVal = x().dbl
        val fn = sin(x)
        (d(fn) / d(x))().dbl shouldBe
          (((sin(dblVal + dx) - sin(dblVal)) / dx) plusOrMinus epsilon)
      }
    }

    "test cos" {
      assertAll(DoubleVarGenerator) { x ->
        val dblVal = x().dbl
        val fn = cos(x)
        (d(fn) / d(x))().dbl shouldBe
          (((cos(dblVal + dx) - cos(dblVal)) / dx) plusOrMinus epsilon)
      }
    }

    "test composition" {
      assertAll(DoubleVarGenerator) { x ->
        val dblVal = x().dbl
        val fn = sin(x * x)
        val xdx = dblVal + dx
        (d(fn) / d(x))().dbl shouldBe
          (((sin(xdx * xdx) - sin(dblVal * dblVal)) / dx) plusOrMinus epsilon)
      }
    }
  }
})