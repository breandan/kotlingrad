package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.experimental.*
import io.kotlintest.properties.Gen
import io.kotlintest.properties.shrinking.Shrinker
import kotlin.math.pow
import kotlin.random.Random

class ExpressionGenerator<X: SFun<X>>(proto: Protocol<X>): Gen<SFun<X>> {
  val sum = { x: SFun<X>, y: SFun<X> -> x + y }
  val sub = { x: SFun<X>, y: SFun<X> -> x - y }
  val mul = { x: SFun<X>, y: SFun<X> -> x * y }
  val div = { x: SFun<X>, y: SFun<X> -> x / y }

  val rng = System.currentTimeMillis().let { println("Seed: $it"); Random(it) }
  val operators = listOf(sum, sub, mul, div)
  val constants = List(100) { proto.wrap(rng.nextDouble() * 10.0.pow(rng.nextInt(5))) }
  val variables = constants.map { proto.variables.random(rng) }

  override fun constants(): Iterable<SFun<X>> = constants + variables

  override fun random(): Sequence<SFun<X>> = generateSequence { randomBiTree() }

  override fun shrinker() = object: Shrinker<SFun<X>> {
    override fun shrink(failure: SFun<X>): List<SFun<X>> =
      when (failure) {
        is Sum -> listOf(failure.left, failure.right)
        is Prod -> listOf(failure.left, failure.right)
        else -> emptyList()
      }
  }

  infix fun SFun<X>.wildOp(that: SFun<X>) = operators.random(rng)(this, that)

  private fun randomBiTree(height: Int = 5): SFun<X> =
    if (height == 0) (constants + variables).random(rng)
    else randomBiTree(height - 1) wildOp randomBiTree(height - 1)
}