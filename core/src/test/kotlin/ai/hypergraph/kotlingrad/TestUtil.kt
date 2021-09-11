package ai.hypergraph.kotlingrad

import ai.hypergraph.kotlingrad.api.*
import ai.hypergraph.kotlingrad.shapes.D1
import io.kotest.matchers.shouldBe
import java.math.*

infix fun Double.shouldBeAbout(d: Double) =
  if (isNaN() && d.isNaN() || d.isInfinite() && isInfinite()) Unit
  else round() shouldBe d.round()

infix fun Any.shouldBeAbout(d: Double) = toString().toDouble().shouldBeAbout(d)
infix fun Double.shouldBeAbout(d: Any) = d.toString().toDouble().shouldBeAbout(this)
infix fun Any.shouldBeAbout(d: Any) = toString().toDouble().shouldBeAbout(d.toString().toDouble())
inline infix fun <reified R: D1, reified C: D1> Mat<DReal, R, C>.shouldBeAbout(t: Mat<DReal, R, C>) =
  flattened.zip(t.flattened).forEach { it.first shouldBeAbout it.second }
inline infix fun <reified T: D1> Vec<DReal, T>.shouldBeAbout(t: Vec<DReal, T>) =
  contents.zip(t.contents).forEach { it.first shouldBeAbout it.second }

fun Double.round(precision: Int = 10) = BigDecimal(this, MathContext(precision)).toDouble()

infix fun Number.shouldBeAbout(d: Number) = this.toDouble() shouldBe d.toDouble()

infix fun Number.shouldBeAbout(d: Double) = this.toDouble() shouldBeAbout d

infix fun Double.shouldBeAbout(d: Number) = this shouldBeAbout d.toDouble()