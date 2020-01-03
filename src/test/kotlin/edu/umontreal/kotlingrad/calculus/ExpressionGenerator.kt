package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.samples.*
import edu.umontreal.kotlingrad.samples.DoublePrecision.x
import edu.umontreal.kotlingrad.samples.DoublePrecision.y
import edu.umontreal.kotlingrad.samples.DoublePrecision.z
import io.kotlintest.properties.Gen

abstract class ExpressionGenerator<X: Fun<X>>: Gen<Fun<X>> {
  companion object: ExpressionGenerator<DoubleReal>() {
    override val variables: List<Var<DoubleReal>> = listOf(x, y, z)
  }

  val sum = { x: Fun<X>, y: Fun<X> -> Sum(x, y) }
  val mul = { x: Fun<X>, y: Fun<X> -> Prod(x, y) }

  val operators: List<(Fun<X>, Fun<X>) -> Fun<X>> = listOf(sum, mul)
  val constants: List<SConst<X>> = listOf(Zero(), One(), Two())
  open val variables: List<Var<X>> = listOf(Var("x"), Var("y"), Var("z"))

  override fun constants(): Iterable<Fun<X>> = constants

  override fun random(): Sequence<Fun<X>> = generateSequence { randomBiTree() }

  private fun randomBiTree(level: Int = 1): Fun<X> =
    if(2 < level)
      if(Math.random() < 0.5) constants.random() else variables.random()
    else operators.random()(randomBiTree(level + 1), randomBiTree(level + 1))
}