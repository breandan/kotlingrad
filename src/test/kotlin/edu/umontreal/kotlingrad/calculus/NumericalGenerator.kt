package edu.umontreal.kotlingrad.calculus

import io.kotlintest.properties.Gen
import io.kotlintest.properties.shrinking.DoubleShrinker
import kotlin.math.absoluteValue
import kotlin.math.log10
import kotlin.math.roundToInt

open class NumericalGenerator(vararg exclude: Number): Gen<Double> {
  private val excluding: List<Double> = exclude.map { it.toDouble() }

  companion object: NumericalGenerator()

  override fun constants() = listOf(0.0) - excluding
  override fun random(): Sequence<Double> =
      generateSequence {
        Gen.double().filter {
          it.isFinite() && log10(it.absoluteValue).absoluteValue.roundToInt() in 1..300
        }.random().first()
      }

  override fun shrinker() = DoubleShrinker
}