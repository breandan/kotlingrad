package edu.umontreal.kotlingrad.delimited

import edu.umontreal.kotlingrad.coroutines.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class DelimitedTest {
  @Test
  fun testNoShift() {
    val x = reset<Int> { 42 }
    assertEquals(42, x)
  }

  @Test
  fun testShiftOnly() {
    val x = reset<Int> { shift { k -> k(42) } }
    assertEquals(42, x)
  }

  @Test
  fun testShiftRight() {
    val x = reset<Int> { 40 + shift<Int> { k -> k(2) } }
    assertEquals(42, x)
  }

  @Test
  fun testShiftLeft() {
    val x = reset<Int> { shift<Int> { k -> k(40) } + 2 }
    assertEquals(42, x)
  }

  @Test
  fun testShiftBoth() {
    val x = reset<Int> { shift<Int> { k -> k(40) } + shift<Int> { k -> k(2) } }
    assertEquals(42, x)
  }

  @Test
  fun testShiftToString() {
    val x = reset<String> { shift<Int> { k -> k(42) }.toString() }
    assertEquals("42", x)
  }

  // TODO: Is this possible? https://en.wikiversity.org/wiki/Introduction_to_Delimited_Continuations/Handling_the_continuation

  @Test
  fun testHiddenShift() {
    var g: DelimitedContinuation<String, Int>? = null
    val x = reset<String> { shift<Int> { k -> g = k; k(42) }.toString() }
    reset<String> { shift<Int> { g!!(42) }.toString() }
    assertEquals("42", x)
  }

  // From: https://en.wikipedia.org/wiki/Delimited_continuation
  // (* 2 (reset (+ 1 (shift k (k 5)))))
  // k := (+ 1 [])
  @Test
  fun testWikiSample() {
    val x = 2 * reset<Int> { 1 + shift<Int> { k -> k(5) } }
    assertEquals(12, x)
  }
}