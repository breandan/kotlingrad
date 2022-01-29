package ai.hypergraph.kotlingrad.typelevel.binary

import kotlin.test.*

/**
./gradlew :kotlingrad:jvmTest --tests "ai.hypergraph.kotlingrad.typelevel.binary.BinaryArithmeticTest"
*/
class BinaryArithmeticTest {
  @Test
  fun testBooleanArithmetic() {
    val t = T.F
      .let { it + T.F }
      .let { it + T.F.F }
      .let { it + T.T }
      .let { it + T.F }
      .let { it - T.F }
      .let { it + T.F }
      .let { it + T.F }
      .let { it + T }

    assertEquals(T.F.F.F.F, t)
  }

  @Test
  fun testUnchecked() {
    val t = T.F.T.F
    assertEquals(T.F.T.F.F.U, t + t)
  }

  @Test
  fun testBooleanMultiplication() {
    val t = T.T.T * T.F.F.T

    assertEquals(T.T.T.T.T.T, t)
  }

  @Test
  fun testBooleanDivision() {
    val t = T.T.T * T.F.F.T

    assertEquals(t / T.F.F.T, T.T.T)
    assertEquals(t / T.T.T, T.F.F.T)
  }
}