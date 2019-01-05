package edu.umontreal.kotlingrad.math.calculus

import edu.umontreal.kotlingrad.math.numerical.DoubleReal
import edu.umontreal.kotlingrad.math.numerical.ProtoDouble
import edu.umontreal.kotlingrad.math.types.Var

object DoubleFunctor: RealFunctor<DoubleReal>(ProtoDouble) {
  fun variable(default: Number) = Var(rfc, DoubleReal(default.toDouble()))

  fun variable(name: String, default: Number) = Var(rfc, DoubleReal(default.toDouble()), name)
}