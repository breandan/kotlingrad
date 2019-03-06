package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.calculus.Differentiable
import edu.umontreal.kotlingrad.utils.randomDefaultName
import edu.umontreal.kotlingrad.utils.superscript

sealed class ScalarFun<X: Field<X>>(open val variables: Set<ScalarVar<X>> = emptySet()):
  Field<ScalarFun<X>>, Function<X>, Differentiable<ScalarFun<X>> {
  open val name: String = randomDefaultName()

  constructor(fn: ScalarFun<X>): this(fn.variables)
  constructor(vararg fns: ScalarFun<X>): this(fns.flatMap { it.variables }.toSet())

  override operator fun invoke(map: Map<Var<X>, X>): X = when (this) {
    is Exp -> exponent(map).exp()
    is Log -> logarithmand(map).log()
    is Negative -> -arg(map)
    is Power -> base(map) pow exponent(map)
    is SquareRoot -> radicand(map).sqrt()
    is Sine -> angle(map).sin()
    is Cosine -> angle(map).cos()
    is Tangent -> angle(map).tan()
    else -> super.invoke(map)
  }

  operator fun invoke(vararg pair: Pair<ScalarVar<X>, X>) = invoke(pair.toMap())

  override fun toString(): String = when {
    this is Exp -> "exp($exponent)"
    this is Log -> "ln($logarithmand)"
    this is Negative -> "-$arg"
    this is Power -> "$base${superscript(exponent)}"
    this is SquareRoot && (radicand is ScalarConst || radicand is ScalarVar) -> "√$radicand"
    this is SquareRoot -> "√($radicand)"
    this is Sine -> "sin($angle)"
    this is Cosine -> "cos($angle)"
    this is Tangent -> "tan($angle)"
    this is ScalarProduct && multiplicand is ScalarSum -> "$multiplicator⋅($multiplicand)"
    this is ScalarProduct && multiplicator is ScalarSum -> "($multiplicator)⋅$multiplicand"
    this is ScalarProduct -> "$multiplicator⋅$multiplicand"
    this is ScalarSum && addend is Negative -> "$augend - ${addend.arg}"
    this is ScalarSum -> "$augend + $addend"
    this is ScalarConst -> "$value"
    this is ScalarVar -> name
    else -> "UNKNOWN"
  }

  override fun grad(): Map<ScalarFun<X>, ScalarFun<X>> = variables.associateWith { diff(it) }

  override fun diff(ind: ScalarFun<X>): ScalarFun<X> = when {
    this == ind -> one
    this is ScalarConst -> zero // breaks TestSimpleDerivatives
    this is ScalarSum -> addend.diff(ind) + augend.diff(ind)
    // Product rule: d(u*v)/dx = du/dx * v + u * dv/dx
    this is ScalarProduct -> multiplicator.diff(ind) * multiplicand + multiplicator * multiplicand.diff(ind)
    this is ScalarVar -> if (this in ind.variables) one * ind.inverse() else zero
    this is Log -> logarithmand.inverse() * logarithmand.diff(ind)
    this is Negative -> -arg.diff(ind)
    this is Power -> (this as Power).diff(ind)
    this is Exp -> exponent.exp() * exponent.diff(ind)
    this is SquareRoot -> radicand.sqrt().inverse() / two * radicand.diff(ind)
    this is Sine -> angle.cos() * angle.diff(ind)
    this is Cosine -> -angle.sin() * angle.diff(ind)
    this is Tangent -> (angle.cos() pow -two) * angle.diff(ind)
    else -> zero
  }

  override fun plus(addend: ScalarFun<X>): ScalarFun<X> = when {
    this == zero -> addend
    addend == zero -> this
    this is ScalarConst && addend is ScalarConst -> const(value + addend.value)
    this == addend -> two * this
    else -> ScalarSum(this, addend)
  }

  override fun times(multiplicand: ScalarFun<X>): ScalarFun<X> = when {
    this == zero -> this
    this == one -> multiplicand
    multiplicand == one -> this
    multiplicand == zero -> multiplicand
    this == multiplicand -> pow(two)
    this is ScalarConst && multiplicand is ScalarConst -> const(value * multiplicand.value)
    this is Power && multiplicand is Power && base == multiplicand.base -> base.pow(exponent + multiplicand.exponent)
    this is Power && multiplicand is ScalarVar && base == multiplicand -> base.pow(exponent + one)
    this is ScalarVar && multiplicand is Power && this == multiplicand.base -> multiplicand.base.pow(multiplicand.exponent + one)
    this is Negative && multiplicand is Negative -> ScalarProduct(arg, multiplicand.arg)
    multiplicand is Negative -> -ScalarProduct(this, multiplicand.arg)
    this is Negative -> -ScalarProduct(arg, multiplicand)
    else -> ScalarProduct(this, multiplicand)
  }

  override fun div(divisor: ScalarFun<X>): ScalarFun<X> = when {
    this == zero -> this
    this == one -> divisor.inverse()
    divisor == one -> this
    divisor == zero -> throw Exception("Cannot divide by $divisor")
    this == divisor -> one
    this is ScalarConst && divisor is ScalarConst -> const(value / divisor.value)
    else -> super.div(divisor)
  }

  override fun equals(other: Any?) =
    if (this is ScalarVar<*> || other is ScalarVar<*>) this === other
    else if (this is ScalarConst<*> && other is ScalarConst<*>) value == other.value
    //TODO implement tree comparison for semantic equals
    else super.equals(other)

  override fun inverse(): ScalarFun<X> = when {
    this == one -> this
    this is ScalarConst -> const(value.inverse())
    this is Power -> base.pow(-exponent)
    else -> pow(-one)
  }

  override fun unaryMinus(): ScalarFun<X> = when {
    this == zero -> this
    this is ScalarConst -> const(-value)
    this is Negative -> arg
    else -> Negative(this)
  }

  override fun pow(exp: ScalarFun<X>): ScalarFun<X> = when {
    this is ScalarConst && exp is ScalarConst -> const(value pow exp.value)
    exp == zero -> one
    exp is ScalarConst && exp == (one / two) -> sqrt()
    this is Power && exp is ScalarConst -> base pow exponent * exp
    else -> Power(this, exp)
  }

  fun const(value: X) = when (value) {
    zero.value -> zero
    one.value -> one
    else -> ScalarConst(value)
  }

  fun ln() = Log(this)
  override fun log() = Log(this)
  override fun sin() = Sine(this)
  override fun cos() = Cosine(this)
  override fun tan() = Tangent(this)
  override fun exp() = Exp(this)
  override fun sqrt() = SquareRoot(this)

  override val one: ScalarConst<X> by lazy { ScalarConst(this().one) }
  override val zero: ScalarConst<X> by lazy { ScalarConst(this().zero) }
  override val e: ScalarConst<X> by lazy { ScalarConst(this().e) }
  val two: ScalarConst<X> by lazy { ScalarConst(one.value + one.value) }

  infix operator fun plus(addend: X) = this + const(addend)
  infix operator fun minus(subtrahend: X) = this - const(subtrahend)
  infix operator fun times(multiplicand: X) = this * const(multiplicand)
  infix operator fun div(divisor: X) = this / const(divisor)
  infix fun pow(exponent: X) = this pow const(exponent)
}

//TODO: Replace roots with fractional powers
class SquareRoot<X: Field<X>> internal constructor(val radicand: ScalarFun<X>): ScalarFun<X>(radicand)

class Sine<X: Field<X>> internal constructor(val angle: ScalarFun<X>): ScalarFun<X>(angle)

class Cosine<X: Field<X>> internal constructor(val angle: ScalarFun<X>): ScalarFun<X>(angle)

class Tangent<X: Field<X>> internal constructor(val angle: ScalarFun<X>): ScalarFun<X>(angle)

class Exp<X: Field<X>> internal constructor(val exponent: ScalarFun<X>): ScalarFun<X>(exponent)

class Log<X: Field<X>> internal constructor(val logarithmand: ScalarFun<X>): ScalarFun<X>(logarithmand)

class Negative<X: Field<X>> internal constructor(val arg: ScalarFun<X>): ScalarFun<X>(arg)

class ScalarProduct<X: Field<X>> internal constructor(
  override val multiplicator: ScalarFun<X>,
  override val multiplicand: ScalarFun<X>
): ScalarFun<X>(multiplicator, multiplicand), Product<X>, Function<X>

class ScalarSum<X: Field<X>> internal constructor(
  override val augend: ScalarFun<X>,
  override val addend: ScalarFun<X>
): ScalarFun<X>(augend, addend), Sum<X>

class Power<X: Field<X>> internal constructor(
  val base: ScalarFun<X>,
  val exponent: ScalarFun<X>
): ScalarFun<X>(base, exponent) {
  override fun diff(ind: ScalarFun<X>) = when (exponent) {
    one -> base.diff(ind)
    is ScalarConst -> exponent * base.pow(exponent - one) * base.diff(ind)
    else -> this * (exponent * base.ln()).diff(ind)
  }

  fun superscript(exponent: ScalarFun<X>) = when {
    exponent == one -> ""
    "$exponent".matches(Regex("[() a-pr-z0-9⋅+-]*")) -> "$exponent".superscript()
    else -> "^($exponent)"
  }
}

// TODO: Try to make RealNumber a subtype of ScalarConst

open class ScalarConst<X: Field<X>> internal constructor(override val value: X): ScalarFun<X>(), Const<X>

class ScalarVar<X: Field<X>>: ScalarFun<X>, Var<X> {
  override val value: X
  override val name: String

  internal constructor(value: X, name: String = randomDefaultName()): super() {
    if (name.contains(' ')) throw IllegalArgumentException("Variable name must not contain spaces")
    this.value = value
    this.name = name.replace(" ", "")
    this.variables = setOf(this)
  }

  override val variables: Set<ScalarVar<X>>
}