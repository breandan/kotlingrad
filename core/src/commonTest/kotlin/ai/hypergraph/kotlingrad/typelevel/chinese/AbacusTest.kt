package ai.hypergraph.kotlingrad.typelevel.chinese

import kotlin.test.*

/*
./gradlew :kotlingrad:jvmTest --tests "ai.hypergraph.kotlingrad.typelevel.chinese.AbacusTest"
*/
@Suppress("ClassName", "NonAsciiCharacters")
class AbacusTest {
  @Test
  fun testAbacus() {
    val 二十一 = 十七 加 四
    assertEquals(21, 二十一.toInt())

    val 四十二 = 十七
      .let { it 加 二 }
      .let { it 加 一 }
      .let { it 加 六 }
      .let { it 减 三 }
      .let { it 加 四 }
      .let { it 加 一 }
      .let { it 加 一 }
      .let { it 加 一 }
      .let { it 加 九 }
      .let { it 加 一 }
      .let { it 加 一 }
      .let { it 加 一 }

    assertEquals(42, 四十二.toInt())
  }
}