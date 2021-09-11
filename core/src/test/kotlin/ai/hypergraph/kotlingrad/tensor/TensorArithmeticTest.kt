package ai.hypergraph.kotlingrad.tensor

import ai.hypergraph.kotlingrad.api.*
import org.junit.jupiter.api.*

class TensorArithmeticTest {
  @Test
  @Disabled
  fun basicTest() {
    val v: Vt<Int, N2> = Vt(1, 2) + Vt(1, 3)
//val w = Vt(1, 2) + Vt(0, 0, 0)

    val m: Mt<Int, N1, N1> = Mt1x2(0, 0) * Mt2x1(0, 0)
//val n = Mt1x2(0, 0) * Mt1x2(0, 0)

    val q: Vt<Int, N3> = Vt(0) cc Vt(0, 0)

    val s: Vt<Int, N2> = Vt(0, 0, 0).rev().take(T2)
    val z: Vt<Int, N1> = s.take(T1)

    val y: Int = z[T1]
//val x = z[T2]
  }
}