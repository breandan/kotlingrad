package edu.umontreal.kotlingrad.calculus

import io.kotlintest.properties.Gen
import io.kotlintest.properties.shrinking.DoubleShrinker

open class NumericalGenerator(vararg exclude: Number, val positive: Boolean = false): Gen<Double> {
  private val excluding: List<Double> = exclude.map { it.toDouble() }

  companion object: NumericalGenerator()

  override fun constants() = listOf(0.0) - excluding
  override fun random(): Sequence<Double> =
    generateSequence {
      val r = Math.random()
      val e = Math.pow(10.0, (-100..100).random().toDouble())
      if (positive) r * e else -e + 2 * e * r
    }

  override fun shrinker() = DoubleShrinker
}