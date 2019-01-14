package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.functions.Function
import edu.umontreal.kotlingrad.functions.Var
import edu.umontreal.kotlingrad.numerical.BigDecimalReal
import edu.umontreal.kotlingrad.numerical.ProtoBigDecimal

object BigDecimalFunctor: RealFunctor<BigDecimalReal>(ProtoBigDecimal) {
  fun variable(default: Number) = Var(rfc, BigDecimalReal(default.toDouble()))

  fun variable(name: String, default: Number) =
    Var(rfc, BigDecimalReal(default.toDouble()), name)

  fun pow(base: Function<BigDecimalReal>, number: Int) =
    pow(base, value(BigDecimalReal(number)))

  operator fun Function<BigDecimalReal>.invoke(pairs: Map<Var<BigDecimalReal>, Number>) =
    this(pairs.map { (it.key to BigDecimalReal(it.value)) }.toMap()).bg

  operator fun Function<BigDecimalReal>.invoke(vararg pairs: Pair<Var<BigDecimalReal>, Number>) =
    this(pairs.map { (it.first to BigDecimalReal(it.second)) }.toMap()).bg

  operator fun Function<BigDecimalReal>.invoke(vararg number: Number) =
    this(variables.zip(number).toMap())

  // TODO: Lift these into RealFunctor, perhaps extending from Field?
  operator fun Function<BigDecimalReal>.plus(number: Number) = this + value(BigDecimalReal(number))

  operator fun Number.plus(fn: Function<BigDecimalReal>) = fn + this

  operator fun Function<BigDecimalReal>.minus(number: Number) = this - value(BigDecimalReal(number))
  operator fun Number.minus(fn: Function<BigDecimalReal>) = fn + -toDouble()

  operator fun Function<BigDecimalReal>.times(number: Number) = this * value(BigDecimalReal(number))
  operator fun Number.times(fn: Function<BigDecimalReal>) = fn * this

  operator fun Function<BigDecimalReal>.div(number: Number) = this / value(BigDecimalReal(number))
  operator fun Number.div(fn: Function<BigDecimalReal>): Function<BigDecimalReal> = this * fn.inverse()
}