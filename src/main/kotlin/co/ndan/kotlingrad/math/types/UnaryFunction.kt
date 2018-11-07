package co.ndan.kotlingrad.math.types

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.calculus.Function

abstract class UnaryFunction<X : Field<X>>(open val arg: Function<X>) : Function<X>()