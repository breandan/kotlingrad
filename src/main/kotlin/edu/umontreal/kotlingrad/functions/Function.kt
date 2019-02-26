package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.utils.randomDefaultName
import edu.umontreal.kotlingrad.utils.superscript

sealed class Function<X : Field<X>>(open val variables: Set<Var<X>> = emptySet()) :
    Field<Function<X>>, kotlin.Function<X> {
  open val name: String = randomDefaultName()

  constructor(fn: Function<X>) : this(fn.variables)
  constructor(vararg fns: Function<X>) : this(fns.flatMap { it.variables }.toSet())

  operator fun invoke(map: Map<Var<X>, X> = emptyMap()): X = when (this) {
    is Const -> value
    is Var -> map.getOrElse(this) { value }
    is Exp -> exponent(map).exp()
    is Log -> logarithmand(map).log()
    is Negative -> -arg(map)
    is Power -> base(map) pow exponent(map)
    is SquareRoot -> radicand(map).sqrt()
    is Sine -> angle(map).sin()
    is Cosine -> angle(map).cos()
    is Tangent -> angle(map).tan()
    is Product -> multiplicator(map) * multiplicand(map)
    is Sum -> augend(map) + addend(map)
  }

  operator fun invoke(vararg pair: Pair<Var<X>, X>) = invoke(pair.toMap())

  override fun toString(): String = when {
    this is Exp -> "exp($exponent)"
    this is Log -> "ln($logarithmand)"
    this is Negative -> "-$arg"
    this is Power -> "$base${superscript(exponent)}"
    this is SquareRoot -> "√($radicand)"
    this is Sine -> "sin($angle)"
    this is Cosine -> "cos($angle)"
    this is Tangent -> "tan($angle)"
    this is Product && multiplicand  is Sum -> "$multiplicator⋅($multiplicand)"
    this is Product && multiplicator is Sum -> "($multiplicator)⋅$multiplicand"
    this is Product -> "$multiplicator⋅$multiplicand"
    this is Sum && addend is Negative -> "$augend - ${addend.arg}"
    this is Sum -> "$augend + $addend"
    this is Const -> "$value"
    this is Var -> name
    else -> "UNKNOWN"
  }

  override fun grad(): Map<Function<X>, Function<X>> = variables.associateWith { diff(it) }

  override fun diff(ind: Function<X>): Function<X> = when {
    this == ind -> one
    this is Const -> zero // breaks TestSimpleDerivatives
    this is Sum -> addend.diff(ind) + augend.diff(ind)
    // Product rule: d(u*v)/dx = du/dx * v + u * dv/dx
    this is Product -> multiplicator.diff(ind) * multiplicand + multiplicator * multiplicand.diff(ind)
    this is Var -> if (this in ind.variables) one * ind.inverse() else zero
    this is Log -> logarithmand.inverse() * logarithmand.diff(ind)
    this is Negative -> -arg.diff(ind)
    this is Power -> (this as Power).diff(ind)
    this is Exp -> exponent.exp() * exponent.diff(ind)
    this is SquareRoot -> radicand.sqrt().inverse() / two * radicand.diff(ind)
    this is Sine -> angle.cos() * angle.diff(ind)
    this is Cosine -> -angle.sin() * angle.diff(ind)
    this is Tangent -> angle.cos().pow(-two) * angle.diff(ind)
    else -> zero
  }

  override fun plus(addend: Function<X>): Function<X> = when {
    this == zero -> addend
    addend == zero -> this
    this is Const && addend is Const -> const(value + addend.value)
    this == addend -> two * this
    else -> Sum(this, addend)
  }

  override fun times(multiplicand: Function<X>): Function<X> = when {
    this == zero -> this
    this == one -> multiplicand
    multiplicand == one -> this
    multiplicand == zero -> multiplicand
    this == multiplicand -> pow(two)
    this is Const && multiplicand is Const -> const(value * multiplicand.value)
    this is Power && multiplicand is Power && base == multiplicand.base -> base.pow(exponent + multiplicand.exponent)
    this is Power && multiplicand is Var && base == multiplicand -> base.pow(exponent + one)
    this is Var && multiplicand is Power && this == multiplicand.base -> multiplicand.base.pow(multiplicand.exponent + one)
    this is Negative && multiplicand is Negative -> Product(arg, multiplicand.arg)
    multiplicand is Negative -> -Product(this, multiplicand.arg)
    this is Negative -> -Product(arg, multiplicand)
    else -> Product(this, multiplicand)
  }

  override fun div(divisor: Function<X>): Function<X> = when {
    this == zero -> this
    this == one -> divisor.inverse()
    divisor == one -> this
    divisor == zero -> throw Exception("Cannot divide by $divisor")
    this is Const && divisor is Const -> const(value / divisor.value)
    this == divisor -> one
    else -> super.div(divisor)
  }

  override fun equals(other: Any?) =
      if (this is Var<*> || other is Var<*>) this === other
      else if (this is Const<*> && other is Const<*>) value == other.value
      //TODO implement tree comparison for semantic equals
      else super.equals(other)


  override fun inverse(): Function<X> = when {
    this == one -> this
    this is Const -> const(value.inverse())
    this is Power -> base.pow(-exponent)
    else -> pow(-one)
  }

  override fun unaryMinus(): Function<X> = when {
    this == zero -> this
    this is Const -> const(-value)
    this is Negative -> arg
    else -> Negative(this)
  }

  override fun pow(exp: Function<X>): Function<X> = when {
    this is Const && exp is Const -> const(value.pow(exp.value))
    exp == zero -> one
    this is Power && exp is Const -> base.pow(exponent * exp)
    else -> Power(this, exp)
  }

  fun const(value: X) = when (value) {
    zero.value -> zero
    one.value -> one
    else -> Const(value)
  }

  fun ln() = Log(this)
  override fun log() = Log(this)
  override fun sin() = Sine(this)
  override fun cos() = Cosine(this)
  override fun tan() = Tangent(this)
  override fun exp() = Exp(this)
  override fun sqrt() = SquareRoot(this)

  override val one: Const<X> by lazy { Const(this().one) }
  override val zero: Const<X> by lazy { Const(this().zero) }
  override val e: Const<X> by lazy { Const(this().e) }
  val two: Const<X> by lazy { Const(one.value + one.value) }

  //TODO: Replace roots with fractional powers
  class SquareRoot<X : Field<X>> internal constructor(val radicand: Function<X>) : Function<X>(radicand)

  class Sine<X : Field<X>> internal constructor(val angle: Function<X>) : Function<X>(angle)

  class Cosine<X : Field<X>> internal constructor(val angle: Function<X>) : Function<X>(angle)

  class Tangent<X : Field<X>> internal constructor(val angle: Function<X>) : Function<X>(angle)

  class Exp<X : Field<X>> internal constructor(val exponent: Function<X>) : Function<X>(exponent)

  class Log<X : Field<X>> internal constructor(val logarithmand: Function<X>) : Function<X>(logarithmand)

  class Negative<X : Field<X>> internal constructor(val arg: Function<X>) : Function<X>(arg)

  class Product<X : Field<X>> internal constructor(
      val multiplicator: Function<X>,
      val multiplicand: Function<X>
  ) : Function<X>(multiplicator, multiplicand)

  class Sum<X : Field<X>> internal constructor(
      val augend: Function<X>,
      val addend: Function<X>
  ) : Function<X>(augend, addend)

  class Power<X : Field<X>> internal constructor(
      val base: Function<X>,
      val exponent: Function<X>
  ) : Function<X>(base, exponent) {
    override fun diff(ind: Function<X>) = when (exponent) {
      one -> base.diff(ind)
      is Const -> exponent * base.pow(exponent - one) * base.diff(ind)
      else -> this * (exponent * base.ln()).diff(ind)
    }

    fun superscript(exponent: Function<X>) =
        if (exponent is Const || exponent is Var && exponent.toString().matches(Regex("[a-pr-z0-9]*")))
          if (exponent == one) "" else exponent.toString().superscript()
        else
          "^($exponent)"
  }

  // TODO: Try to make RealNumber a subtype of Const

  open class Const<X : Field<X>> internal constructor(val value: X) : Function<X>()

  class Var<X : Field<X>> internal constructor(
      val value: X,
      override val name: String = randomDefaultName()
  ) : Function<X>() {
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