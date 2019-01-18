package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.calculus.Differentiable
import edu.umontreal.kotlingrad.numerical.FieldPrototype
import edu.umontreal.kotlingrad.utils.randomDefaultName

sealed class Function<X: Field<X>>(open val variables: Set<Var<X>>):
  Field<Function<X>>, Differentiable<X, Function<X>>, kotlin.Function<X> {
  open val name: String = randomDefaultName()

  operator fun invoke(map: Map<Var<X>, X> = emptyMap()): X = when (this) {
    is Var -> if (map[this] != null) map[this]!! else value
    is Exp -> prototype.exp(exponent(map))
    is Log -> prototype.log(logarithmand(map))
    is Negative -> -arg(map)
    is Power -> base.invoke(map).pow(exponent(map))
    is SquareRoot -> prototype.sqrt(radicand(map))
    is Sine -> prototype.sin(angle(map))
    is Cosine -> prototype.cos(angle(map))
    is Tangent -> prototype.tan(angle(map))
    is Product -> multiplicator(map) * multiplicand(map)
    is Sum -> augend(map) + addend(map)
    is Const -> value
  }

  operator fun invoke(vararg pair: Pair<Var<X>, X>) = invoke(pair.toMap())

  override fun toString(): String = when (this) {
    is Exp -> "exp($exponent)"
    is Log -> "ln($logarithmand)"
    is Negative -> "-$arg"
    is Power -> "($base${superscript(exponent)})"
    is SquareRoot -> "√($radicand)"
    is Sine -> "sin($angle)"
    is Cosine -> "cos($angle)"
    is Tangent -> "tan($angle)"
    is Product -> "($multiplicator * $multiplicand)"
    is Sum -> "($augend + $addend)"
    is Const -> value.toString()
    is Var -> name
  }

  override fun grad(): Map<Var<X>, Function<X>> = variables.associateWith { diff(it) }

  override fun diff(ind: Var<X>): Function<X> = when (this) {
    is Const -> Const(value - value) // zero // breaks TestSimpleDerivatives
    is Sum -> addend.diff(ind) + augend.diff(ind)
    // Product rule: d(u*v)/dx = du/dx * v + u * dv/dx
    is Product -> multiplicator.diff(ind) * multiplicand + multiplicator * multiplicand.diff(ind)
    is Var -> Const(if (this == ind) prototype.one else prototype.zero)
    is Log -> logarithmand.inverse() * logarithmand.diff(ind)
    is Negative -> -arg.diff(ind)
    is Power -> (this as Power).diff(ind)
    is Exp -> exp(exponent) * exponent.diff(ind)
    is SquareRoot -> sqrt(radicand).inverse() / two * radicand.diff(ind)
    is Sine -> cos(angle) * angle.diff(ind)
    is Cosine -> -sin(angle) * angle.diff(ind)
    is Tangent -> cos(angle).pow(-two) * angle.diff(ind)
  }

  override fun plus(addend: Function<X>): Function<X> = when {
    this is Zero -> addend
    addend is Zero -> this
    this is Const && addend is Const -> Const(value + addend.value)
    this == addend -> two * this
    else -> Sum(this, addend)
  }

  override fun times(multiplicand: Function<X>): Function<X> = when {
    this is Zero -> this
    this is One -> multiplicand
    multiplicand is One -> this
    multiplicand is Zero -> multiplicand
    this == multiplicand -> pow(two)
    this is Const && multiplicand is Const -> Const(value * multiplicand.value)
    this is Power && multiplicand is Power && base == multiplicand.base -> base.pow(exponent + multiplicand.exponent)
    this is Power && multiplicand is Var && base == multiplicand -> base.pow(exponent + one)
    this is Var && multiplicand is Power && this == multiplicand.base -> multiplicand.base.pow(multiplicand.exponent + one)
    else -> Product(this, multiplicand)
  }

  override fun div(divisor: Function<X>): Function<X> = when {
    this is Zero -> this
    this is One -> this
    divisor is One -> divisor.inverse()
    divisor is Zero -> throw Exception("Cannot divide by $divisor")
    this is Const && divisor is Const -> Const(value / divisor.value)
    this == divisor -> one
    else -> super.div(divisor)
  }

  override fun equals(other: Any?) =
    if (this is Var<*> || other is Var<*>) this === other
    //TODO implement tree comparison for semantic equals
    else super.equals(other)


  override fun inverse(): Function<X> = when (this) {
    is Const -> Const(value.inverse())
    is One -> this
    is Power -> base.pow(-exponent)
    else -> pow(-one)
  }

  override fun unaryMinus(): Function<X> = when(this) {
    is Const -> Const(-value)
    is Zero -> this
    is Negative -> arg
    else -> Negative(this)
  }

  override fun pow(exp: Function<X>): Function<X> = when {
    this is Const && exp is Const -> Const(value.pow(exp.value))
    this is Power && exp is Const -> base.pow(exponent * exp)
    else -> Power(this, exp)
  }

  companion object {
    fun <T: Field<T>> ln(angle: Function<T>) = Log(angle)
    fun <T: Field<T>> sin(angle: Function<T>) = Sine(angle)
    fun <T: Field<T>> cos(angle: Function<T>) = Cosine(angle)
    fun <T: Field<T>> tan(angle: Function<T>) = Tangent(angle)
    fun <T: Field<T>> exp(exponent: Function<T>) = Exp(exponent)
    fun <T: Field<T>> sqrt(radicand: Function<T>) = SquareRoot(radicand)
    fun <T: Field<T>> pow(base: Function<T>, exponent: Function<T>) = Power(base, exponent)
  }

  fun ln() = Log(this)
  fun sin() = Sine(this)
  fun cos() = Cosine(this)
  fun tan() = Tangent(this)
  fun exp() = Exp(this)
  fun sqrt() = SquareRoot(this)

  open val prototype: FieldPrototype<X> by lazy { variables.first().prototype }

  val one: One<X> by lazy { One(prototype) }

  val zero: Zero<X> by lazy { Zero(prototype) }

  val two: Const<X> by lazy { Const(one.value + one.value) }

  val e: Const<X> by lazy { Const(prototype.one) }

  //TODO: Replace roots with fractional powers
  class SquareRoot<X: Field<X>>(val radicand: Function<X>): Function<X>(radicand.variables)

  class Sine<X: Field<X>>(val angle: Function<X>): Function<X>(angle.variables)

  class Cosine<X: Field<X>>(val angle: Function<X>): Function<X>(angle.variables)

  class Tangent<X: Field<X>>(val angle: Function<X>): Function<X>(angle.variables)

  class Exp<X: Field<X>>(val exponent: Function<X>): Function<X>(exponent.variables)

  class Log<X: Field<X>>(val logarithmand: Function<X>): Function<X>(logarithmand.variables)

  class Negative<X: Field<X>>(val arg: Function<X>): Function<X>(arg.variables)

  class Product<X: Field<X>>(
    val multiplicator: Function<X>,
    val multiplicand: Function<X>
  ): Function<X>(multiplicator.variables + multiplicand.variables)

  class Sum<X: Field<X>>(
    val augend: Function<X>,
    val addend: Function<X>
  ): Function<X>(augend.variables + addend.variables)

  class Power<X: Field<X>>(
    val base: Function<X>,
    var exponent: Function<X>
  ): Function<X>(base.variables + exponent.variables) {
    override fun diff(ind: Var<X>) = when (exponent) {
      is One -> base.diff(ind)
      is Const -> exponent * Power(base, Const((exponent - one)())) * base.diff(ind)
      else -> this * (exponent * base.ln()).diff(ind)
    }

    fun superscript(exponent: Function<X>) =
      if (exponent is Const)
        if (exponent == one) ""
        else exponent.toString()
          .replace(".", "⋅")
          .replace("-", "⁻")
          .replace("0", "⁰")
          .replace("1", "¹")
          .replace("2", "²")
          .replace("3", "³")
          .replace("4", "⁴")
          .replace("5", "⁵")
          .replace("6", "⁶")
          .replace("7", "⁷")
          .replace("8", "⁸")
          .replace("9", "⁹")
      else
        "^($exponent)"
  }

  open class Const<X: Field<X>>(val value: X): Function<X>(emptySet())

  class One<X: Field<X>>(fieldPrototype: FieldPrototype<X>): Const<X>(fieldPrototype.one)

  class Zero<X: Field<X>>(fieldPrototype: FieldPrototype<X>): Const<X>(fieldPrototype.zero)

  class Var<X: Field<X>>(
    override val prototype: FieldPrototype<X>,
    val value: X = prototype.zero,
    override val name: String = randomDefaultName()
  ): Function<X>(emptySet()) {
    override val variables: Set<Var<X>> = setOf(this)
  }
}

//abstract class NullaryFunction<X: Field<X>>: Function<X>(emptySet())

//abstract class UnaryFunction<X: Field<X>>(arg: Function<X>): Function<X>(arg.variables)

//abstract class BinaryFunction<X: Field<X>>(rfn: Function<X>, lfn: Function<X>): Function<X>(rfn.variables + lfn.variables)

//abstract class TernaryFunction<X: Field<X>>(fn1: Function<X>, fn2: Function<X>, fn3: Function<X>): Function<X>(fn1.variables + fn2.variables + fn3.variables)
//
//abstract class QuaternaryFunction<X: Field<X>>(val fn1: Function<X>, val fn2: Function<X>, val fn3: Function<X>, val fn4: Function<X>): Function<X>
//
//abstract class QuinaryFunction<X: Field<X>>(val fn1: Function<X>, val fn2: Function<X>, val fn3: Function<X>, val fn4: Function<X>, val fn5: Function<X>): Function<X>
//
//abstract class SenaryFunction<X: Field<X>>(val fn1: Function<X>, val fn2: Function<X>, val fn3: Function<X>, val fn4: Function<X>, val fn5: Function<X>, val fn6: Function<X>): Function<X>
//
//abstract class SeptenaryFunction<X: Field<X>>(val fn1: Function<X>, val fn2: Function<X>, val fn3: Function<X>, val fn4: Function<X>, val fn5: Function<X>, val fn6: Function<X>, val fn7: Function<X>): Function<X>
//
//abstract class OctonaryFunction<X: Field<X>>(val fn1: Function<X>, val fn2: Function<X>, val fn3: Function<X>, val fn4: Function<X>, val fn5: Function<X>, val fn6: Function<X>, val fn7: Function<X>, val fn8: Function<X>): Function<X>
//
//abstract class NovenaryFunction<X: Field<X>>(val fn1: Function<X>, val fn2: Function<X>, val fn3: Function<X>, val fn4: Function<X>, val fn5: Function<X>, val fn6: Function<X>, val fn7: Function<X>, val fn8: Function<X>, val fn9: Function<X>): Function<X>
//
//abstract class DenaryFunction<X: Field<X>>(val fn1: Function<X>, val fn2: Function<X>, val fn3: Function<X>, val fn4: Function<X>, val fn5: Function<X>, val fn6: Function<X>, val fn7: Function<X>, val fn8: Function<X>, val fn9: Function<X>, val fn10: Function<X>): Function<X>