package ai.hypergraph.shipshape

import org.intellij.lang.annotations.Language

val range = 2..16

@Language("kt")
fun genPeanoArithmetic() = """
// This file was generated by Shipshape
@file:Suppress("UNUSED_PARAMETER", "UNCHECKED_CAST")

package ai.hypergraph.kotlingrad.typelevel.peano

import kotlin.jvm.JvmName

open class S<X>(val x: S<X>?)
object O: S<O>(null)
fun S<*>.toInt(i: Int = 0): Int = x?.toInt(i + 1) ?: i

${genAliases()}

${genConsts()}

${genArithmetic()}
""".trimMargin()

fun genAliases(): String =
  "val S1 = S(O)" +
  range.joinToString("\n", "\n", "\n") {
    val (a, b) = balancedPartition(it)
    "val S$it = S$a.plus$b()"
  } +
    "private typealias L1 = S<O>" +
    range.joinToString("\n", "\n", "\n") {
      val (a, b) = balancedPartition(it)
      "private typealias L$it = ${genChurchNat(it, "O")}"
    } +
    "private typealias Q1 = S<*>" +
    range.joinToString("\n", "\n") {
      val (a, b) = balancedPartition(it)
      "private typealias Q$it<T> = ${genChurchNat(it, "T")}"
    }

fun genConsts(): String =
  """
    fun <W: S<*>, X: S<W>> W.plus1(): X = S(this) as X
    fun <W: S<*>, X: S<W>> X.minus1(): W = x as W
  """.trimIndent() +
  range.joinToString( "\n", "\n" ) {
    val (a, b) = balancedPartition(it)
    """
      fun <W: S<*>, X: Q$it<W>> W.plus$it(): X = plus$a().plus$b()
      fun <W: S<*>, X: Q$it<W>> X.minus$it(): W = minus$a().minus$b()
    """.trimIndent()
  }

fun balancedPartition(i: Int) =
  (i / 2).let { half -> half to if (half * 2 == i) half else half + 1 }

fun genArithmetic(
  ops: Map<Pair<String, String>, (Int, Int) -> Int> = mapOf(
    "times" to "*" to { a, b -> a * b },
    "div" to "÷" to { a, b -> if ((a / b) * b == a) a / b else Int.MIN_VALUE },
  )
) = genSpecials() + "\n" + genPlus() + "\n" + genMinus() + "\n" +
  (range * range * ops.entries).filter { (a, b, c) -> c.value(a, b) in range }
    .joinToString("\n", "\n") { (a, b, c) ->
      val res = c.value(a, b)
//        val sres = genChurchNat(res, "O")
      val op = c.key.second
      val name = c.key.first
//        "@JvmName(\"$a$op$b\") operator fun <W: $sa, X: $sb, Y: $sres> W.$name(x: X): Y = $name$b()"
      "@JvmName(\"$a$op$b\") operator fun <W: L$a, X: L$b> W.$name(x: X) = S${res}"
    }

fun genPlus() =
  range.joinToString("\n", "\n") {
    """
      @JvmName("n+$it") operator fun <W: L$it, X: S<*>> X.plus(x: W) = plus$it()
    """.trimIndent()
  }

// I think this is called a quotient type? https://en.wikipedia.org/wiki/Quotient_type
fun genMinus() =
  range.joinToString("\n", "\n") {
    """
      @JvmName("n-$it") operator fun <V: L$it, W: S<*>, X: Q$it<W>> X.minus(v: V) = minus$it()
    """.trimIndent()
  }

fun genTimes(): String = TODO()
fun genDiv(): String = TODO()

operator fun IntRange.times(s: IntRange) =
  flatMap { l -> s.map { r -> l to r }.toSet() }.toSet()

operator fun <T, Y, Z> Set<Pair<T, Y>>.times(s: Set<Z>): Set<Triple<T, Y, Z>> =
  flatMap { (l, ll) -> s.map { r -> Triple(l, ll, r) }.toSet() }.toSet()

fun genSpecials() =
  """
    @JvmName("n+0") operator fun <W: S<*>> W.plus(x: O) = this
    @JvmName("0+n") operator fun <X: S<*>> O.plus(x: X) = x
    @JvmName("n+1") operator fun <W: S<*>, X: S<O>> W.plus(x: X) = plus1()
    @JvmName("1+n") operator fun <W: S<*>, X: S<O>> X.plus(w: W) = w.plus1()
    @JvmName("n-1") operator fun <W: S<*>, X: S<W>, Y: S<O>> X.minus(y: Y) = minus1()
    @JvmName("n÷1") operator fun <W: S<*>, X: S<O>> W.div(x: X) = this
    @JvmName("n*1") operator fun <W: S<*>, X: S<O>> W.times(x: X) = this
    @JvmName("1*n") operator fun <W: S<*>, X: S<O>> X.times(w: W) = w
    @JvmName("n*0") operator fun <W: S<*>> W.times(x: O) = O
    @JvmName("0*n") operator fun <X: S<*>> O.times(x: X) = O
  """.trimIndent()

tailrec fun genChurchNat(i: Int, prev: String = "K"): String =
  if (i == 0) prev else genChurchNat(i - 1, "S<$prev>")