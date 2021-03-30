package edu.umontreal.kotlingrad.api

import kotlin.random.Random

// https://arxiv.org/pdf/1912.01412.pdf#appendix.C
open class ExpressionGenerator<X: RealNumber<X, *>>(
  val rand: Random = Random.Default,
  val operators: List<(SFun<X>, SFun<X>) -> SFun<X>> = listOf(
    { x: SFun<X>, y: SFun<X> -> x + y },
    { x: SFun<X>, y: SFun<X> -> x - y },
    { x: SFun<X>, y: SFun<X> -> x * y },
    { x: SFun<X>, y: SFun<X> -> x / y },
  )
) {
  val x by SVar<X>()
  val y by SVar<X>()
  val z by SVar<X>()
  open val variables = listOf(x, y, z)

  infix fun SFun<X>.wildOp(that: SFun<X>) = operators.random(rand)(this, that)

  fun randomConst(): SFun<X> = x.wrap(rand.nextDouble(-1.0, 1.0))

  fun randomBiTree(height: Int = 5): SFun<X> =
    if (height == 0) (variables + randomConst()).random()
    else randomBiTree(height - 1) wildOp randomBiTree(height - 1)
}