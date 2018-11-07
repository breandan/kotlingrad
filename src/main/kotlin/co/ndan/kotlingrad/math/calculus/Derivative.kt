package co.ndan.kotlingrad.math.calculus

import co.ndan.kotlingrad.math.types.Var
import co.ndan.kotlingrad.math.types.Double

class Derivative(val dividend: Function<Double>) {
  operator fun div(divisor: Var<Double>) = dividend.differentiate(divisor)
}