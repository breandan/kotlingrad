package co.ndan.kotlingrad.math.calculus

import co.ndan.kotlingrad.math.algebra.*
import co.ndan.kotlingrad.math.types.*
import java.util.*

class RealFunctor<X : Real<X>>(val rfc: RealPrototype<X>) {
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

  fun cos(fnx: Function<X>) =
    object : UnaryFunction<X>(fnx) {
      override val value: X
        get() = rfc.cos(this.fnx.value)

      override fun differentiate(arg: Var<X>) = (sin(this.fnx) * this.fnx.differentiate(arg)).unaryMinus()

      override fun toString() = "cos(${this.fnx})"
    }

  fun sin(fnx: Function<X>): Function<X> =
    object : UnaryFunction<X>(fnx) {
      override val value: X
        get() = rfc.sin(this.fnx.value)

      override fun differentiate(arg: Var<X>) = cos(this.fnx) * this.fnx.differentiate(arg)

      override fun toString(): String = "sin(${this.fnx})"
    }

  fun tan(fnx: Function<X>): Function<X> =
    object : UnaryFunction<X>(fnx) {
      override val value: X
        get() = rfc.tan(this.fnx.value)

      override fun differentiate(arg: Var<X>) = UnivariatePolynomialTerm(1, cos(this.fnx), -2) * this.fnx.differentiate(arg)

      override fun toString(): String = "tan(${this.fnx})"
    }

  fun exp(fnx: Function<X>): Function<X> =
    object : UnaryFunction<X>(fnx) {
      override val value: X
        get() = rfc.exp(this.fnx.value)

      override fun differentiate(arg: Var<X>) = exp(this.fnx) * this.fnx.differentiate(arg)

      override fun toString(): String = "exp(${this.fnx})"
    }

  fun log(fnx: Function<X>) =
    object : UnaryFunction<X>(fnx) {

      override val value: X
        get() = rfc.log(this.fnx.value)

      override fun differentiate(arg: Var<X>) = Inverse(this.fnx).times(this.fnx.differentiate(arg))

      override fun toString(): String = "log(${this.fnx})"
    }

  fun pow(base: Function<X>, exponent: Const<X>): Function<X> =
    object : BiFunction<X>(base, exponent) {
      override val value: X
        get() = rfc.pow(lfn.value, rfn.value)

      override fun differentiate(arg: Var<X>) = rfn * this@RealFunctor.pow(lfn, this@RealFunctor.value(rfn.value - rfc.one)) * lfn.differentiate(arg)

      override fun toString(): String = "pow($lfn, $rfn)"
    }

  fun sqrt(radicand: Function<X>): Function<X> =
    object : UnaryFunction<X>(radicand) {
      override val value: X
        get() = rfc.sqrt(this.fnx.value)

      override fun differentiate(arg: Var<X>) = sqrt(this.fnx).inverse() / this@RealFunctor.value(rfc.one * 2L) * this.fnx.differentiate(arg)

      override fun toString(): String = "sqrt(${this.fnx})"
    }

  fun square(base: Function<X>) =
    object : UnaryFunction<X>(base) {
      override val value: X
        get() = rfc.square(this.fnx.value)

      override fun differentiate(arg: Var<X>) = this.fnx * this@RealFunctor.value(rfc.one * 2L) * this.fnx.differentiate(arg)

      override fun toString(): String = "square(${this.fnx})"
    }
}
