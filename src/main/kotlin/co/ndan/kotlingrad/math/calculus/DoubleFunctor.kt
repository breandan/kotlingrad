package co.ndan.kotlingrad.math.calculus

import co.ndan.kotlingrad.math.algebra.DoublePrototype
import co.ndan.kotlingrad.math.numerical.Double
import co.ndan.kotlingrad.math.types.Var

object DoubleFunctor: RealFunctor<Double>(DoublePrototype) {
  override fun variable(name: String, x: Double) = Var(name, x, rfc)
  fun variable(name: String, x: Number) = Var(name, Double(x.toDouble()), rfc)
}