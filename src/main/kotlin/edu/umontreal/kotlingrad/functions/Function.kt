package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.numerical.FieldPrototype
import edu.umontreal.kotlingrad.utils.randomDefaultName

interface Differentiable<X: Field<X>, D> {
  fun diff(ind: Var<X>): D

  fun grad(): Map<Var<X>, D>
}

sealed class Function<X: Field<X>>(open val variables: Set<Var<X>>):
  Field<Function<X>>, Differentiable<X, Function<X>>, kotlin.Function<X> {
  open val name: String = randomDefaultName()

  abstract operator fun invoke(map: Map<Var<X>, X> = emptyMap()): X

  operator fun invoke(vararg pair: Pair<Var<X>, X>) = invoke(pair.toMap())

  abstract override fun toString(): String

  override fun grad(): Map<Var<X>, Function<X>> = variables.associateWith { diff(it) }

  override fun diff(ind: Var<X>): Function<X> = when (this) {
    is Const -> Const(value - value) // zero // breaks TestSimpleDerivatives
    is Sum -> addend.diff(ind) + augend.diff(ind)
    // Product rule: d(u*v)/dx = du/dx * v + u * dv/dx
    is Product -> multiplicator.diff(ind) * multiplicand + multiplicator * multiplicand.diff(ind)
    is Var -> Const(if (this == ind) prototype.one else prototype.zero)
    is Log -> Inverse(logarithmand) * logarithmand.diff(ind)
    is Inverse -> -one * Power(arg, -two) * arg.diff(ind)
    is Negative -> -arg.diff(ind)
    is Power -> (this as Power).diff(ind)
    is Exp -> exp(exponent) * exponent.diff(ind)
    is SquareRoot -> sqrt(radicand).inverse() / two * radicand.diff(ind)
    is Sine -> cos(angle) * angle.diff(ind)
    is Cosine -> -sin(angle) * angle.diff(ind)
    is Tangent -> Power(cos(angle), -two) * angle.diff(ind)
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
    this is Const && multiplicand is Const -> Const(value * multiplicand.value)
    this == multiplicand -> pow(two)
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

  fun ln(): Function<X> = Log(this)

  override fun inverse(): Function<X> = Inverse(this)

  override fun unaryMinus(): Function<X> = when {
    this is Const -> Const(-value)
    else -> Negative(this)
  }

  override fun pow(exponent: Function<X>): Function<X> = when {
    this is Const && exponent is Const -> Const(value.pow(exponent.value))
    else -> Power(this, exponent)
  }

  fun cos(angle: Function<X>) = Cosine(angle)

  fun sin(angle: Function<X>): Function<X> = Sine(angle)

  fun tan(angle: Function<X>): Function<X> = Tangent(angle)

  fun exp(exponent: Function<X>): Function<X> = Exp(exponent)

  fun sqrt(radicand: Function<X>): Function<X> = SquareRoot(radicand)

  open val prototype: FieldPrototype<X> by lazy { variables.first().prototype }

  val one: One<X> by lazy { One(prototype) }

  val zero: Zero<X> by lazy { Zero(prototype) }

  val two: Const<X> by lazy { Const(one.value + one.value) }

  val e: Const<X> by lazy { Const(prototype.one) }
}

class Var<X: Field<X>>(
  override val prototype: FieldPrototype<X>,
  val value: X = prototype.zero,
  override val name: String = randomDefaultName()
): Function<X>(emptySet()) {
  override val variables: Set<Var<X>> = setOf(this)

  override fun invoke(map: Map<Var<X>, X>): X = if (map[this] != null) map[this]!! else value

  override fun toString() = name
}

class Inverse<X: Field<X>>(val arg: Function<X>): Function<X>(arg.variables) {
  override fun invoke(map: Map<Var<X>, X>) = arg(map).inverse()

  override fun toString() = "$arg⁻¹"

  override fun inverse() = arg
}

class Exp<X: Field<X>>(
  val exponent: Function<X>
): Function<X>(exponent.variables) {
  override fun invoke(map: Map<Var<X>, X>): X = prototype.exp(exponent(map))

  override fun toString(): String = "exp($exponent)"
}

class Log<X: Field<X>>(val logarithmand: Function<X>): Function<X>(logarithmand.variables) {
  override fun invoke(map: Map<Var<X>, X>): X = prototype.log(logarithmand(map))

  override fun toString(): String = "ln($logarithmand)"
}

class Negative<X: Field<X>>(val arg: Function<X>): Function<X>(arg.variables) {
  override fun invoke(map: Map<Var<X>, X>) = -arg(map)

  override fun toString() = "-$arg"

  override fun unaryMinus() = arg
}

class Power<X: Field<X>>(
  private val base: Function<X>,
  var exponent: Function<X>
): Function<X>(base.variables + exponent.variables) {
  override fun invoke(map: Map<Var<X>, X>): X = base.invoke(map).pow(exponent(map))

  override fun diff(ind: Var<X>) = when (exponent) {
    is One -> base.diff(ind)
    is Const -> exponent * Power(base, Const((exponent - one)())) * base.diff(ind)
    else -> this * (exponent * base.ln()).diff(ind)
  }

  override fun toString() = "($base${superscript(exponent)})"

  override fun inverse() = Power(base, -exponent)

  private fun superscript(exponent: Function<X>) =
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

class SquareRoot<X: Field<X>>(val radicand: Function<X>): Function<X>(radicand.variables) {
  override fun invoke(map: Map<Var<X>, X>): X = prototype.sqrt(radicand(map))

  override fun toString(): String = "√($radicand)"
}

class Sine<X: Field<X>>(val angle: Function<X>): Function<X>(angle.variables) {
  override fun invoke(map: Map<Var<X>, X>): X = prototype.sin(angle(map))

  override fun toString(): String = "sin($angle)"
}

class Cosine<X: Field<X>>(val angle: Function<X>): Function<X>(angle.variables) {
  override fun invoke(map: Map<Var<X>, X>): X = prototype.cos(angle(map))

  override fun toString() = "cos($angle)"
}

class Tangent<X: Field<X>>(val angle: Function<X>): Function<X>(angle.variables) {
  override fun invoke(map: Map<Var<X>, X>): X = prototype.tan(angle(map))

  override fun toString(): String = "tan($angle)"
}

class Product<X: Field<X>>(
  val multiplicator: Function<X>,
  val multiplicand: Function<X>
): Function<X>(multiplicator.variables + multiplicand.variables) {
  override fun invoke(map: Map<Var<X>, X>) = multiplicator(map) * multiplicand(map)

  override fun toString() = "($multiplicator * $multiplicand)"
}

class Sum<X: Field<X>>(
  val augend: Function<X>,
  val addend: Function<X>
): Function<X>(augend.variables + addend.variables) {
  // Some operations are inherently parallelizable. TODO: Explore how to parallelize these with FP...
  override fun invoke(map: Map<Var<X>, X>) = augend(map) + addend(map)

  override fun toString() = "($augend + $addend)"
}

open class Const<X: Field<X>>(val value: X): Function<X>(emptySet()) {
  override fun invoke(map: Map<Var<X>, X>): X = value

  override fun toString(): String = value.toString()

  override fun inverse(): Const<X> = Const(value.inverse())

  override fun unaryMinus(): Const<X> = Const(-value)
}

class One<X: Field<X>>(fieldPrototype: FieldPrototype<X>): Const<X>(fieldPrototype.one)

class Zero<X: Field<X>>(fieldPrototype: FieldPrototype<X>): Const<X>(fieldPrototype.zero) {
  override fun unaryMinus() = this
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