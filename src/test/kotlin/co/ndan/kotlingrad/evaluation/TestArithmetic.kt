package co.ndan.kotlingrad.evaluation

import co.ndan.kotlingrad.calculus.DoubleVarGenerator
import co.ndan.kotlingrad.math.calculus.DoubleFunctor.sin
import co.ndan.kotlingrad.math.types.Double
import co.ndan.kotlingrad.math.types.Var
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

class TestArithmetic: StringSpec({
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

  "test multiplication" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x: Var<Double>, y: Var<Double> ->
      (x * y).value == x.value * y.value
    }
  }

  "test multiplication with numerical type" {
    assertAll(DoubleVarGenerator) { x: Var<Double> ->
      sin(x * 2) * 2
      (x * 2).value == x.value * 2
    }
  }

  "test division" {
    assertAll(DoubleVarGenerator, DoubleVarGenerator) { x: Var<Double>, y: Var<Double> ->
      (x / y).value == x.value / y.value
    }
  }
})