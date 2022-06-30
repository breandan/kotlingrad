package ai.hypergraph.kotlingrad.typelevel.arity

import ai.hypergraph.kotlingrad.typelevel.binary.*
import kotlin.test.*

/**
./gradlew :kotlingrad:jvmTest --tests "ai.hypergraph.kotlingrad.typelevel.arity.VariableCaptureTest"
 */
class VariableCaptureTest {
  @Test
  fun testVariableCapture() {
    val xyz: Ex<XX, XX, XX> by x + (y + z) * 2; println(xyz)
    val x_z: Ex<XX, OO, XX> by xyz(y to 1); println(x_z)
    val out: Int = xyz(x to 1, z to 3)(y to 2).also { println("out = $it") }

    val f by x + y + z * 3; println(f)
    val f1 by f(x to 1)(y to 2); println(f1)
    val j: Int = f1(z to 3); println("j = $j")

    val q by x + y + z + y + 0.0; println(q)
    val totalApp = q(x to 1.0, y to 2.0, z to 3.0) // Name resolution
    assertEquals(8.0, totalApp)
    val partialApp = q(x to 1.0, y to 1.0)(z to 1.0) // Currying is possible
    assertEquals(4.0, partialApp)
    val partialApp2 = q(x to 1.0)(y to 1.0, z to 1.0) // Any arity is possible
    assertEquals(4.0, partialApp2)
    val partialApp3 = q(z to 1.0)(x to 1.0, y to 1.0) // Any order is possible
    assertEquals(4.0, partialApp3)

    val t = (x + z) / (z + y + 0.0)
    val v = t(y to 4.0)
    val l = t(x to 1.0)(z to 2.0)
    val r = t(x to 1.0)(z to 2.0)(y to 3.0) // Full currying

    val o = x + z + 0.0
    //val k = o(y to 4.0) // Does not compile
    val s = (o(x to 1.0) + y)(z to 4.0)(y to 3.0)

    val p = x + y * z + 0.0
    val totalApp2 = p(x to 1.0, y to 2.0, z to 3.0)
    assertEquals(7.0, totalApp2)
    val d = x + z * x
    assertEquals(15.0, d(x to 3.0, z to 4.0))
    assertEquals(30.0, (2.0 * d)(x to 3.0, z to 4.0))
  }
}