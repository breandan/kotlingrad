package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.experimental.DoublePrecision.x
import edu.umontreal.kotlingrad.experimental.DoublePrecision.y
import edu.umontreal.kotlingrad.experimental.DoublePrecision.z
import io.kotlintest.properties.Gen
import io.kotlintest.properties.shrinking.Shrinker

abstract class ExpressionGenerator<X: SFun<X>>: Gen<SFun<X>> {
  companion object: ExpressionGenerator<DReal>() {
    override val variables: List<Var<DReal>> = listOf(x, y, z)
  }

  val sum = { x: SFun<X>, y: SFun<X> -> Sum(x, y) }
  val mul = { x: SFun<X>, y: SFun<X> -> Prod(x, y) }

  val operators: List<(SFun<X>, SFun<X>) -> SFun<X>> = listOf(sum, mul)
  val constants: List<SConst<X>> = listOf(Zero(), One(), Two())
  open val variables: List<Var<X>> = listOf(Var("x"), Var("y"), Var("z"))

  override fun constants(): Iterable<SFun<X>> = constants

  override fun random(): Sequence<SFun<X>> = generateSequence { randomBiTree() }

  override fun shrinker() = object: Shrinker<SFun<X>> {
    override fun shrink(failure: SFun<X>): List<SFun<X>> =
      when(failure) {
        is Sum -> listOf(failure.left, failure.right)
        is Prod -> listOf(failure.left, failure.right)
        else -> emptyList()
      }
  }

  private fun randomBiTree(level: Int = 1): SFun<X> =
    if(5 < level)
      if(Math.random() < 0.5) constants.random() else variables.random()
    else operators.random()(randomBiTree(level + 1), randomBiTree(level + 1))
}