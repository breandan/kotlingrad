package edu.umontreal.kotlingrad

import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.shouldBe

@Suppress("NonAsciiCharacters")
val ε: Double = 1E-7
infix fun Double.shouldBeAbout(d: Double) = this shouldBe (d plusOrMinus ε)