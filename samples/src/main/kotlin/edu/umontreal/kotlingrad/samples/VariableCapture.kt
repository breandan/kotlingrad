@file:Suppress("UNCHECKED_CAST", "UNUSED_VARIABLE")

package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.DoubleContext

fun main() {
  with(DoubleContext) {
    val q = X + Y + Z + Y + 0.0
    println("q = $q") // Should print above expression
    val totalApp = q(X to 1.0, Y to 2.0, Z to 3.0) // Name resolution
    println(totalApp) // Should be 8
    val partialApp = q(X to 1.0, Y to 1.0)(Z to 1.0) // Currying is possible
    println(partialApp) // Should be 4
    val partialApp2 = q(X to 1.0)(Y to 1.0, Z to 1.0) // Any arity is possible
    println(partialApp2) // Should be 4
    val partialApp3 = q(Z to 1.0)(X to 1.0, Y to 1.0) // Any order is possible
    println(partialApp3) // Should be 4

    val t = X + Z / Z + Y + 0.0
    val v = t(Y to 4.0)
    val l = t(X to 1.0)(Z to 2.0)
    val r = t(X to 1.0)(Z to 2.0)(Y to 3.0) // Full currying

    val o = X + Z + 0.0
    //val k = o(Y to 4.0) // Does not compile
    val s = (o(X to 1.0) + Y)(Z to 4.0)(Y to 3.0)

    val p = X + Y * Z + 0.0
    val totalApp2 = p(X to 1.0, Y to 2.0, Z to 3.0)
    println(totalApp2) // Should be 7
    val d = X + Z * X
    println(d(X to 3.0, Z to 4.0)) // Should be 15
    println((2.0 * d)(X to 3.0, Z to 4.0)) // Should be 30
  }
}