package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.calculus.Function

abstract class BiFunction<X : Field<X>>(val rfn: Function<X>, val lfn: Function<X>) : Function<X>()