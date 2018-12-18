package co.ndan.kotlingrad.evaluation

import co.ndan.kotlingrad.calculus.DoubleVarGenerator
import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.properties.assertAll
import co.ndan.kotlingrad.math.calculus.DoubleFunctor.variable
import co.ndan.kotlingrad.math.calculus.Function
import co.ndan.kotlingrad.math.types.Double
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TestArithmetic: StringSpec({
  val epsilon = 1E-10

  "test addition" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x, y ->
      (x + y).value.dbl shouldBe ((x.value + y.value).dbl plusOrMinus epsilon)
    }
  }

  "test subtraction" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x, y ->
      (x - y).value.dbl shouldBe ((x.value - y.value).dbl plusOrMinus epsilon)
    }
  }

  "test unary minus" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x, y ->
      (-y + x).value.dbl shouldBe ((x - y).value.dbl plusOrMinus epsilon)
    }
  }

  "test multiplication" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x, y ->
      (x * y).value.dbl shouldBe ((x.value * y.value).dbl plusOrMinus epsilon)
    }
  }

  "test multiplication with numerical type" {
    assertAll(DoubleVarGenerator) {
      (it * 2).value.dbl shouldBe (it.value.dbl * 2 plusOrMinus epsilon)
    }
  }

//  "test division" {
//    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x, y ->
//      (x / y).value.dbl shouldBe ((x.value / y.value).dbl plusOrMinus epsilon)
//    }
//  }
//
//  "test inverse" {
//    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x, y ->
//      (x * y.inverse()).value.dbl shouldBe ((x / y).value.dbl plusOrMinus epsilon)
//    }
//  }

  "test associativity" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator, DoubleVarGenerator) { x, y, z ->
      (x * (y * z)).value.dbl shouldBe (((x * y) * z).value.dbl plusOrMinus epsilon)
    }
  }

  "test commutativity" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator, DoubleVarGenerator) { x, y, z ->
      (x * y * z).value.dbl shouldBe ((z * y * x).value.dbl plusOrMinus epsilon)
    }
  }

  "test distributivity" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator, DoubleVarGenerator) { x, y, z ->
      (x * (y + z)).value.dbl shouldBe ((x * y + x * z).value.dbl plusOrMinus epsilon)
    }
  }

  "test functional" {
    assertAll(DoubleVarGenerator) { x ->
      val f: Function<Double> = x * x
      val q = variable("c", Double(1.0))
//      f(q.value)
      println(((q * f).inverse().value).dbl)

    }
  }
})