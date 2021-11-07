package ai.hypergraph.kotlingrad.samples

import java.io.File

val resourcesPath =
  File(File("").absolutePath)
    .walk(FileWalkDirection.TOP_DOWN)
    .first { it.name == "samples" }.absolutePath + "/src/main/resources"

infix fun ClosedRange<Double>.step(step: Double): Iterable<Double> {
  require(start.isFinite())
  require(endInclusive.isFinite())
  require(step > 0.0) { "Step must be positive, was: $step." }
  val sequence = generateSequence(start) { previous ->
    if (previous == Double.POSITIVE_INFINITY) return@generateSequence null
    val next = previous + step
    if (next > endInclusive) null else next
  }
  return sequence.asIterable()
}