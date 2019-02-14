package edu.umontreal.kotlingrad.utils

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

fun <T> Iterable<T>.repeat(n: Int) = sequence { repeat(n) { yieldAll(this@repeat) } }

fun randomDefaultName() = (('a'..'z') - 'q').map { it }.shuffled().subList(0, 4).joinToString("")

fun String.superscript() = this
    .replace(".", "⋅")
    .replace("-", "⁻")
    .replace("+", "⁺")
    .replace("0", "⁰")
    .replace("1", "¹")
    .replace("2", "²")
    .replace("3", "³")
    .replace("4", "⁴")
    .replace("5", "⁵")
    .replace("6", "⁶")
    .replace("7", "⁷")
    .replace("8", "⁸")
    .replace("9", "⁹")
    .replace("a", "ᵃ")
    .replace("b", "ᵇ")
    .replace("c", "ᶜ")
    .replace("d", "ᵈ")
    .replace("e", "ᵉ")
    .replace("f", "ᶠ")
    .replace("g", "ᵍ")
    .replace("h", "ʰ")
    .replace("i", "ⁱ")
    .replace("j", "ʲ")
    .replace("k", "ᵏ")
    .replace("l", "ˡ")
    .replace("m", "ᵐ")
    .replace("n", "ⁿ")
    .replace("o", "ᵒ")
    .replace("p", "ᵖ")
    .replace("r", "ʳ")
    .replace("s", "ˢ")
    .replace("t", "ᵗ")
    .replace("u", "ᵘ")
    .replace("v", "ᵛ")
    .replace("w", "ʷ")
    .replace("x", "ˣ")
    .replace("y", "ʸ")
    .replace("z", "ᶻ")