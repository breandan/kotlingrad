package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.functions.ScalarFun
import edu.umontreal.kotlingrad.numerical.DoublePrecision
import edu.umontreal.kotlingrad.numerical.DoubleReal
import io.kotlintest.properties.Gen

open class AdversarialGenerator(vararg val inputs: DoubleReal, val function: ScalarFun<DoubleReal>): Gen<DoubleReal> {
  val alpha = DoubleReal(0.01)
  override fun constants(): Iterable<DoubleReal> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  //  override fun constants() = listOf(0.0) - excluding
  override fun random(): Sequence<DoubleReal> =
    with(DoublePrecision) {
      generateSequence(inputs[0], { it /*- alpha * function.grad().entries.first().value(it)*/ })
    }

//  override fun shrinker() = DoubleShrinker
}