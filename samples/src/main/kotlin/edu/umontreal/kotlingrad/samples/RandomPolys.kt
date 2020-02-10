package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.experimental.DoublePrecision.invoke
import edu.umontreal.kotlingrad.experimental.DoublePrecision.saveToFile
import edu.umontreal.kotlingrad.experimental.DoublePrecision.toDouble
import edu.umontreal.kotlingrad.utils.step
import kotlin.math.absoluteValue
import kotlin.random.Random

fun main() {
  val rand = Random(5)
  val eg = ExpressionGenerator(rand)

  for (i in 0..9) {
    val bt = eg.scaledRandomBiTree(5)
    plotOracle("oracle$i.svg", 1.0) { bt(it).toDouble() }
    bt.saveToFile("btree_rand$i.dot")
    ((-1.0..1.0) step 0.01).joinToString("\n") { "$it, ${bt(it)}" }.saveAs("btree_rand$i.csv")
  }
}

class ExpressionGenerator(val rng: Random): Protocol<DReal>(DReal) {
  val sum = { x: SFun<DReal>, y: SFun<DReal> -> x + y }
  val mul = { x: SFun<DReal>, y: SFun<DReal> -> x * y }

  val operators = listOf(sum, mul)
  override val variables = listOf(x)

  infix fun SFun<DReal>.wildOp(that: SFun<DReal>) = operators.random(rng)(this, that)

  fun randomBiTree(height: Int = 5): SFun<DReal> =
    if (height == 0) (listOf(wrap(rng.nextDouble(-1.0, 1.0))) + variables).random(rng)
    else randomBiTree(height - 1) wildOp randomBiTree(height - 1)

  fun scaledRandomBiTree(height: Int = 4, maxX: Double = 1.0, maxY: Double = 1.0) =
    randomBiTree(height).let { it - it.invoke(0.0) }.let {
      it * wrap(maxY) / ((-maxX..maxX) step 0.01).toList()
        .map { num -> it(num).toDouble().absoluteValue }.max()!!
    }
}

fun plotOracle(filename: String, maxX: Double = 1.0, oracle: (Double) -> Double) {
  val t = ((-maxX..maxX) step 0.01).toList()
  mapOf( "x" to t, "y" to t.map { oracle(it) }).plot2D("Oracle", filename)
}