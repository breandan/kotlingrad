package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.experimental.DoublePrecision.x
import edu.umontreal.kotlingrad.experimental.DoublePrecision.y
import edu.umontreal.kotlingrad.experimental.DoublePrecision.z
import io.kotlintest.properties.Gen
import io.kotlintest.properties.shrinking.Shrinker

abstract class ExpressionGenerator<X: Fun<X>>: Gen<Fun<X>> {
  companion object: ExpressionGenerator<DReal>() {
    override val variables: List<Var<DReal>> = listOf(x, y, z)
  }

  val sum = { x: Fun<X>, y: Fun<X> -> Sum(x, y) }
  val mul = { x: Fun<X>, y: Fun<X> -> Prod(x, y) }

  val operators: List<(Fun<X>, Fun<X>) -> Fun<X>> = listOf(sum, mul)
  val constants: List<SConst<X>> = listOf(Zero(), One(), Two())
  open val variables: List<Var<X>> = listOf(Var("x"), Var("y"), Var("z"))

  override fun constants(): Iterable<Fun<X>> = constants

  override fun random(): Sequence<Fun<X>> = generateSequence { randomBiTree() }

  override fun shrinker() = object: Shrinker<Fun<X>> {
    override fun shrink(failure: Fun<X>): List<Fun<X>> =
      when(failure) {
        is Sum -> listOf(failure.left, failure.right)
        is Prod -> listOf(failure.left, failure.right)
        else -> emptyList()
      }
  }

  private fun randomBiTree(level: Int = 1): Fun<X> =
    if(5 < level)
      if(Math.random() < 0.5) constants.random() else variables.random()
    else operators.random()(randomBiTree(level + 1), randomBiTree(level + 1))
}