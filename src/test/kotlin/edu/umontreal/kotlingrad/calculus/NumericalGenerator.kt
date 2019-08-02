package edu.umontreal.kotlingrad.calculus

import io.kotlintest.properties.Gen
import io.kotlintest.properties.shrinking.DoubleShrinker
import kotlin.math.pow
import kotlin.random.Random

open class NumericalGenerator(vararg exclude: Number, val positive: Boolean = false): Gen<Double> {
  private val excluding: List<Double> = exclude.map { it.toDouble() }

  companion object: NumericalGenerator()

  override fun constants() = listOf(0.0) - excluding
  override fun random(): Sequence<Double> =
    generateSequence {
      val r = Random.nextDouble()
      val e = 10.0.pow((-100..100).random().toDouble())
      if (positive) r * e else -e + 2 * e * r
    }

  override fun shrinker() = DoubleShrinker
}