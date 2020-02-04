package edu.umontreal.kotlingrad

import edu.umontreal.kotlingrad.experimental.D1
import edu.umontreal.kotlingrad.experimental.DReal
import edu.umontreal.kotlingrad.experimental.Mat
import edu.umontreal.kotlingrad.experimental.Vec
import io.kotlintest.shouldBe
import java.math.BigDecimal
import java.math.MathContext
import kotlin.random.Random

infix fun Double.shouldBeAbout(d: Double) =
  if (isNaN() && d.isNaN() || d.isInfinite() && isInfinite()) Unit
  else round() shouldBe d.round()

infix fun Any.shouldBeAbout(d: Double) = toString().toDoubleOrNull()?.shouldBeAbout(d) ?: false
infix fun Double.shouldBeAbout(d: Any) = d.toString().toDoubleOrNull()?.shouldBeAbout(this) ?: false
infix fun Any.shouldBeAbout(d: Any) = toString().toDoubleOrNull()?.shouldBeAbout(d.toString().toDouble()) ?: false
infix fun <T: Mat<DReal, R, C>, R: D1, C: D1> T.shouldBeAbout(t: T) =
  flattened.zip(t.flattened).forEach { it.first shouldBeAbout it.second }
infix fun <R: D1> Vec<DReal, R>.shouldBeAbout(t: Vec<DReal, R>) =
  contents.zip(t.contents).forEach { it.first shouldBeAbout it.second }

fun Double.round(precision: Int = 10) = BigDecimal(this, MathContext(precision)).toDouble()

infix fun Number.shouldBeAbout(d: Number) = this.toDouble() shouldBe d.toDouble()

infix fun Number.shouldBeAbout(d: Double) = this.toDouble() shouldBeAbout d

infix fun Double.shouldBeAbout(d: Number) = this shouldBeAbout d.toDouble()

val seededRandom = Random(4)