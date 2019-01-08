package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.functions.BinaryFunction
import edu.umontreal.kotlingrad.functions.Function
import edu.umontreal.kotlingrad.numerical.DoubleReal
import edu.umontreal.kotlingrad.numerical.ProtoDouble
import edu.umontreal.kotlingrad.types.Const
import edu.umontreal.kotlingrad.types.Var

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