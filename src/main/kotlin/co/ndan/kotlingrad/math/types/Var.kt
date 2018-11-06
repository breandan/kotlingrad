package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.FieldPrototype
import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.calculus.Function

class Var<X : Field<X>>(private var name: String, override var value: X, val Prototype: FieldPrototype<X>) : Function<X>() {
  override val isVariable: Boolean
    get() = true

  override fun differentiate(arg: Var<X>) = if (this === arg) One(Prototype) else Zero(Prototype)

  override fun toString() = name

  override fun div(divisor: Function<X>) = if (divisor === this) One(Prototype) else super.times(divisor.inverse())
}