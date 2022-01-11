package ai.hypergraph.kotlingrad.typelevel.church

import kotlin.test.*

class ChurchArithmeticTest {
  @Test
  fun shouldBeFour() {
    val four = S2 + S2
    assertEquals(4, four.toInt())
  }

  @Test
  fun shouldBeNine() {
    var nine = S3 * S3
    assertEquals(9, nine.toInt())
    nine = S4 + S4 + S1
    assertEquals(9, nine.toInt())
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

  fun takes5(five: Q2<L3>): S<S<S<S<O>>>> = five - S1
  fun takes6(six: S<L5>): S<S<S<S<S<O>>>>> = six - S1

  @Test
  fun testComposition() {
    val four = takes5(takes6(S2 * S3))
    assertEquals(4, four.toInt())
  }

  @Test
  fun testLiteral() {
    assertEquals(4, S4.toInt())
    assertEquals(4, S(S(S(S(O)))).toInt())
  }

  @Test
  fun shouldBeThree() {
    val three = S2 * S2 * S2 / S4 + S2 - S1

    assertEquals(3, three.toInt())
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
      .minus2()
      .minus1()
      .let { it * it }
      .let { it / S2 }
      .let { it + S3 }
      .let { it + S3 }
      .let { it + S3 }
      .let { it + S3 }
      .let { it + S3 }
      .let { it - S3 }
      .let { it - S2 }
      .let { it - S2 }
      .let { it - S2 }
      .let { it - S2 }
      .let { it - S1 }

    takes5(five)

    assertEquals(5, five.toInt())
  }

//  @Test
//  fun shouldBeThirteen() {
//    val thirteen = S2
//      .let { it + S4 }
//      .let { it + S4 }
//      .let { it + S4 }
//      .let { it + S4 }
//      .let { it + S4 }
//      .let { it + S4 }
//      .let { it + S4 }
//      .let { it - S3 }
//      .let { it - S3 }
//      .let { it - S3 }
//      .let { it - S3 }
//      .let { it - S3 }
//
//    assertEquals(13, thirteen.toInt())
//  }
}
