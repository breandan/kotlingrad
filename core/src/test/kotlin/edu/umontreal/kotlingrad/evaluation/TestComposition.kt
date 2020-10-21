package edu.umontreal.kotlingrad.evaluation

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.experimental.DReal.Companion.ONE
import edu.umontreal.kotlingrad.experimental.DReal.Companion.TWO
import edu.umontreal.kotlingrad.experimental.DReal.Companion.ZERO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class TestComposition {
  val x by DReal.Var()
  val y by DReal.Var()
  val z by DReal.Var()

  @Test
  fun testNullaryFunction() {
    val f = ZERO
    assertEquals(0.0, f(1).toDouble())
    assertEquals(0.0, f(x to 1).toDouble())
  }

  @Test
  fun testUnaryFunction() {
    val f = x
    assertEquals(1.0, f(ONE).toDouble())
    assertEquals(2.0, f(2).toDouble())
    assertEquals(3.0, f(x to 3).toDouble())
    assertEquals(4.0, f(x to 4.0).toDouble())
  }

  @Test
  fun testBinaryFunction() {
    val f = y / 2 + 1 * x
    assertEquals(2.0, f(ONE, TWO).toDouble())
    assertEquals(3.5, f(x to 2, y to 3).toDouble())
    assertEquals(5.0, f(y to 4, x to 3.0).toDouble())
    assertEquals(6.5, f(x to 4.0, y to 5.0, z to 6).toDouble())
  }

  @Test
  fun testNestedComposition() {
    val f = x + 1
    val g = y * 2
    val h = f(x to g)() // (y * 2) + 1
    assertEquals(3.0, h(1).toDouble())
    val i = z * 3
    val j = h(y to i)() // ((z * 3) * 2) + 1
    assertEquals(7.0, j(1).toDouble())
  }
}