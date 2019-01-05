package edu.umontreal.kotlingrad.evaluation

import edu.umontreal.kotlingrad.calculus.DoubleGenerator
import edu.umontreal.kotlingrad.math.calculus.DoubleFunctor
import edu.umontreal.kotlingrad.math.calculus.DoubleFunctor.variable
import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TestArithmetic: StringSpec({
  val ε = 1E-10

  val x = variable("x")
  val y = variable("y")
  val z = variable("z")

  "test addition" {
    assertAll(DoubleGenerator, DoubleGenerator) { xVal, yVal ->
      (x + y).invoke(x to xVal, y to yVal).dbl shouldBe ((yVal + xVal).dbl plusOrMinus ε)
    }
  }

  "test subtraction" {
    assertAll(DoubleGenerator, DoubleGenerator) { xVal, yVal ->
      (x - y)(x to xVal, y to yVal).dbl shouldBe ((xVal - yVal).dbl plusOrMinus ε)
    }
  }

  "test unary minus" {
    assertAll(DoubleGenerator, DoubleGenerator) { xVal, yVal ->
      (-y + x)(x to xVal, y to yVal).dbl shouldBe ((xVal - yVal).dbl plusOrMinus ε)
    }
  }

  "test multiplication" {
    assertAll(DoubleGenerator, DoubleGenerator) { xVal, yVal ->
      (x * y)(x to xVal, y to yVal).dbl shouldBe ((xVal * yVal).dbl plusOrMinus ε)
    }
  }

  "test multiplication with numerical type" {
    assertAll(DoubleGenerator) { xVal ->
      (x * 2)(x to xVal).dbl shouldBe (xVal.dbl * 2 plusOrMinus ε)
    }
  }

//  "test division" {
//    assertAll(DoubleGenerator, DoubleGenerator) { x, y ->
//      (x / y)().dbl shouldBe ((x() / y()).dbl plusOrMinus ε)
//    }
//  }
//
//  "test inverse" {
//    assertAll(DoubleGenerator, DoubleGenerator) { x, y ->
//      (x * y.inverse())().dbl shouldBe ((x / y)().dbl plusOrMinus ε)
//    }
//  }

  "test associativity" {
    assertAll(DoubleGenerator, DoubleGenerator, DoubleGenerator) { xVal, yVal, zVal ->
      (x * (y * z))(x to xVal, y to yVal, z to zVal).dbl shouldBe (((x * y) * z)(x to xVal, y to yVal, z to zVal).dbl plusOrMinus ε)
    }
  }

  "test commutativity" {
    assertAll(DoubleGenerator, DoubleGenerator, DoubleGenerator) { xVal, yVal, zVal ->
      (x * y * z)(x to xVal, y to yVal, z to zVal).dbl shouldBe ((z * y * x)(x to xVal, y to yVal, z to zVal).dbl plusOrMinus ε)
    }
  }

  "test distributivity" {
    assertAll(DoubleGenerator, DoubleGenerator, DoubleGenerator) { xVal, yVal, zVal ->
      (x * (y + z))(x to xVal, y to yVal, z to zVal).dbl shouldBe ((x * y + x * z)(x to xVal, y to yVal, z to zVal).dbl plusOrMinus ε)
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