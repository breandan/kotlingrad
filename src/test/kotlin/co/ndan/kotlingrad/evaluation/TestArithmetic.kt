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
    assertAll(DoubleGenerator, DoubleGenerator) { xVal, yVal ->
      (x + y).invoke(x to DoubleReal(1.0), y to yVal).dbl shouldBe ((yVal + xVal).dbl plusOrMinus epsilon)
    }
  }

  "test subtraction" {
    assertAll(DoubleGenerator, DoubleGenerator) { xVal, yVal ->
      (x - y)(x to xVal, y to yVal).dbl shouldBe ((xVal - yVal).dbl plusOrMinus epsilon)
    }
  }

  "test unary minus" {
    assertAll(DoubleGenerator, DoubleGenerator) { xVal, yVal ->
      (-y + x)(x to xVal, y to yVal).dbl shouldBe ((xVal - yVal).dbl plusOrMinus epsilon)
    }
  }

  "test multiplication" {
    assertAll(DoubleGenerator, DoubleGenerator) { xVal, yVal ->
      (x * y)(x to xVal, y to yVal).dbl shouldBe ((xVal * yVal).dbl plusOrMinus epsilon)
    }
  }

  "test multiplication with numerical type" {
    assertAll(DoubleGenerator) { xVal ->
      (x * 2)(x to xVal).dbl shouldBe (xVal.dbl * 2 plusOrMinus epsilon)
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
    assertAll(DoubleGenerator, DoubleGenerator, DoubleGenerator) { xVal, yVal, zVal ->
      (x * (y * z))(x to xVal, y to yVal, z to zVal).dbl shouldBe (((x * y) * z)(x to xVal, y to yVal, z to zVal).dbl plusOrMinus epsilon)
    }
  }

  "test commutativity" {
    assertAll(DoubleGenerator, DoubleGenerator, DoubleGenerator) { xVal, yVal, zVal ->
      (x * y * z)(x to xVal, y to yVal, z to zVal).dbl shouldBe ((z * y * x)(x to xVal, y to yVal, z to zVal).dbl plusOrMinus epsilon)
    }
  }

  "test distributivity" {
    assertAll(DoubleGenerator, DoubleGenerator, DoubleGenerator) { xVal, yVal, zVal ->
      (x * (y + z))(x to xVal, y to yVal, z to zVal).dbl shouldBe ((x * y + x * z)(x to xVal, y to yVal, z to zVal).dbl plusOrMinus epsilon)
    }
  }

  "test functional" {
    assertAll(DoubleGenerator) { xVal ->
      with(DoubleFunctor) {
        val f = x * x
        val q = variable("c", 1)
//      f(q())

      }
    }
  }
})