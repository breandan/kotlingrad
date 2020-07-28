package edu.umontreal.kotlingrad.perf

import ch.ethz.idsc.tensor.*
import org.apache.commons.math3.linear.MatrixUtils
import org.ejml.data.*
import org.ejml.kotlin.times
import org.jetbrains.bio.viktor.F64Array
import org.junit.jupiter.api.*
import kotlin.system.measureTimeMillis

class SparseTest {
  @Test
  @Disabled
  fun testDoubleMatrixMultiplication() {
    val m = 1000
    val sparsity = 0.1
    val fill = { if (Math.random() < sparsity) Math.random() else 0.0 }
    val contents = Array(m) { i -> Array(m) { fill() }.toDoubleArray() }

    testMatmul("VIKTOR", F64Array(m, m) { i, j -> contents[i][j] }) { a, b -> a * b }
    testMatmul("TENSOR", Tensors.matrixDouble(contents)) { a, b -> a.pmul(b) }
    testMatmul("APACHE", MatrixUtils.createRealMatrix(contents)) { a, b -> a.multiply(b) }
    testMatmul("EJML/D", DMatrixRMaj(contents)) { a, b -> a * b }
    DMatrixSparseCSC(m, m, m * m / 5).let { s ->
      for (i in 0 until m) for (j in 0 until m) contents[i][j].let { if (0 < it) s[i, j] = it }
      testMatmul("EJML/S", s) { a, b -> a * b }
    }
  }

  fun <T> testMatmul(name: String, constructor: T, matmul: (T, T) -> T): Any =
    measureTimeMillis { (0..100).fold(constructor) { acc, i -> matmul(acc, constructor) } }
      .also { println("$name: ${it.toDouble() / 1000}s") }
}