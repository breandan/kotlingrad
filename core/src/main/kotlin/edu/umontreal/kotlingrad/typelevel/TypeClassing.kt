package edu.umontreal.kotlingrad.typelevel

import java.math.BigInteger.*
import kotlin.system.measureTimeMillis

fun main() {
  Ring(
    nil = ZERO,
    one = ONE,
    plus = { a, b -> a + b },
    times = { a, b -> a * b }
  ).run {
    measureTimeMillis {
      printFibNum(valueOf(20))
      printPrimes(valueOf(20))
    }.also { ms -> println("Took ${ms}ms") }
  }

  Ring(
    nil = ZERO,
    one = ONE,
    plus = { a, b -> a + b }
  ).run {
    measureTimeMillis {
      printFibNum(valueOf(20))
      printPrimes(valueOf(20))
    }.also { ms -> println("Took ${ms}ms") }
  }

  Nat(
    nil = ZERO,
    succ = { a -> a + ONE }
  ).run {
    measureTimeMillis {
      printFibNum(valueOf(20))
      printPrimes(valueOf(20))
    }.also { ms -> println("Took ${ms}ms") }
  }

  Ring(
    nil = GaussianInteger(0, 0),
    one = GaussianInteger(1, 1), // http://www.math.ucsd.edu/~alina/oldcourses/2012/104b/zi.pdf
    plus = { a, b -> GaussianInteger(a.a + b.a, a.b + b.b) },
    times = { a, b -> GaussianInteger(a.a * b.a - a.b * b.b, a.a * b.b + a.b * b.a) },
  )
}

// TODO
data class GaussianInteger(val a: Int, val b: Int)

/** Corecursive Fibonacci sequence of [Nat]s **/
tailrec fun <T> Nat<T>.printFibNum(
  n: T,
  seed: Pair<T, T> = nil to one,
  fib: (Pair<T, T>) -> Pair<T, T> = { (a, b) -> b to a + b },
  i: T = nil,
): T =
  if (i == n) fib(seed).first
  else printFibNum(
    n = n,
    seed = fib(seed).also { print("${it.first},") },
    i = i + one
  )

/** Returns a sequence of [Nat]s starting from [from] until [to] **/
tailrec fun <T> Nat<T>.seq(
  from: T = one,
  to: T,
  acc: Set<T> = emptySet()
): Set<T> =
  if (from == to) acc else seq(from + one, to, acc + from)

/** Returns whether an [Nat] is prime **/
fun <T> Nat<T>.isPrime(t: T, kps: Set<T> = emptySet()): Boolean =
  // Take Cartesian product, filter distinct pairs due to commutativity
  (if (kps.isNotEmpty()) kps * kps
  else seq(to = t) * seq(to = t))
    .distinctBy { (l, r) -> setOf(l, r) }
    .all { (i, j) -> if (i == one || j == one) true else i * j != t }

/** Prints [t] prime [Nat]s **/
tailrec fun <T> Nat<T>.printPrimes(
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
  flatMap { l -> s.map { r -> l to r }.toSet() }.toSet()

/** Returns the product of two [Nat]s **/
tailrec fun <T> Nat<T>.times(l: T, r: T, acc: T = nil, i: T = nil): T =
  if (i == r) acc else times(l, r, acc + l, i + one)

/** Returns the sum of two [Nat]s **/
tailrec fun <T> Nat<T>.plus(l: T, r: T, acc: T = l, i: T = nil): T =
  if (i == r) acc else plus(l, r, succ(acc), succ(i))

fun <T> Nat<T>.sum(list: List<T>): T = list.reduce { acc, t -> acc + t }

interface Nat<T> {
  val nil: T
  val one: T
    get() = succ(nil)

  fun succ(t: T): T
  operator fun T.plus(t: T) = plus(this, t)
  operator fun T.times(t: T) = times(this, t)

  companion object {
    operator fun <T> invoke(nil: T, succ: (T) -> T): Nat<T> =
      object: Nat<T> {
        override val nil: T = nil
        override fun succ(t: T): T = succ(t)
      }
  }
}

interface Ring<T>: Nat<T> {
  override fun succ(t: T): T = t + one

  companion object {
    operator fun <T> invoke(nil: T, one: T, plus: (T, T) -> T): Ring<T> =
      object: Ring<T> {
        override fun T.plus(t: T) = plus(this, t)
        override val nil: T = nil
        override val one: T = one
      }

    operator fun <T> invoke(
      nil: T, one: T,
      plus: (T, T) -> T,
      times: (T, T) -> T
    ): Ring<T> =
      object: Ring<T> {
        override fun T.plus(t: T) = plus(this, t)
        override fun T.times(t: T) = times(this, t)
        override val nil: T = nil
        override val one: T = one
      }
  }
}