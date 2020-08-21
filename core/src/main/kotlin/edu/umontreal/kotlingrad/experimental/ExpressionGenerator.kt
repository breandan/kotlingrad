package edu.umontreal.kotlingrad.experimental

import kotlin.random.Random

// https://arxiv.org/pdf/1912.01412.pdf#appendix.C
open class ExpressionGenerator<X: RealNumber<X, *>>(
  proto: Protocol<X>, val rand: Random = Random(0),
  val operators: List<(SFun<X>, SFun<X>) -> SFun<X>> = listOf(
    { x: SFun<X>, y: SFun<X> -> x + y },
    { x: SFun<X>, y: SFun<X> -> x - y },
    { x: SFun<X>, y: SFun<X> -> x * y },
    { x: SFun<X>, y: SFun<X> -> x / y },
  )
): Protocol<X>(proto.prototype) {
  infix fun SFun<X>.wildOp(that: SFun<X>) = operators.random(rand)(this, that)

  fun randomBiTree(height: Int = 5): SFun<X> =
    if (height == 0) (listOf(wrap(rand.nextDouble(-1.0, 1.0))) + variables).random(rand)
    else randomBiTree(height - 1) wildOp randomBiTree(height - 1)
}