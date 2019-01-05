package co.ndan.kotlingrad.evaluation

import co.ndan.kotlingrad.calculus.DoubleGenerator
import co.ndan.kotlingrad.math.calculus.DoubleFunctor
import co.ndan.kotlingrad.math.calculus.DoubleFunctor.variable
import co.ndan.kotlingrad.math.numerical.DoubleReal
import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TestArithmetic: StringSpec({
  val epsilon = 1E-10

  val x = variable("x", DoubleReal(1.0))
  val y = variable("y", DoubleReal(1.0))
  val z = variable("z", DoubleReal(1.0))

  "test addition" {
    assertAll(DoubleGenerator, DoubleGenerator) { xt, yt ->
      (x + y).invoke(x to DoubleReal(1.0), y to yt).dbl shouldBe ((yt + xt).dbl plusOrMinus epsilon)
    }
  }

  "test subtraction" {
    assertAll(DoubleGenerator, DoubleGenerator) { xt, yt ->
      (x - y)(x to xt, y to yt).dbl shouldBe ((xt - yt).dbl plusOrMinus epsilon)
    }
  }

  "test unary minus" {
    assertAll(DoubleGenerator, DoubleGenerator) { xt, yt ->
      (-y + x)(x to xt, y to yt).dbl shouldBe ((xt - yt).dbl plusOrMinus epsilon)
    }
  }

  "test multiplication" {
    assertAll(DoubleGenerator, DoubleGenerator) { xt, yt ->
      (x * y)(x to xt, y to yt).dbl shouldBe ((xt * yt).dbl plusOrMinus epsilon)
    }
  }

  "test multiplication with numerical type" {
    assertAll(DoubleGenerator) { xt ->
      (x * 2)(x to xt).dbl shouldBe (xt.dbl * 2 plusOrMinus epsilon)
    }
  }

//  "test division" {
//    assertAll(DoubleGenerator, DoubleGenerator) { x, y ->
//      (x / y)().dbl shouldBe ((x() / y()).dbl plusOrMinus epsilon)
//    }
//  }
//
//  "test inverse" {
//    assertAll(DoubleGenerator, DoubleGenerator) { x, y ->
//      (x * y.inverse())().dbl shouldBe ((x / y)().dbl plusOrMinus epsilon)
//    }
//  }

  "test associativity" {
    assertAll(DoubleGenerator, DoubleGenerator, DoubleGenerator) { xt, yt, zt ->
      (x * (y * z))(x to xt, y to yt, z to zt).dbl shouldBe (((x * y) * z)(x to xt, y to yt, z to zt).dbl plusOrMinus epsilon)
    }
  }

  "test commutativity" {
    assertAll(DoubleGenerator, DoubleGenerator, DoubleGenerator) { xt, yt, zt ->
      (x * y * z)(x to xt, y to yt, z to zt).dbl shouldBe ((z * y * x)(x to xt, y to yt, z to zt).dbl plusOrMinus epsilon)
    }
  }

  "test distributivity" {
    assertAll(DoubleGenerator, DoubleGenerator, DoubleGenerator) { xt, yt, zt ->
      (x * (y + z))(x to xt, y to yt, z to zt).dbl shouldBe ((x * y + x * z)(x to xt, y to yt, z to zt).dbl plusOrMinus epsilon)
    }
  }

  "test functional" {
    assertAll(DoubleGenerator) { xt ->
      with(DoubleFunctor) {
        val f = x * x
        val q = variable("c", 1)
//      f(q())

      }
    }
  }
})