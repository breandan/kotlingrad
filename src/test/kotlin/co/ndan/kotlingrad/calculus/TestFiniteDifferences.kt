package co.ndan.kotlingrad.calculus

import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import co.ndan.kotlingrad.math.calculus.DoubleFunctor.cos
import co.ndan.kotlingrad.math.calculus.DoubleFunctor.sin
import co.ndan.kotlingrad.math.types.Double
import co.ndan.kotlingrad.math.types.Var
import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlin.math.cos
import kotlin.math.sin

class TestFiniteDifferences: StringSpec({
  val epsilon = 0.001
  val dx = 0.001

  "test sin" {
    assertAll(DoubleVarGenerator) { x: Var<Double> ->
      val dblVal = x.value.dbl
      (d(sin(x)) / d(x)).value.dbl shouldBe
        (((sin(dblVal + dx) - sin(dblVal)) / dx) plusOrMinus epsilon)
    }
  }

  "test cos" {
    assertAll(DoubleVarGenerator) { x: Var<Double> ->
      val dblVal = x.value.dbl
      (d(cos(x)) / d(x)).value.dbl shouldBe
        (((cos(dblVal + dx) - cos(dblVal)) / dx) plusOrMinus epsilon)
    }
  }
})