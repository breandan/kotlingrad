package ai.hypergraph.kotlingrad.evaluation

import ai.hypergraph.kotlingrad.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestVariableNames {
  val a by SVar(DReal)
  @Test
  fun testVariableNames() = assertEquals(a.name, "a")
}