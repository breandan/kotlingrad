package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.Group

operator fun Number.times(multiplicand: Group<*>) = multiplicand * toLong()
operator fun Number.times(multiplicand: Double) = multiplicand * toDouble()
operator fun Number.plus(addend: Double) = addend + toDouble()
operator fun Number.minus(addend: Double) = addend - toDouble()
operator fun Number.div(addend: Double) = addend / toDouble()