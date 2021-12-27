package ai.hypergraph.kotlingrad.typelevel

import ai.hypergraph.kotlingrad.typelevel.peano.*
import kotlin.test.*

class TLArithmeticTest {
  @Test
  fun basicTest() {
    val four = S2 + S2
    assertEquals(four.toInt(), 4)
    
    val sixteen = four * four
    assertEquals(sixteen.toInt(), 16)
  }
}