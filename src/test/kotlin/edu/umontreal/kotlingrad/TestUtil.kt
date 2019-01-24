package edu.umontreal.kotlingrad

import io.kotlintest.shouldBe
import java.math.BigDecimal
import java.math.MathContext

infix fun Double.shouldBeAbout(d: Double) =
  if (isNaN() && d.isNaN() || d.isInfinite() && isInfinite()) Unit
  else round() shouldBe d.round()

fun Double.round(precision: Int = 10) = BigDecimal(this, MathContext(precision)).toDouble()

infix fun Number.shouldBeAbout(d: Number) = this.toDouble() shouldBe d.toDouble()

infix fun Number.shouldBeAbout(d: Double) = this.toDouble() shouldBeAbout d

infix fun Double.shouldBeAbout(d: Number) = this shouldBeAbout d.toDouble()