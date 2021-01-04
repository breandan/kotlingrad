package edu.umontreal.kotlingrad.typelevel

import java.math.BigInteger.*

// https://kotlin.christmas/2020/7
interface Addable<T> {
  val one: T
  val nil: T
  operator fun T.plus(t: T): T

  companion object {
    operator fun <T> invoke(nil: T, one: T, plus: (T, T) -> T): Addable<T> =
      object: Addable<T> {
        override fun T.plus(t: T) = plus(this, t)
        override val nil: T = nil
        override val one: T = one
      }
  }
}

/** Corecursive Fibonacci sequence of [Addable]s **/
tailrec fun <T> Addable<T>.fibCorec(
  n: T,
  seed: Pair<T, T> = nil to one,
  fib: (Pair<T, T>) -> Pair<T, T> = { (a, b) -> b to a + b },
  i: T = nil,
): T =
  if (i == n) fib(seed).first
  else fibCorec(n = n, seed = fib(seed), i = i + one)

/** Returns the product of two [Addable]s **/
tailrec fun <T> Addable<T>.times(l: T, r: T, acc: T = nil, i: T = nil): T =
  if (i == r) acc else times(l, r, acc + l, i + one)

/** Returns a sequence of [Addable]s starting with [from] until [to] **/
tailrec fun <T> Addable<T>.seq(
  from: T = one,
  to: T,
  acc: Set<T> = emptySet()
): Set<T> =
  if (from == to) acc else seq(from + one, to, acc + from)

/** Returns whether an [Addable] is prime **/
fun <T> Addable<T>.isPrime(t: T, kps: Set<T> = emptySet()): Boolean =
  // Take Cartesian product, take only distinct pairs due to commutativity
  (if (kps.isNotEmpty()) kps * kps
  else seq(to = t) * seq(to = t))
    .distinctBy { (l, r) -> setOf(l, r) }
    .all { (i, j) -> if (i == one || j == one) true else times(i, j) != t }

/** Prints [t] prime [Addable]s **/
tailrec fun <T> Addable<T>.printPrimes(
  t: T, // number of primes
  i: T = nil, // counter
  c: T = one + one, // prime candidate
  kps: Set<T> = emptySet() // known primes
): Unit =
  when {
    i == t -> Unit
    isPrime(c) -> {
      print("$c, "); printPrimes(t, i + one, c + one, kps + c)
    }
    else -> printPrimes(t, i, c + one, kps)
  }

// Returns the Cartesian product of two sets
operator fun <T> Set<T>.times(s: Set<T>) =
  flatMap { l -> s.map { r -> l to r } }

fun main() {
  Addable(ZERO, ONE) { a, b -> a + b }.run {
    val q = fibCorec(valueOf(100))
    println(q)
    println(times(valueOf(5), valueOf(5)))
    printPrimes(valueOf(10))
  }
}