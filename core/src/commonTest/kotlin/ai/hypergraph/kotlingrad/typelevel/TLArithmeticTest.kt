package ai.hypergraph.kotlingrad.typelevel

import ai.hypergraph.kotlingrad.typelevel.peano.*
import kotlin.test.*

class TLArithmeticTest {
  @Test
  fun basicTest() {
    val four = S2 + S2
    assertEquals(4, four.toInt())
    
    val sixteen = four * four
    assertEquals(16, sixteen.toInt())

    val five = O
      .plus2()
      .let { it + S3 }
      .plus4()
      .minus3()
      .minus3()
      .let { it + it }
      .minus3()
      .let { it * S2 }
      .minus4()
      .let { it * it }
      .let { it - S2 }
      .let { it + S3 }
      .minus3()
      .let { it * it }
      .let { it / S2 }
      .let { it + S3 }

    assertEquals(5, five.toInt())
  }
}