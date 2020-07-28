package edu.umontreal.kotlingrad.perf

import ch.ethz.idsc.tensor.Tensors
import edu.umontreal.kotlingrad.experimental.*
import org.apache.commons.math3.linear.MatrixUtils
import org.ejml.data.*
import org.ejml.kotlin.times
import org.jetbrains.bio.viktor.F64Array
import org.junit.jupiter.api.*
import scientifik.kmath.linear.*
import scientifik.kmath.structures.Matrix
import kotlin.system.measureTimeMillis

class SparseTest {
  /**
   * Benchmarking 200x200 sparse matrix powering on a Xeon E3-1575M:
   *
   * EJML/S: 0.085s
   * EJML/D: 0.171s
   * APACHE: 0.16s
   * VIKTOR: 0.752s
   * KMATH:  2.878s
   * TENSOR: 5.618s
   * KTGRAD: 64.823s
   */

  @Test
  @Disabled
  fun testDoubleMatrixMultiplication() {
    val m = 100
    val sparsity = 0.1
    val fill = { if (Math.random() < sparsity) Math.random() else 0.0 }
    val contents = Array(m) { i -> Array(m) { fill() }.toDoubleArray() }

    DMatrixSparseCSC(m, m, m * m / 5).let { s ->
      for (i in 0 until m) for (j in 0 until m) contents[i][j].let { if (0 < it) s[i, j] = it }
      bench("EJML/S", s) { a, b -> a * b }
    }

    bench("EJML/D", DMatrixRMaj(contents)) { a, b -> a * b }
    bench("APACHE", MatrixUtils.createRealMatrix(contents)) { a, b -> a.multiply(b) }
    bench("VIKTOR", F64Array(m, m) { i, j -> contents[i][j] }) { a, b -> a matmul b }
    bench(" KMATH", VirtualMatrix(m, m) { i, j -> contents[i][j] } as Matrix<Double>) { a, b -> a dot b }
    bench("TENSOR", Tensors.matrixDouble(contents)) { a, b -> a.dot(b) }
    bench("KTGRAD", with(DoublePrecision) { Mat(D100, D100) { a, b -> contents[a][b]} }) { a, b -> (a * b) as Mat<DReal, D100, D100> }
  }

  fun <T> bench(name: String, constructor: T, matmul: (T, T) -> T): Any =
    measureTimeMillis { (0..100).fold(constructor) { acc, i -> matmul(acc, constructor) } }
      .also { println("$name: ${it.toDouble() / 1000}s") }

  infix fun F64Array.matmul(f: F64Array) =
    F64Array(shape[0], shape[1]) { i, j -> view(i) dot f.view(j, 1) }
}