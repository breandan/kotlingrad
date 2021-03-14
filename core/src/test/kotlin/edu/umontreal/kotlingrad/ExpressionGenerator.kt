package edu.umontreal.kotlingrad

import edu.umontreal.kotlingrad.api.*
import io.kotlintest.properties.Gen
import io.kotlintest.properties.shrinking.Shrinker
import kotlin.math.pow

class TestExpressionGenerator<X : RealNumber<X, *>>(proto: X) :
  ExpressionGenerator<X>(), Gen<SFun<X>> {
  val constants = List(100) { SConst<X>(rand.nextDouble() * 10.0.pow(rand.nextInt(5))) }

  override fun constants(): Iterable<SFun<X>> = constants + variables

  override fun random(): Sequence<SFun<X>> = generateSequence { randomBiTree() }

  override fun shrinker() = object : Shrinker<SFun<X>> {
    override fun shrink(failure: SFun<X>): List<SFun<X>> =
      when (failure) {
        is Sum -> listOf(failure.left, failure.right)
        is Prod -> listOf(failure.left, failure.right)
        else -> emptyList()
      }
  }
}