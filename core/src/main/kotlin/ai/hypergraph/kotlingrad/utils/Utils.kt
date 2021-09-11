package ai.hypergraph.kotlingrad.utils

import org.jetbrains.bio.viktor.F64Array

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

fun F64Array.toKotlinArray() =
  toGenericArray().map { it as DoubleArray }.toTypedArray()

infix fun F64Array.matmul(f: F64Array) =
  F64Array(shape[0], f.shape[1]) { i, j -> view(i) dot f.view(j, 1) }

fun <T> Iterable<T>.repeat(n: Int) =
  sequence { repeat(n) { yieldAll(this@repeat) } }

fun randomDefaultName() =
  (('a'..'z') - 'q').map { it }.shuffled().subList(0, 4).joinToString("")

fun String.superscript() =
  map {
    when (it) {
      '.' -> '⋅'
      '-' -> '⁻'
      '+' -> '⁺'
      '⋅' -> '˙'
      '(' -> '⁽'
      ')' -> '⁾'
      '0' -> '⁰'
      '1' -> '¹'
      '2' -> '²'
      '3' -> '³'
      '4' -> '⁴'
      '5' -> '⁵'
      '6' -> '⁶'
      '7' -> '⁷'
      '8' -> '⁸'
      '9' -> '⁹'
      'a' -> 'ᵃ'
      'b' -> 'ᵇ'
      'c' -> 'ᶜ'
      'd' -> 'ᵈ'
      'e' -> 'ᵉ'
      'f' -> 'ᶠ'
      'g' -> 'ᵍ'
      'h' -> 'ʰ'
      'i' -> 'ⁱ'
      'j' -> 'ʲ'
      'k' -> 'ᵏ'
      'l' -> 'ˡ'
      'm' -> 'ᵐ'
      'n' -> 'ⁿ'
      'o' -> 'ᵒ'
      'p' -> 'ᵖ'
      'r' -> 'ʳ'
      's' -> 'ˢ'
      't' -> 'ᵗ'
      'u' -> 'ᵘ'
      'v' -> 'ᵛ'
      'w' -> 'ʷ'
      'x' -> 'ˣ'
      'y' -> 'ʸ'
      'z' -> 'ᶻ'
      else -> it
    }
  }.joinToString("").replace(" ", "")