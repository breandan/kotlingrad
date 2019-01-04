package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.DoublePrototype
import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.algebra.FieldPrototype
import co.ndan.kotlingrad.math.functions.Function
import co.ndan.kotlingrad.math.numerical.Double

open class Var<X: Field<X>>(private var name: String, val prototype: FieldPrototype<X>, var value: X = prototype.zero): Function<X> {
  override fun invoke(map: Map<Var<X>, X>): X = if (map[this] != null) map[this]!! else value

  override fun differentiate(ind: Var<X>) = if (this === ind) One(prototype) else Zero(prototype)

  override fun toString() = "$name:$value"

  override fun div(divisor: Function<X>) = if (divisor === this) One(prototype) else this * divisor.inverse()
}

object Variable {
  operator fun invoke(name: String) = Var(name, DoublePrototype)
  //...
}