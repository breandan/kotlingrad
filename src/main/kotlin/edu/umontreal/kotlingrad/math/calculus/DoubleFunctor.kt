package edu.umontreal.kotlingrad.math.calculus

import edu.umontreal.kotlingrad.math.functions.BinaryFunction
import edu.umontreal.kotlingrad.math.functions.Function
import edu.umontreal.kotlingrad.math.numerical.DoubleReal
import edu.umontreal.kotlingrad.math.numerical.ProtoDouble
import edu.umontreal.kotlingrad.math.types.Const
import edu.umontreal.kotlingrad.math.types.Var

object DoubleFunctor: RealFunctor<DoubleReal>(ProtoDouble) {
  fun variable(default: Number) = Var(rfc, DoubleReal(default.toDouble()))

  fun variable(name: String, default: Number) =
      Var(rfc, DoubleReal(default.toDouble()), name)

  fun pow(base: Function<DoubleReal>, number: Int): BinaryFunction<DoubleReal> =
      pow(base, Const(DoubleReal(number), rfc))

  operator fun Function<DoubleReal>.invoke(pairs: Map<Var<DoubleReal>, Number>) =
      this(pairs.map { (it.key to DoubleReal(it.value)) }.toMap()).dbl

  operator fun Function<DoubleReal>.invoke(vararg pairs: Pair<Var<DoubleReal>, Number>) =
      this(pairs.map { (it.first to DoubleReal(it.second)) }.toMap()).dbl

  operator fun Function<DoubleReal>.invoke(vararg number: Number) =
      this(independentVariables().zip(number).toMap())
}