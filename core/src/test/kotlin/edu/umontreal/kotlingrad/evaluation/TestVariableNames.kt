package edu.umontreal.kotlingrad.evaluation

import edu.umontreal.kotlingrad.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestVariableNames {
  val a by SVar(DReal)
  @Test
  fun testVariableNames() {
    assertEquals(a.name, "a")
  }
}