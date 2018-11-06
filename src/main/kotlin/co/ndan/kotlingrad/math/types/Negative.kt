package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.calculus.Function

class Negative<X : Field<X>>(override val fnx: Function<X>) : UnaryFunction<X>(fnx) {
  override val value: X
    get() = -fnx.value

  override fun differentiate(arg: Var<X>) = -fnx.differentiate(arg)

  override fun toString() = "-$fnx"

  override fun unaryMinus() = fnx
}
