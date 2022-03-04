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


    val 四十二 = (十七 减 九).let { it 加 it }
      .let { (it 加 八) 加 六 }
      .let { (it 减 三) 加 九 }
      .let { (it 加 六) 除 六 }
      .let { (it 乘 六) 加 五 }
      .let { (it 减 三) 减 九 }
      .let { (it 加 五) 加 二 }
      .also { assertEquals(六 乘 七, it) }

    assertEquals(42, 四十二.toInt())


  }
}