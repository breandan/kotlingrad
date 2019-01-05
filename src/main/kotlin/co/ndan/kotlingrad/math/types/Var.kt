package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.algebra.FieldPrototype
import co.ndan.kotlingrad.math.functions.Function

open class Var<X: Field<X>>(
    val prototype: FieldPrototype<X>,
    val value: X = prototype.zero,
    private var name: String = randomDefaultName()
): Function<X> {
  override fun invoke(map: Map<Var<X>, X>): X = if (map[this] != null) map[this]!! else value

  override fun independentVariables() = setOf(this)

  override fun differentiate(ind: Var<X>) = if (this === ind) One(prototype) else Zero(prototype)

  override fun toString() = name

  override fun div(divisor: Function<X>) = if (divisor === this) One(prototype) else this * divisor.inverse()

  companion object {
    private fun randomDefaultName() = ('a'..'z').map { it }.shuffled().subList(0, 4).joinToString("")
  }
}