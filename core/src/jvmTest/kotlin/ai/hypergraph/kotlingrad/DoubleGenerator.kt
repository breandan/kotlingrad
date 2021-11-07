package ai.hypergraph.kotlingrad

import io.kotest.property.*
import kotlin.math.pow

open class DoubleGenerator(
  vararg exclude: Number,
  val positive: Boolean = false,
  val expRange: IntRange = -100..100
): Arb<Double>() {
  private val excluding: List<Sample<Double>> = exclude.map { Sample(it.toDouble()) }

  companion object: DoubleGenerator()

  override fun sample(rs: RandomSource) =
    (generateSequence {
      val r = rs.random.nextDouble()
      val e = 10.0.pow(expRange.random().toDouble())
      Sample(if (positive) r * e else -e + 2 * e * r)
    } - excluding).first()

  override fun edgecase(rs: RandomSource) = null
}