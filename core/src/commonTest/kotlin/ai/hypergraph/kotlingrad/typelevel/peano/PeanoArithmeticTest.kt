package ai.hypergraph.kotlingrad.typelevel.peano

import kotlin.test.*

class PeanoArithmeticTest {
  @Test
  fun shouldBeFour() {
    val four = S2 + S2
    assertEquals(4, four.toInt())
  }

  @Test
  fun shouldBeSixteen() {
    val sixteen = S4 * S4
    assertEquals(16, sixteen.toInt())
  }

  @Test
  fun shouldBeFive() {
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

  @Test
  fun shouldBeTwo() {
    val two = S1
      .plus1()
      .plus1()
      .plus1()
      .plus1()
      .plus1()
      .plus1()
      .plus1()
      .minus2()
      .minus2()
      .minus2()
      .minus2()
      .plus2()
    

    assertEquals(2, two.toInt())
  }
  
  @Test
  fun shouldBeThree() {
    val three = S2 * S2 * S2 * S2 / S4 * S3 / S6 + S3 - S2

    assertEquals(3, three.toInt())
  }
}