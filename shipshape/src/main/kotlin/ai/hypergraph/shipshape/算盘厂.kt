package ai.hypergraph.shipshape

import kotlin.streams.toList

fun main() {
  val opA = (0..999)
  val opB = (0..9)
  val addAndSub = (opA * opB).asSequence()
    .map { (a, b) -> Triple(a.toString(), b.toString(), (a + b).toString()) }
//    .filter { (a, _, c) -> a.length == c.length }
    .map { (a, b, c) ->
      val (invA, invC) = if (a.length != c.length) "无$a" to "无$c"
      else a.zip(c).mapNotNull { (ai, ci) -> if (ai == ci) null else (ai to ci) }
        .fold("丁" to "丁") { acc, it -> (acc.first + it.first) to (acc.second + it.second) }

      Triple(invA, b, invC)
    }.toSortedSet(
      compareBy<Triple<String, String, String>> { it.second.toInt() }
        .thenBy { it.first.length }
        .thenBy { it.first }
    )
    .map { (a, b, c) ->
      Triple(
        a.a2z().toCode(),
        b.a2z().toCode(),
        c.a2z().toCode()
      )
    }
    .let {
      val eqs =
        it.map { (a, b, c) -> "$a 加 $b = $c" } +
        it.map { (a, b, c) -> "$c 减 $b = $a" }
      eqs.joinToString("\n")
    }

  val divs = (0..200)
    .map { i -> i.nontrivialDivisors(99) { i % it == 0 && it > 1 && it != 10 } }
    .flatten().let {
      val eqs = it.map { (a, b, c) ->
        val (a, b, c) = Triple(
          "无$a".a2z().toCode(),
          "无$b".a2z().toCode(),
          "无$c".a2z().toCode()
        )
        "$a 除 $b = $c\n" +
        "$c 乘 $b = $a"
      }
      eqs.joinToString("\n")
    }


  val allOps = "$addAndSub\n$divs"
  println(allOps)
  println("=================")
  println(allOps.lines().size)
}


val z2a: Map<String, String> = mapOf(
  "零" to "0",
  "一" to "1",
  "二" to "2",
  "三" to "3",
  "四" to "4",
  "五" to "5",
  "六" to "6",
  "七" to "7",
  "八" to "8",
  "九" to "9",
  "十" to "",
  "百" to "",
)

val a2z = z2a.entries.associate { (k, v) -> v to k }

fun String.toCode(lb: Char = '<', rb: Char = '>') =
  fold("") { acc, it -> if(acc.isEmpty()) "$it" else "$it$lb$acc$rb" }
fun String.a2z() = toCharArray().toList().joinToString("") { a2z(it) }
fun a2z(c: Char) = a2z.getOrElse("$c") { "$c" }

