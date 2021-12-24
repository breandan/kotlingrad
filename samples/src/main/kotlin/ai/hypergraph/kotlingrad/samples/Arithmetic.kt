package ai.hypergraph.kotlingrad.samples

import ai.hypergraph.kotlingrad.typelevel.*

fun main() {
  val t: P_II_III = op(object: PLUS {}, II(), III())
  println(t.int())
//  // val t: P_II_III = plus(object: I{}, object: III{}) // "Specify type explicitly", it will produce P_II_III
//  // val t: Three = op(object: III{}, object: II{}, object: PLUS{}) // Sensitive to input argument order
//  // If we omit the type for t and "Specify type explicitly" on the following type, it is "Three". With the inferred type above, it is "Five"
//  val r = apply(t, object: EVAL {})
//  println(r::class)
  val r = apply(t, object: EVAL {})
  println(r.int())
}