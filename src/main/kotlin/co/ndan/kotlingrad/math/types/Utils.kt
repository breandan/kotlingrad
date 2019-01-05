package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.Group
import co.ndan.kotlingrad.math.numerical.DoubleReal

operator fun Number.times(multiplicand: Group<*>) = multiplicand * toLong()
operator fun Number.times(multiplicand: DoubleReal) = multiplicand * toDouble()
operator fun Number.plus(addend: DoubleReal) = addend + toDouble()
operator fun Number.minus(addend: DoubleReal) = addend - toDouble()
operator fun Number.div(addend: DoubleReal) = addend / toDouble()