package edu.umontreal.kotlingrad

import io.kotlintest.shouldBe
import java.math.BigDecimal
import java.math.MathContext

infix fun Double.shouldBeAbout(d: Double) =
  if (isNaN() && d.isNaN() || d.isInfinite() && isInfinite()) Unit
  else round() shouldBe d.round()

infix fun Any.shouldBeAbout(d: Double) = this.toString().toDoubleOrNull()?.shouldBeAbout(d) ?: false
infix fun Double.shouldBeAbout(d: Any) = d.toString().toDoubleOrNull()?.shouldBeAbout(this) ?: false
infix fun Any.shouldBeAbout(d: Any) = this.toString().toDoubleOrNull()?.shouldBeAbout(d.toString().toDouble()) ?: false

fun Double.round(precision: Int = 10) = BigDecimal(this, MathContext(precision)).toDouble()

infix fun Number.shouldBeAbout(d: Number) = this.toDouble() shouldBe d.toDouble()

infix fun Number.shouldBeAbout(d: Double) = this.toDouble() shouldBeAbout d

infix fun Double.shouldBeAbout(d: Number) = this shouldBeAbout d.toDouble()