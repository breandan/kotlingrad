package co.ndan.kotlingrad.math.calculus

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.types.Var

interface Differentiable<X : Field<X>, D> {
  fun differentiate(arg: Var<X>): D
}
