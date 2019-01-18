package edu.umontreal.kotlingrad

import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.shouldBe

@Suppress("NonAsciiCharacters")
val ε: Double = 1E-7

infix fun Number.shouldBeAbout(d: Number) = this.toDouble() shouldBe (d.toDouble() plusOrMinus ε)

infix fun Number.shouldBeAbout(d: Double) = this.toDouble() shouldBe (d plusOrMinus ε)

infix fun Double.shouldBeAbout(d: Double) = this shouldBe (d plusOrMinus ε)

infix fun Double.shouldBeAbout(d: Number) = this shouldBe (d.toDouble() plusOrMinus ε)