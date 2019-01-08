package edu.umontreal.kotlingrad.evaluation

import edu.umontreal.kotlingrad.calculus.DoubleFunctor
import edu.umontreal.kotlingrad.calculus.DoubleRealGenerator
import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TestArithmetic: StringSpec({
  with(DoubleFunctor) {
    val ε = 1E-10
    val x = variable("x")
    val y = variable("y")
    val z = variable("z")

    "test addition" {
      assertAll(DoubleRealGenerator, DoubleRealGenerator) { xVal, yVal ->
        (x + y).invoke(x to xVal, y to yVal).dbl shouldBe ((yVal + xVal).dbl plusOrMinus ε)
      }
    }

    "test subtraction" {
      assertAll(DoubleRealGenerator, DoubleRealGenerator) { xVal, yVal ->
        (x - y)(x to xVal, y to yVal).dbl shouldBe ((xVal - yVal).dbl plusOrMinus ε)
      }
    }

    "test unary minus" {
      assertAll(DoubleRealGenerator, DoubleRealGenerator) { xVal, yVal ->
        (-y + x)(x to xVal, y to yVal).dbl shouldBe ((xVal - yVal).dbl plusOrMinus ε)
      }
    }

    "test multiplication" {
      assertAll(DoubleRealGenerator, DoubleRealGenerator) { xVal, yVal ->
        (x * y)(x to xVal, y to yVal).dbl shouldBe ((xVal * yVal).dbl plusOrMinus ε)
      }
    }

    "test multiplication with numerical type" {
      assertAll(DoubleRealGenerator) { xVal ->
        (x * 2)(x to xVal).dbl shouldBe (xVal.dbl * 2 plusOrMinus ε)
      }
    }

//  "test division" {
//    assertAll(DoubleRealGenerator, DoubleRealGenerator) { x, y ->
//      (x / y)().dbl shouldBe ((x() / y()).dbl plusOrMinus ε)
//    }
//  }
//
//  "test inverse" {
//    assertAll(DoubleRealGenerator, DoubleRealGenerator) { x, y ->
//      (x * y.inverse())().dbl shouldBe ((x / y)().dbl plusOrMinus ε)
//    }
//  }

    "test associativity" {
      assertAll(DoubleRealGenerator, DoubleRealGenerator, DoubleRealGenerator) { xVal, yVal, zVal ->
        val values = mapOf(x to xVal, y to yVal, z to zVal)
        (x * (y * z))(values).dbl shouldBe (((x * y) * z)(values).dbl plusOrMinus ε)
      }
    }

    "test commutativity" {
      assertAll(DoubleRealGenerator, DoubleRealGenerator, DoubleRealGenerator) { xVal, yVal, zVal ->
        val values = mapOf(x to xVal, y to yVal, z to zVal)
        (x * y * z)(values).dbl shouldBe ((z * y * x)(values).dbl plusOrMinus ε)
      }
    }

    "test distributivity" {
      assertAll(DoubleRealGenerator, DoubleRealGenerator, DoubleRealGenerator) { xVal, yVal, zVal ->
        val values = mapOf(x to xVal, y to yVal, z to zVal)
        (x * (y + z))(values).dbl shouldBe ((x * y + x * z)(values).dbl plusOrMinus ε)
      }
    }

    "test functional" {
      assertAll(DoubleRealGenerator) { xVal ->
        with(DoubleFunctor) {
          val f = pow(x, 2)
          val q = variable("c", 1)
//      f(q())

        }
      }
    }
  }
})