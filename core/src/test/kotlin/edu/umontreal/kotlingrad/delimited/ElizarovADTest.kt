package edu.umontreal.kotlingrad.delimited

import edu.umontreal.kotlingrad.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.*

/*
 * Implementation of backward-mode automatic differentiation.
 *
 * @author Roman Elizarov
 *
 * https://gist.github.com/elizarov/1ad3a8583e88cb6ea7a0ad09bb591d3d#file-adtest-kt
 */

class ADTest {
  @Test
  fun testPlusX2() {
    val x = D(3) // diff w.r.t this x at 3
    val y = grad { x + x }
    assertEquals(6.0, y.x) //    y  = x + x = 6
    assertEquals(2.0, x.d) // dy/dx = 2
  }

  @Test
  fun testPlus() {
    // two variables
    val x = D(2)
    val y = D(3)
    val z = grad { x + y }
    assertEquals(5.0, z.x) //    z  = x + y = 5
    assertEquals(1.0, x.d) // dz/dx = 1
    assertEquals(1.0, y.d) // dz/dy = 1
  }

  @Test
  fun testMinus() {
    // two variables
    val x = D(7)
    val y = D(3)
    val z = grad { x - y }
    assertEquals(4.0, z.x)  //    z  = x - y = 4
    assertEquals(1.0, x.d)  // dz/dx = 1
    assertEquals(-1.0, y.d) // dz/dy = -1
  }

  @Test
  fun testMulX2() {
    val x = D(3) // diff w.r.t this x at 3
    val y = grad { x * x }
    assertEquals(9.0, y.x) //    y  = x * x = 9
    assertEquals(6.0, x.d) // dy/dx = 2 * x = 7
  }

  @Test
  fun testSqr() {
    val x = D(3)
    val y = grad { sqr(x) }
    assertEquals(9.0, y.x) //    y  = x ^ 2 = 9
    assertEquals(6.0, x.d) // dy/dx = 2 * x = 7
  }

  @Test
  fun testSqrSqr() {
    val x = D(2)
    val y = grad { sqr(sqr(x)) }
    assertEquals(16.0, y.x) //     y = x ^ 4   = 16
    assertEquals(32.0, x.d) // dy/dx = 4 * x^3 = 32
  }

  @Test
  fun testX3() {
    val x = D(2) // diff w.r.t this x at 2
    val y = grad { x * x * x }
    assertEquals(8.0, y.x)  //    y  = x * x * x = 8
    assertEquals(12.0, x.d) // dy/dx = 3 * x * x = 12
  }

  @Test
  fun testDiv() {
    val x = D(5)
    val y = D(2)
    val z = grad { x / y }
    assertEquals(2.5, z.x)   //     z =  x / y   = 2.5
    assertEquals(0.5, x.d)   // dz/dx =  1 / y   = 0.5
    assertEquals(-1.25, y.d) // dz/dy = -x / y^2 = -1.25
  }

  @Test
  fun testPow3() {
    val x = D(2) // diff w.r.t this x at 2
    val y = grad { pow(x, 3) }
    assertEquals(8.0, y.x)  //    y  = x ^ 3     = 8
    assertEquals(12.0, x.d) // dy/dx = 3 * x ^ 2 = 12
  }

  @Test
  fun testPowFull() {
    val x = D(2)
    val y = D(3)
    val z = grad { pow(x, y) }
    assertApprox(8.0, z.x)           //     z = x ^ y = 8
    assertApprox(12.0, x.d)          // dz/dx = y * x ^ (y - 1) = 12
    assertApprox(8.0 * ln(2.0), y.d) // dz/dy = x ^ y * ln(x)
  }

  @Test
  fun testFromPaper() {
    val x = D(3)
    val y = grad { 2 * x + x * x * x }
    assertEquals(33.0, y.x)  //     y = 2 * x + x * x * x = 33
    assertEquals(29.0, x.d)  // dy/dx = 2 + 3 * x * x = 29
  }

  @Test
  fun testLongChain() {
    val n = 10_000
    val x = D(1)
    val y = grad {
      var pow = D(1)
      for (i in 1..n) pow *= x
      pow
    }
    assertEquals(1.0, y.x)          //     y = x ^ n = 1
    assertEquals(n.toDouble(), x.d) // dy/dx = n * x ^ (n - 1) = n - 1
  }

  @Test
  fun testExample() {
    val x = D(2)
    val y = grad { sqr(x) + 5 * x + 3 }
    assertEquals(17.0, y.x) // the value of result (y)
    assertEquals(9.0, x.d)  // dy/dx
  }

  @Test
  fun testSqrt() {
    val x = D(16)
    val y = grad { sqrt(x) }
    assertEquals(4.0, y.x)     //     y = x ^ 1/2 = 4
    assertEquals(1.0 / 8, x.d) // dy/dx = 1/2 / x ^ 1/4 = 1/8
  }

  @Test
  fun testSin() {
    val x = D(PI / 6)
    val y = grad { sin(x) }
    assertApprox(0.5, y.x)           //    y = sin(PI/6) = 0.5
    assertApprox(sqrt(3.0) / 2, x.d) // dy/dx = cos(PI/6) = sqrt(3)/2
  }

  @Test
  fun testCos() {
    val x = D(PI / 6)
    val y = grad { cos(x) }
    assertApprox(sqrt(3.0) / 2, y.x) //     y =  cos(PI/6) = sqrt(3)/2
    assertApprox(-0.5, x.d)          // dy/dx = -sin(PI/6) = -0.5
  }

  private fun assertApprox(a: Double, b: Double) {
    if ((a - b) > 1e-10) assertEquals(a, b)
  }
}