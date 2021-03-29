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
    val x = reset<Int> {
      shift<Int> { k -> k(42) }
    }
    assertEquals(42, x)
  }

  @Test
  fun testShiftRight() {
    val x = reset<Int> {
      40 + shift<Int> { k -> k(2) }
    }
    assertEquals(42, x)
  }

  @Test
  fun testShiftLeft() {
    val x = reset<Int> {
      shift<Int> { k -> k(40) } + 2
    }
    assertEquals(42, x)
  }

  @Test
  fun testShiftBoth() {
    val x = reset<Int> {
      shift<Int> { k -> k(40) } +
        shift<Int> { k -> k(2) }
    }
    assertEquals(42, x)
  }

  @Test
  fun testShiftToString() {
    val x = reset<String> {
      shift<Int> { k -> k(42) }.toString()
    }
    assertEquals("42", x)
  }

  @Test
  fun testShiftWithoutContinuationInvoke() {
    val x = reset<Int> {
      shift<String> {
        42 // does not call continuation, just override result
      }
      0 // this is not called
    }
    assertEquals(42, x)
  }

  // From: https://en.wikipedia.org/wiki/Delimited_continuation
  // (* 2 (reset (+ 1 (shift k (k 5)))))
  // k := (+ 1 [])
  @Test
  fun testWikiSample() {
    val x = 2 * reset<Int> {
      1 + shift<Int> { k -> k(5) }
    }
    assertEquals(12, x)
  }

  // It must be extension on DelimitedScope<Int> to be able to shift
  private suspend fun DelimitedScope<Int>.shiftFun(x: Int): Int =
    shift<Int> { k -> k(x) } * 2

  @Test
  fun testShiftFromFunction() {
    val x = reset<Int> {
      2 + shiftFun(20)
    }
    assertEquals(42, x)
  }

  @Test
  // Ensure there's no stack overflow because of many "shift" calls
  fun testManyShifts() {
    val res = reset<String> {
      for (x in 0..10000) {
        shift<Int> { k ->
          k(x)
        }
      }
      "OK"
    }
    assertEquals("OK", res)
  }

  @Test
  // See https://gist.github.com/elizarov/5bbbe5a3b88985ae577d8ec3706e85ef#gistcomment-3304535
  fun testShiftRemainderCalled() {
    val log = ArrayList<String>()
    val x = reset<Int> {
      val y = shift<Int> { k ->
        log += "before 1"
        val r = k(1)
        log += "after 1"
        r
      }
      log += y.toString()
      val z = shift<Int> { k ->
        log += "before 2"
        val r = k(2)
        log += "after 2"
        r
      }
      log += z.toString()
      y + z
    }
    assertEquals(3, x)
    assertEquals(
      listOf(
        "before 1",
        "1",
        "before 2",
        "2",
        "after 2",
        "after 1"
      ), log
    )
  }
}