package edu.umontreal.kotlingrad

import io.kotlintest.properties.Gen
import io.kotlintest.properties.shrinking.*
import kotlin.math.pow

open class DoubleGenerator(
  vararg exclude: Number,
  val positive: Boolean = false,
  val expRange: IntRange = -100..100
): Gen<Double> {
  private val excluding: List<Double> = exclude.map { it.toDouble() }

  companion object: DoubleGenerator()

  override fun constants() = listOf(0.0) - excluding
  override fun random(): Sequence<Double> =
    generateSequence {
      val r = seededRandom.nextDouble()
      val e = 10.0.pow(expRange.random().toDouble())
      if (positive) r * e else -e + 2 * e * r
    } - excluding

  override fun shrinker() = object : Shrinker<Double> {
    override fun shrink(failure: Double) = DoubleShrinker.shrink(failure) - excluding
  }
}