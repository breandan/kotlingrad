package co.ndan.kotlingrad.evaluation

import co.ndan.kotlingrad.calculus.DoubleVarGenerator
import co.ndan.kotlingrad.math.calculus.DoubleFunctor
import co.ndan.kotlingrad.math.functions.Function
import co.ndan.kotlingrad.math.numerical.Double
import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TestArithmetic: StringSpec({
  val epsilon = 1E-10

  "test addition" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x, y ->
      (x + y)().dbl shouldBe ((x() + y()).dbl plusOrMinus epsilon)
    }
  }

  "test subtraction" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x, y ->
      (x - y)().dbl shouldBe ((x() - y()).dbl plusOrMinus epsilon)
    }
  }

  "test unary minus" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x, y ->
      (-y + x)().dbl shouldBe ((x - y)().dbl plusOrMinus epsilon)
    }
  }

  "test multiplication" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x, y ->
      (x * y)().dbl shouldBe ((x() * y()).dbl plusOrMinus epsilon)
    }
  }

  "test multiplication with numerical type" {
    assertAll(DoubleVarGenerator) {
      (it * 2)().dbl shouldBe (it().dbl * 2 plusOrMinus epsilon)
    }
  }

//  "test division" {
//    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x, y ->
//      (x / y)().dbl shouldBe ((x() / y()).dbl plusOrMinus epsilon)
//    }
//  }
//
//  "test inverse" {
//    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x, y ->
//      (x * y.inverse())().dbl shouldBe ((x / y)().dbl plusOrMinus epsilon)
//    }
//  }

  "test associativity" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator, DoubleVarGenerator) { x, y, z ->
      (x * (y * z))().dbl shouldBe (((x * y) * z)().dbl plusOrMinus epsilon)
    }
  }

  "test commutativity" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator, DoubleVarGenerator) { x, y, z ->
      (x * y * z)().dbl shouldBe ((z * y * x)().dbl plusOrMinus epsilon)
    }
  }

  "test distributivity" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator, DoubleVarGenerator) { x, y, z ->
      (x * (y + z))().dbl shouldBe ((x * y + x * z)().dbl plusOrMinus epsilon)
    }
  }

  "test functional" {
    assertAll(DoubleVarGenerator) { x ->
      with(DoubleFunctor) {
        val f: Function<Double> = x * x
        val q = variable("c", 1)
//      f(q())
        println(((q * f).inverse()()).dbl)

      }
    }
  }
})