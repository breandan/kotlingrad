package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.calculus.Differentiable
import edu.umontreal.kotlingrad.utils.randomDefaultName

sealed class Fun<X: Fun<X>>(open val variables: Set<ScalarVar<X>> = emptySet()):
  Field<Fun<X>>, Differentiable<Fun<X>> {
  open val name: String = randomDefaultName()
  constructor(fn: Fun<X>): this(fn.variables)
  constructor(vararg fns: Fun<X>): this(fns.flatMap { it.variables }.toSet())

  @JvmName("substitutionInvoke")
  operator fun invoke(map: Map<ScalarVar<X>, Fun<X>>): Fun<X> = when (this) {
    is Exp -> exponent(map).exp()
    is Log -> logarithmand(map).log()
    is Negative -> -arg(map)
    is Power -> base(map) pow exponent(map)
    is SquareRoot -> radicand(map).sqrt()
    is Sine -> angle(map).sin()
    is Cosine -> angle(map).cos()
    is Tangent -> angle(map).tan()
    is ScalarConst -> this
    is ScalarVar -> map.getOrElse(this) { value }
    is ScalarProduct -> multiplicator(map) * multiplicand(map)
    is ScalarSum -> augend(map) + addend(map)
  }

  @JvmName("numericalInvoke")
  operator fun invoke(map: Map<ScalarVar<X>, X>): Fun<X> = when (this) {
    is Exp -> exponent(map).exp()
    is Log -> logarithmand(map).log()
    is Negative -> -arg(map)
    is Power -> base(map) pow exponent(map)
    is SquareRoot -> radicand(map).sqrt()
    is Sine -> angle(map).sin()
    is Cosine -> angle(map).cos()
    is Tangent -> angle(map).tan()
    is ScalarConst -> this
    is ScalarVar -> map.getOrElse(this) { value }
    is ScalarProduct -> multiplicator(map) * multiplicand(map)
    is ScalarSum -> augend(map) + addend(map)
  }

  override fun toString(): String = when {
    this is Exp -> "exp($exponent)"
    this is Log -> "ln($logarithmand)"
    this is Negative -> "-$arg"
    this is Power -> if (exponent == one) "$base" else "$base pow (${exponent})"
    this is SquareRoot -> "sqrt($radicand)"
    this is Sine -> "sin($angle)"
    this is Cosine -> "cos($angle)"
    this is Tangent -> "tan($angle)"
    this is ScalarProduct && multiplicand is ScalarSum -> "$multiplicator * ($multiplicand)"
    this is ScalarProduct && multiplicator is ScalarSum -> "($multiplicator) * $multiplicand"
    this is ScalarProduct -> "$multiplicator * $multiplicand"
    this is ScalarSum && addend is Negative -> "$augend - ${addend.arg}"
    this is ScalarSum -> "$augend + $addend"
    this is ScalarVar -> name
    else -> "UNKNOWN"
  }

  override fun grad(): Map<Fun<X>, Fun<X>> = variables.associateWith { diff(it) }

  override fun diff(ind: Fun<X>): Fun<X> = when {
    this == ind -> one
    this is ScalarConst -> zero // breaks TestSimpleDerivatives
    this is ScalarSum -> addend.diff(ind) + augend.diff(ind)
    // Product rule: d(u*v)/dx = du/dx * v + u * dv/dx
    this is ScalarProduct -> multiplicator.diff(ind) * multiplicand + multiplicator * multiplicand.diff(ind)
    this is ScalarVar -> if (this in ind.variables) one * ind.inverse() else zero
    this is Log -> logarithmand.inverse() * logarithmand.diff(ind)
    this is Negative -> -arg.diff(ind)
    this is Exp -> exponent.exp() * exponent.diff(ind)
    this is SquareRoot -> radicand.sqrt().inverse() / two * radicand.diff(ind)
    this is Sine -> angle.cos() * angle.diff(ind)
    this is Cosine -> -angle.sin() * angle.diff(ind)
    this is Tangent -> (angle.cos() pow -two) * angle.diff(ind)
    else -> zero
  }

  override fun plus(addend: Fun<X>): Fun<X> = when {
    this == zero -> addend
    addend == zero -> this
    this == addend -> two * this
    else -> ScalarSum(this, addend)
  }

  override fun times(multiplicand: Fun<X>): Fun<X> = when {
    this == zero -> this
    this == one -> multiplicand
    multiplicand == one -> this
    multiplicand == zero -> multiplicand
    this == multiplicand -> pow(two)
    this is Power && multiplicand is Power && base == multiplicand.base -> base.pow(exponent + multiplicand.exponent)
    this is Power && multiplicand is ScalarVar && base == multiplicand -> base.pow(exponent + one)
    this is ScalarVar && multiplicand is Power && this == multiplicand.base -> multiplicand.base.pow(multiplicand.exponent + one)
    this is Negative && multiplicand is Negative -> ScalarProduct(arg, multiplicand.arg)
    multiplicand is Negative -> -ScalarProduct(this, multiplicand.arg)
    this is Negative -> -ScalarProduct(arg, multiplicand)
    else -> ScalarProduct(this, multiplicand)
  }

  override fun div(divisor: Fun<X>): Fun<X> = when {
    this == zero -> this
    this == one -> divisor.inverse()
    divisor == one -> this
    divisor == zero -> throw Exception("Cannot divide by $divisor")
    this == divisor -> one
    else -> super.div(divisor)
  }

  override fun equals(other: Any?) =
    if (this is ScalarVar<*> || other is ScalarVar<*>) this === other
    //TODO implement tree comparison for semantic equals
    else super.equals(other)

  override fun inverse(): Fun<X> = when {
    this == one -> this
    this is Power -> base.pow(-exponent)
    else -> pow(-one)
  }

  override fun unaryMinus(): Fun<X> = when {
    this == zero -> this
    this is Negative -> arg
    else -> Negative(this)
  }

  override fun pow(exp: Fun<X>): Fun<X> = when {
    exp == zero -> one
    exp is ScalarConst && exp == (one / two) -> sqrt()
    this is Power && exp is ScalarConst -> base pow exponent * exp
    else -> Power(this, exp)
  }

  fun ln() = Log(this)
  override fun log(): Fun<X> = Log(this)
  override fun sin(): Fun<X> = Sine(this)
  override fun cos(): Fun<X> = Cosine(this)
  override fun tan(): Fun<X> = Tangent(this)
  override fun exp(): Fun<X> = Exp(this)
  override fun sqrt(): Fun<X> = SquareRoot(this)

  open val proto: X by lazy { variables.first().value }
  override val one: ScalarConst<X> by lazy { proto.one }
  override val zero: ScalarConst<X> by lazy { proto.zero }
  override val e: ScalarConst<X> by lazy { proto.e }
  open val two: ScalarConst<X> by lazy { proto.two }
}

//TODO: Replace roots with fractional powers
class SquareRoot<X: Fun<X>> internal constructor(val radicand: Fun<X>): Fun<X>(radicand)

class Sine<X: Fun<X>> internal constructor(val angle: Fun<X>): Fun<X>(angle)

class Cosine<X: Fun<X>> internal constructor(val angle: Fun<X>): Fun<X>(angle)

class Tangent<X: Fun<X>> internal constructor(val angle: Fun<X>): Fun<X>(angle)

class Exp<X: Fun<X>> internal constructor(val exponent: Fun<X>): Fun<X>(exponent)

class Log<X: Fun<X>> internal constructor(val logarithmand: Fun<X>): Fun<X>(logarithmand)

class Negative<X: Fun<X>> internal constructor(val arg: Fun<X>): Fun<X>(arg)

class ScalarProduct<X: Fun<X>> internal constructor(
    val multiplicator: Fun<X>,
    val multiplicand: Fun<X>
): Fun<X>(multiplicator, multiplicand)

class ScalarSum<X: Fun<X>> internal constructor(
    val augend: Fun<X>,
    val addend: Fun<X>
): Fun<X>(augend, addend)

class Power<X: Fun<X>> internal constructor(
    val base: Fun<X>,
    val exponent: Fun<X>
): Fun<X>(base, exponent) {
  override fun diff(ind: Fun<X>) = when (exponent) {
    one -> base.diff(ind)
    is ScalarConst -> exponent * base.pow(exponent - one) * base.diff(ind)
    else -> this * (exponent * base.ln()).diff(ind)
  }
}

open class ScalarConst<X: Fun<X>>: Fun<X>()

class ScalarVar<X: Fun<X>>: Fun<X> {
  val value: X
  override val name: String
  override val variables: Set<ScalarVar<X>>

  internal constructor(value: X, name: String = randomDefaultName()): super() {
    require(' ' !in name) { "Variable name must not contain spaces" }
    this.value = value
    this.name = name.replace(" ", "")
    this.variables = setOf(this)
  }
}