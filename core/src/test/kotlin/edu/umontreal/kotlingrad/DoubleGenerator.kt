package edu.umontreal.kotlingrad

import io.kotest.property.*
import kotlin.math.pow
import kotlin.random.Random

open class DoubleGenerator(
  vararg exclude: Number,
  val positive: Boolean = false,
  val expRange: IntRange = -100..100
): Arb<Double>() {
  private val excluding: List<Sample<Double>> = exclude.map { Sample(it.toDouble()) }

  companion object: DoubleGenerator()

  override fun values(rs: RandomSource): Sequence<Sample<Double>> =
    generateSequence {
      val r = Random.Default.nextDouble()
      val e = 10.0.pow(expRange.random().toDouble())
      Sample(if (positive) r * e else -e + 2 * e * r)
    } - excluding

  override fun edgecases() = listOf<Double>()
}