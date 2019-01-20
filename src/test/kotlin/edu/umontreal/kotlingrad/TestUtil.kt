package edu.umontreal.kotlingrad

import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.shouldBe

@Suppress("NonAsciiCharacters")
val ε: Double = 1E-7

infix fun Double.shouldBeAbout(d: Double) = if (isNaN() && d.isNaN()) Unit else this shouldBe (d plusOrMinus ε)

infix fun Number.shouldBeAbout(d: Number) = this.toDouble() shouldBe d.toDouble()

infix fun Number.shouldBeAbout(d: Double) = this.toDouble() shouldBeAbout d

infix fun Double.shouldBeAbout(d: Number) = this shouldBeAbout d.toDouble()