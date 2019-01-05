package co.ndan.kotlingrad.math.calculus

import co.ndan.kotlingrad.math.algebra.DoublePrototype
import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.numerical.DoubleReal
import co.ndan.kotlingrad.math.types.Var

object DoubleFunctor: RealFunctor<DoubleReal>(DoublePrototype) {
  fun variable(default: Number) = Var(rfc, DoubleReal(default.toDouble()))

  fun variable(name: String, default: Number) = Var(rfc, DoubleReal(default.toDouble()), name)
}