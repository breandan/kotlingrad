package edu.umontreal.kotlingrad.math.types

import edu.umontreal.kotlingrad.math.algebra.Group
import edu.umontreal.kotlingrad.math.numerical.DoubleReal

operator fun Number.times(multiplicand: Group<*>) = multiplicand * toLong()
operator fun Number.times(multiplicand: DoubleReal) = multiplicand * toDouble()
operator fun Number.plus(addend: DoubleReal) = addend + toDouble()
operator fun Number.minus(addend: DoubleReal) = addend - toDouble()
operator fun Number.div(addend: DoubleReal) = addend / toDouble()