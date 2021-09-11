package ai.hypergraph.kotlingrad.typelevel

import org.junit.jupiter.api.Test

class TLArithmeticTest {
  @Test
  fun basicTest() {
    println(D3.eval)
    val t = Vec(D3)
    val q = t.takesThree()

    val four: N4 = (D1 + D1) + (D1 + D1)
    val vec3 = Vec(D1) + Vec(D2)

    val m = Sum<N2, N2>(D2, D2)

//  takesFour(m)
  }
}