@file:Suppress("NonAsciiCharacters")

package edu.umontreal.kotlingrad.typelevel

import java.math.BigDecimal as bd
import java.math.BigInteger as bi
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

fun main() {
  Nat(
    nil = bi.ZERO,
    next = { this + bi.ONE }
  ).run {
    measureTimeMillis {
      fibonacci(bi.valueOf(20))
      primes(bi.valueOf(20))
      factorial(bi.valueOf(10))
    }.also { ms -> println("\nTook ${ms}ms") }
  }

  Ring(
    nil = bi.ZERO,
    one = bi.ONE,
    plus = { a, b -> a + b }
  ).run {
    measureTimeMillis {
      fibonacci(bi.valueOf(20))
      primes(bi.valueOf(20))
      factorial(bi.valueOf(10))
    }.also { ms -> println("\nTook ${ms}ms") }
  }

  Ring(
    nil = bi.ZERO,
    one = bi.ONE,
    plus = { a, b -> a + b },
    times = { a, b -> a * b }
  ).run {
    measureTimeMillis {
      fibonacci(bi.valueOf(20))
      primes(bi.valueOf(20))
      factorial(bi.valueOf(10))
    }.also { ms -> println("\nTook ${ms}ms") }
  }

  Field(
    nil = Rational(0, 0),
    one = Rational(1, 1),
    plus = { a, b -> a + b },
    times = { a, b -> a * b },
    div = { a, b -> a / b },
    minus = { a, b -> a - b },
  ).apply {
    measureTimeMillis {
      println(sum(seq(to = Rational(1000))))
      // TODO: fix infinite loop, maybe with comparator?
      // fibonacci(Rational(4, 1))
      // primes(Rational(20, 1))
    }.also { ms -> println("\nTook ${ms}ms") }
  }

  Field(
    nil = bd.ZERO,
    one = bd.ONE,
    plus = { a, b -> a + b },
    times = { a, b -> a * b },
    div = { a, b -> a / b },
    minus = { a, b -> a - b },
  ).let { field ->
    println("Vec: " +
      VectorField(field).run {
        bd.ONE dot Vector(bd.ZERO, bd.ONE)
      }
    )
  }
}

/** Corecursive Fibonacci sequence of [Nat]s **/
tailrec fun <T> Nat<T>.fibonacci(
  n: T,
  seed: Pair<T, T> = nil to one,
  fib: (Pair<T, T>) -> Pair<T, T> = { (a, b) -> b to a + b },
  i: T = nil,
): T =
  if (i == n) fib(seed).first
  else fibonacci(
    n = n,
    seed = fib(seed).also { print("${it.first}, ") },
    i = i.next()
  )

/** Returns [n]! **/
fun <T> Nat<T>.factorial(n: T): T = prod(seq(to = n.next()))

/** Returns a sequence of [Nat]s starting from [from] until [to] **/
tailrec fun <T> Nat<T>.seq(
  from: T = one,
  to: T,
  acc: Set<T> = emptySet()
): Set<T> =
  if (from == to) acc else seq(from.next(), to, acc + from)

/** Returns whether an [Nat] is prime **/
fun <T> Nat<T>.isPrime(t: T, kps: Set<T> = emptySet()): Boolean =
  // Take Cartesian product, filter distinct pairs due to commutativity
  (if (kps.isNotEmpty()) kps * kps else seq(to = t) * seq(to = t))
    .distinctBy { (l, r) -> setOf(l, r) }
    .all { (i, j) -> if (i == one || j == one) true else i * j != t }

/** Prints [t] prime [Nat]s **/
tailrec fun <T> Nat<T>.primes(
  t: T, // number of primes
  i: T = nil, // counter
  c: T = one.next(), // prime candidate
  kps: Set<T> = emptySet() // known primes
): Unit =
  when {
    i == t -> Unit
    isPrime(c) -> {
      print("$c, "); primes(t, i.next(), c.next(), kps + c)
    }
    else -> primes(t, i, c.next(), kps)
  }

// Returns the Cartesian product of two sets
operator fun <T> Set<T>.times(s: Set<T>) =
  flatMap { l -> s.map { r -> l to r }.toSet() }.toSet()

/** Returns the sum of two [Nat]s **/
tailrec fun <T> Nat<T>.plus(l: T, r: T, acc: T = l, i: T = nil): T =
  if (i == r) acc else plus(l, r, acc.next(), i.next())

/** Returns the product of two [Nat]s **/
tailrec fun <T> Nat<T>.times(l: T, r: T, acc: T = nil, i: T = nil): T =
  if (i == r) acc else times(l, r, acc + l, i.next())

fun <T> Nat<T>.sum(list: Iterable<T>): T = list.reduce { acc, t -> acc + t }

fun <T> Nat<T>.prod(list: Iterable<T>): T = list.reduce { acc, t -> acc * t }

interface Nat<T> {
  val nil: T
  val one: T
    get() = nil.next()

  fun T.next(): T
  operator fun T.plus(t: T) = plus(this, t)
  operator fun T.times(t: T) = times(this, t)

  companion object {
    operator fun <T> invoke(nil: T, next: T.() -> T): Nat<T> =
      object: Nat<T> {
        override val nil: T = nil
        override fun T.next(): T = next()
      }
  }
}

interface Ring<T>: Nat<T> {
  override fun T.next(): T = this + one

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

@Suppress("NO_TAIL_CALLS_FOUND")
/** Returns the result of subtracting two [Field]s **/
tailrec fun <T> Field<T>.minus(l: T, r: T, acc: T = nil, i: T = nil): T = TODO()

@Suppress("NO_TAIL_CALLS_FOUND")
/** Returns the result of dividing of two [Field]s **/
tailrec fun <T> Field<T>.div(l: T, r: T, acc: T = l, i: T = nil): T = TODO()

interface Field<T>: Ring<T> {
  operator fun T.minus(t: T): T = minus(this, t)
  operator fun T.div(t: T): T = div(this, t)

  companion object {
    operator fun <T> invoke(
      nil: T, one: T,
      plus: (T, T) -> T,
      times: (T, T) -> T,
      minus: (T, T) -> T,
      div: (T, T) -> T
    ): Field<T> =
      object: Field<T> {
        override fun T.plus(t: T) = plus(this, t)
        override fun T.times(t: T) = times(this, t)
        override fun T.minus(t: T) = minus(this, t)
        override fun T.div(t: T) = div(this, t)
        override val nil: T = nil
        override val one: T = one
      }
  }
}

interface Vector<T>: Nat<T> {
  val ts: Array<T>
  override val nil: T
    get() = ts.first()

  override fun T.next(): T =
    ts.indexOf(this).let { c ->
      if (ts.size <= c + 1) this else ts[c + 1]
    }

  companion object {
    operator fun <T> invoke(vararg tz: T): Vector<T> =
      object: Vector<T> {
        override val ts: Array<T> = tz as Array<T>
        override fun toString() = ts.joinToString(",", "[", "]")
      }
  }
}

interface VectorField<T, F: Field<T>> {
  infix fun T.dot(p: Vector<T>): Vector<T>

  companion object {
    inline operator fun <reified T, reified F: Field<T>>
      invoke(field: F): VectorField<T, F> = field.run {
      object: VectorField<T, F> {
        override fun T.dot(p: Vector<T>): Vector<T> =
          Vector(*p.ts.map { times(this, it) }.toTypedArray())
      }
    }
  }
}

// http://www.math.ucsd.edu/~alina/oldcourses/2012/104b/zi.pdf
data class GaussInt(val a: Int, val b: Int) {
  operator fun plus(o: GaussInt): GaussInt = GaussInt(a + o.a, b + o.b)
  operator fun times(o: GaussInt): GaussInt =
    GaussInt(a * o.a - b * o.b, a * o.b + b * o.a)
}

class Rational(i: Int, j: Int = 1) {
  private val canonicalRatio = reduce(i, j)
  val a = canonicalRatio.first
  val b = canonicalRatio.second

  operator fun times(r: Rational) = Rational(a * r.a, b * r.b)

  operator fun plus(r: Rational) = Rational(a * r.b + r.a * b, b * r.b)

  operator fun minus(r: Rational) = Rational(a * r.b - r.a * b, b * r.b)

  operator fun div(r: Rational) = Rational(a * r.b, b * r.a)

  override fun toString() = "$a/$b"
  override fun equals(other: Any?) =
    (other as? Rational).let { a == it!!.a && b == it.b }

  override fun hashCode() = toString().hashCode()

  companion object {
    fun reduce(a: Int, b: Int) = Pair(
      (a.toFloat() / a.gcd(b).toFloat()).roundToInt(),
      (b.toFloat() / a.gcd(b).toFloat()).roundToInt()
    )

    private tailrec fun Int.gcd(that: Int): Int = when {
      this == that -> if (that == 0) 1 else this
      this > that -> (this - that).gcd(that)
      else -> gcd(that - this)
    }
  }
}