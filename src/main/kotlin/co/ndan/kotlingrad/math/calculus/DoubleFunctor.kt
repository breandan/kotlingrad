package co.ndan.kotlingrad.math.calculus

import co.ndan.kotlingrad.math.algebra.DoublePrototype
import co.ndan.kotlingrad.math.numerical.Double
import co.ndan.kotlingrad.math.types.Var

object DoubleFunctor: RealFunctor<Double>(DoublePrototype) {
  override fun variable(name: String, default: Double) = Var(name, rfc, default)
  fun variable(name: String, default: Number) = Var(name, rfc, Double(default.toDouble()))
}