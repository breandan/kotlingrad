package edu.umontreal.kotlingrad.samples

tailrec fun <I, O : Comparable<O>> minimize(
    fn: (I) -> (O), min: I, budget: Int): I =
    if (budget <= 0) min
    else minimize(fn, random<I>().let { input ->
      if (fn(input) < fn(min)) input else min
    }, budget - 1)

fun <I> random(): I = TODO()

interface Metric<T : Metric<T>> : Comparable<T> {
  operator fun plus(metric: T): T
  operator fun minus(metric: T): T
}

tailrec fun <I, O : Metric<O>> minimizeMetric(
    fn: (I) -> (O), min: I, budget: Int): I =
    if (budget <= 0) min
    else minimizeMetric(fn, wiggle(min).filter { fn(it) < fn(min) }
        .maxBy { fn(min) - fn(it) } ?: min, budget - 1)

fun <I> wiggle(min: I): Sequence<I> = TODO()

tailrec fun <T : Field<T>> minimizeField(
    fn: (T) -> (T), a: T, least: T, budget: Int): T =
    if (budget <= 0) least
    else minimizeField(fn, a,
        least - (fn(least) - fn(least + a)) / a,
        budget - 1)

val t: (Int, Int) -> Pair<Int, Int>? = { i, x -> null }