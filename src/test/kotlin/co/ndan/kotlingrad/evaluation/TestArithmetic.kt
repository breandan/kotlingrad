package co.ndan.kotlingrad.evaluation

import co.ndan.kotlingrad.calculus.DoubleVarGenerator
import co.ndan.kotlingrad.math.types.Double
import co.ndan.kotlingrad.math.types.Var
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

class TestArithmetic : StringSpec({
  "test addition" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x: Var<Double>, y: Var<Double> ->
      (x + y).value == x.value + y.value
    }
  }

  "test subtraction" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x: Var<Double>, y: Var<Double> ->
      (x - y).value == x.value - y.value
    }
  }

  "test unary minus" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x: Var<Double>, y: Var<Double> ->
      (-y + x).value == x.value - y.value
    }
  }

  "test multiplication" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x: Var<Double>, y: Var<Double> ->
      (x * y).value == x.value * y.value
    }
  }

  "test multiplication with numerical type" {
    assertAll(DoubleVarGenerator) {
      (it * 2).value == it.value * 2
    }
  }

  "test division" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x: Var<Double>, y: Var<Double> ->
      (x / y).value == x.value / y.value
    }
  }

  "test inverse" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x: Var<Double>, y: Var<Double> ->
      (x * y.inverse()).value == (x / y).value
    }
  }
})