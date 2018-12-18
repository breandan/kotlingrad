package co.ndan.kotlingrad.math.calculus

import co.ndan.kotlingrad.math.algebra.Real
import co.ndan.kotlingrad.math.algebra.RealPrototype
import co.ndan.kotlingrad.math.types.*
import java.util.*

open class RealFunctor<X : Real<X>>(val rfc: RealPrototype<X>) {
  fun value(fnx: X) = Const(fnx, rfc)

  fun value(vararg fnx: X): ConstVector<X> {
    val size = fnx.size
    val list = ArrayList<Const<X>>(size)
    for (i in 0 until size) list.add(value(fnx[i]))
    return ConstVector(rfc, list)
  }

  fun zero(size: Int): ConstVector<X> {
    val list = ArrayList<Const<X>>(size)
    for (i in 0 until size) list.add(zero())
    return ConstVector(rfc, list)
  }

  fun variable(name: String, x: X) = Var(name, x, rfc)

  fun function(vararg fnx: Function<X>): VectorFunction<X> {
    val size = fnx.size
    val list = ArrayList<Function<X>>(size)
    for (i in 0 until size) list.add(fnx[i])
    return VectorFunction(rfc, *list.toTypedArray())
  }

  fun zero() = Zero(rfc)

  fun one() = One(rfc)

  fun cos(angle: Function<X>) = object : UnaryFunction<X>(angle) {
    override val value: X
      get() = rfc.cos(arg.value)

    override fun differentiate(ind: Var<X>) = -(sin(arg) * arg.differentiate(ind))

    override fun toString() = "cos($arg)"
  }

  fun sin(angle: Function<X>): Function<X> = object : UnaryFunction<X>(angle) {
    override val value: X
      get() = rfc.sin(arg.value)

    override fun differentiate(ind: Var<X>) = cos(arg) * arg.differentiate(ind)

    override fun toString(): String = "sin($arg)"
  }

  fun tan(angle: Function<X>): Function<X> = object : UnaryFunction<X>(angle) {
    override val value: X
      get() = rfc.tan(arg.value)

    override fun differentiate(ind: Var<X>) = UnivariatePolynomialTerm(1, cos(arg), -2) * arg.differentiate(ind)

    override fun toString(): String = "tan($arg)"
  }

  fun exp(exponent: Function<X>): Function<X> = object : UnaryFunction<X>(exponent) {
    override val value: X
      get() = rfc.exp(arg.value)

    override fun differentiate(ind: Var<X>) = exp(arg) * arg.differentiate(ind)

    override fun toString(): String = "exp($arg)"
  }

  fun log(logarithmand: Function<X>) = object : UnaryFunction<X>(logarithmand) {
    override val value: X
      get() = rfc.log(arg.value)

    override fun differentiate(ind: Var<X>) = Inverse(arg) * arg.differentiate(ind)

    override fun toString(): String = "log₁₀($arg)"
  }

  fun pow(base: Function<X>, exponent: Const<X>): Function<X> = object : BiFunction<X>(base, exponent) {
    override val value: X
      get() = rfc.pow(lfn.value, rfn.value)

    override fun differentiate(ind: Var<X>) = rfn * this@RealFunctor.pow(lfn, this@RealFunctor.value(rfn.value - rfc.one)) * lfn.differentiate(ind)

    override fun toString(): String = "pow($lfn, $rfn)"
  }

  fun sqrt(radicand: Function<X>): Function<X> = object : UnaryFunction<X>(radicand) {
    override val value: X
      get() = rfc.sqrt(arg.value)

    override fun differentiate(ind: Var<X>) = sqrt(arg).inverse() / this@RealFunctor.value(rfc.one * 2L) * arg.differentiate(ind)

    override fun toString(): String = "√($arg)"
  }

  fun square(base: Function<X>) = object : UnaryFunction<X>(base) {
    override val value: X
      get() = rfc.square(arg.value)

    override fun differentiate(ind: Var<X>) = arg * this@RealFunctor.value(rfc.one * 2L) * arg.differentiate(ind)

    override fun toString(): String = "($arg)²"
  }
}