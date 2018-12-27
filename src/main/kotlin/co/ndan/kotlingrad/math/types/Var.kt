package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.*
import co.ndan.kotlingrad.math.functions.Function
import co.ndan.kotlingrad.math.numerical.Double

open class Var<X: Field<X>>(private var name: String, override var value: X, val prototype: FieldPrototype<X>): Function<X>() {
  override fun differentiate(ind: Var<X>) = if (this === ind) One(prototype) else Zero(prototype)

  override fun toString() = "$name:$value"

  override fun div(divisor: Function<X>) = if (divisor === this) One(prototype) else this * divisor.inverse()
}

object Variable {
  operator fun invoke(name: String, value: Double) = Var(name, value, DoublePrototype)
  //...
}
