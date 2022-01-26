package ai.hypergraph.kotlingrad.typelevel.binary

import ai.hypergraph.kotlingrad.typelevel.binary.arithmetic.*
import kotlin.test.*

class BinaryArithmeticTest {
  @Test
  fun testBooleanAddition() {
    val t = T.F.plus2()
      .plus4()
      .plus2()
      .plus8()
      .plus8()
      .plus8()
      .plus8()
      .plus8()
      .plus8()
      .plus8()
      .plus8()
      .plus8()
      .plus8()
      .plus8()
      .plus8()
      .plus8()
      .plus8()
      .plus8()
      .plus8()
      .plus8()
      .plus8()
      .plus8()
      .plus8()

    assertEquals(T.F.T.F.T.F.T.F, t)
  }
}
