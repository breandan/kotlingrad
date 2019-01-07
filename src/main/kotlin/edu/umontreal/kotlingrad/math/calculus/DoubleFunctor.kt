package edu.umontreal.kotlingrad.math.calculus

import edu.umontreal.kotlingrad.math.functions.Function
import edu.umontreal.kotlingrad.math.numerical.DoubleReal
import edu.umontreal.kotlingrad.math.numerical.ProtoDouble
import edu.umontreal.kotlingrad.math.types.Var

object DoubleFunctor: RealFunctor<DoubleReal>(ProtoDouble) {
  fun variable(default: Number) = Var(rfc, DoubleReal(default.toDouble()))

  fun variable(name: String, default: Number) = Var(rfc, DoubleReal(default.toDouble()), name)

  operator fun Function<DoubleReal>.invoke(pairs: Map<Var<DoubleReal>, Number>) =
      this.invoke(pairs.map { Pair(it.key, DoubleReal(it.value)) }.toMap()).dbl

  operator fun Function<DoubleReal>.invoke(vararg pairs: Pair<Var<DoubleReal>, Number>) =
      this.invoke(pairs.map { Pair(it.first, DoubleReal(it.second)) }.toMap()).dbl
}