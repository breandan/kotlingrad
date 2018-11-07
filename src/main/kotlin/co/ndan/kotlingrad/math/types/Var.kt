package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.FieldPrototype
import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.calculus.Function

class Var<X : Field<X>>(private var name: String, override var value: X, val prototype: FieldPrototype<X>) : Function<X>() {
  override fun differentiate(ind: Var<X>) = if (this === ind) One(prototype) else Zero(prototype)

  override fun toString() = name

  override fun div(divisor: Function<X>) = if (divisor === this) One(prototype) else this * divisor.inverse()
}