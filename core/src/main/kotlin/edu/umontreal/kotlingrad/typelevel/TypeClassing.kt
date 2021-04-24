@file:Suppress("NonAsciiCharacters")

package edu.umontreal.kotlingrad.typelevel

import kotlin.math.roundToInt

/** Corecursive Fibonacci sequence of [Nat]s **/
// Pending: https://youtrack.jetbrains.com/issue/KT-46189
/*tailrec*/ fun <T> Nat<T>.fibonacci(
  n: T,
  seed: Pair<T, T> = nil to one,
  fib: (Pair<T, T>) -> Pair<T, T> = { (a, b) -> b to a + b },
  i: T = nil,
): T =
  if (i == n) fib(seed).first
  else fibonacci(n = n, seed = fib(seed), i = i.next())

/** Returns [n]! **/
fun <T> Nat<T>.factorial(n: T): T = prod(seq(to = n.next()))

/** Returns a sequence of [Nat]s starting from [from] until [to] **/
tailrec fun <T> Nat<T>.seq(
  from: T = one, to: T,
  acc: Set<T> = emptySet()
): Set<T> = if (from == to) acc else seq(from.next(), to, acc + from)

/** Returns true iff [t] is prime **/
fun <T> Nat<T>.isPrime(t: T, kps: Set<T> = emptySet()): Boolean =
  // Take Cartesian product, filter distinct pairs due to commutativity
  (if (kps.isNotEmpty()) kps * kps else seq(to = t) * seq(to = t))
    .distinctBy { (l, r) -> setOf(l, r) }
    .all { (i, j) -> if (i == one || j == one) true else i * j != t }

/** Returns [total] prime [Nat]s **/
tailrec fun <T> Nat<T>.primes(
  total: T, // total number of primes
  i: T = nil, // counter
  c: T = one.next(), // prime candidate
  kps: Set<T> = emptySet() // known primes
): Set<T> =
  when {
    i == total -> kps
    isPrime(c) -> {
      primes(total, i.next(), c.next(), kps + c)
    }
    else -> primes(total, i, c.next(), kps)
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

tailrec fun <T> Nat<T>.pow(base: T, exp: T, acc: T = one, i: T = one): T =
  if (i == exp) acc else pow(base, exp, acc * base, i.next())

fun <T> Nat<T>.sum(list: Iterable<T>): T = list.reduce { acc, t -> acc + t }

fun <T> Nat<T>.prod(list: Iterable<T>): T = list.reduce { acc, t -> (acc * t) }

interface Nat<T> {
  val nil: T
  val one: T get() = nil.next()

  fun T.next(): T
  operator fun T.plus(t: T) = plus(this, t)
  operator fun T.times(t: T) = times(this, t)
  infix fun T.pow(t: T) = pow(this, t)

  companion object {
    operator fun <T> invoke(nil: T, next: T.() -> T): Nat<T> =
      object: Nat<T> {
        override val nil: T = nil
        override fun T.next(): T = next()
      }
  }
}

interface Group<T>: Nat<T> {
  override fun T.next(): T = this + one
  override fun T.plus(t: T): T

  companion object {
    operator fun <T> invoke(nil: T, one: T, plus: (T, T) -> T): Group<T> =
      object: Group<T> {
        override fun T.plus(t: T) = plus(this, t)
        override val nil: T = nil
        override val one: T = one
      }
  }
}

interface Ring<T>: Group<T> {
  override fun T.plus(t: T): T
  override fun T.times(t: T): T

  companion object {
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

interface Vector<T> {
  val ts: List<T>

  fun vmap(map: (T) -> T) = Vector(ts.map { map(it) })

  fun zip(other: Vector<T>, merge: (T, T) -> T) =
    Vector(ts.zip(other.ts).map { (a, b) -> merge(a, b) })

  companion object {
    operator fun <T> invoke(vararg ts: T): Vector<T> = invoke(ts.toList())
    operator fun <T> invoke(ts: List<T>): Vector<T> = object: Vector<T> {
      override val ts: List<T> = ts
      override fun toString() =
        ts.joinToString(",", "${ts.javaClass.simpleName}[", "]")
    }
  }
}

interface VectorField<T, V: Vector<T>, F: Field<T>> {
  val v: V
  val f: F

  operator fun Vector<T>.plus(vec: Vector<T>): Vector<T> =
    zip(vec) { a, b -> f.plus(a, b) }

  infix fun T.dot(p: Vector<T>): Vector<T> = p.vmap { f.times(it, this) }

  companion object {
    operator fun <T, F: Field<T>> invoke(v: Vector<T> = Vector(), f: F) =
      object: VectorField<T, Vector<T>, F> {
        override val v: Vector<T> get() = v
        override val f: F get() = f
      }
  }
}

// http://www.math.ucsd.edu/~alina/oldcourses/2012/104b/zi.pdf
data class GaussInt(val a: Int, val b: Int) {
  operator fun plus(o: GaussInt): GaussInt = GaussInt(a + o.a, b + o.b)
  operator fun times(o: GaussInt): GaussInt =
    GaussInt(a * o.a - b * o.b, a * o.b + b * o.a)
}

class Rational {
  constructor(i: Int, j: Int = 1) {
    if (j == 0) throw ArithmeticException("Denominator must not be zero!")
    canonicalRatio = reduce(i, j)
    a = canonicalRatio.first
    b = canonicalRatio.second
  }

  private val canonicalRatio: Pair<Int, Int>
  val a: Int
  val b: Int

  operator fun times(r: Rational) = Rational(a * r.a, b * r.b)

  operator fun plus(r: Rational) = Rational(a * r.b + r.a * b, b * r.b)

  operator fun minus(r: Rational) = Rational(a * r.b - r.a * b, b * r.b)

  operator fun div(r: Rational) = Rational(a * r.b, b * r.a)

  override fun toString() = "$a/$b"
  override fun equals(other: Any?) =
    (other as? Rational).let { a == it!!.a && b == it.b }

  override fun hashCode() = toString().hashCode()

  companion object {
    val ZERO = Rational(0, 1)
    val ONE = Rational(1, 1)

    fun reduce(a: Int, b: Int) = Pair(
      (a.toFloat() / a.gcd(b).toFloat()).roundToInt(),
      (b.toFloat() / a.gcd(b).toFloat()).roundToInt()
    )

    private tailrec fun Int.gcd(that: Int): Int = when {
      this == that -> this
      this in 0..1 || that in 0..1 -> 1
      this > that -> (this - that).gcd(that)
      else -> gcd(that - this)
    }
  }
}