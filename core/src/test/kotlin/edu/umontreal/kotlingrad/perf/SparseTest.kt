package edu.umontreal.kotlingrad.perf

import ch.ethz.idsc.tensor.*
import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.round
import org.apache.commons.math3.linear.*
import org.ejml.data.*
import org.ejml.kotlin.times
import org.jetbrains.bio.viktor.F64Array
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
import scientifik.kmath.linear.*
import scientifik.kmath.structures.*
import scientifik.kmath.structures.Matrix
import kotlin.system.measureTimeMillis

class SparseTest {
  val m = 100
  val sparsity = 0.1
  val fill = { if (Math.random() < sparsity) Math.random() else 0.0 }
  val contents = Array(m) { Array(m) { fill() }.toDoubleArray() }

  /**
   * Benchmarking 100x100 sparse matrix powering on a Xeon E3-1575M:
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
    contents.run {
      arrayOf(
        "EJML/S" to powBench(toEJMLSparse()) { a, b -> a * b },
        "EJML/D" to powBench(toEJMLDense()) { a, b -> a * b },
        "APACHE" to powBench(toApacheCommons()) { a, b -> a.multiply(b) },
        "VIKTOR" to powBench(toViktor()) { a, b -> a matmul b },
        "KTMATH" to powBench(toKMath()) { a, b -> a dot b },
        "TENSOR" to powBench(toTensor()) { a, b -> a.dot(b) },
        "KTGRAD" to powBench(toKTGrad()) { a, b -> (a * b) as Mat<DReal, D100, D100> }
      ).map { (name, time) -> println("$name: ${time.toDouble() / 1000}s") }
    }
  }

  @Test
  fun testNumericalEquivalence() {
    val powered = contents.run {
      arrayOf(
        toEJMLSparse().power(10) { a, b -> a * b }.toKotlinArray().round(),
        toEJMLDense().power(10) { a, b -> a * b }.toKotlinArray().round(),
        toApacheCommons().power(10) { a, b -> a.multiply(b) }.toKotlinArray().round(),
        toViktor().power(10) { a, b -> a matmul b }.toKotlinArray().round(),
        toKMath().power(10) { a, b -> a.dot(b) }.toKotlinArray().round(),
        toTensor().power(10) { a, b -> a.dot(b) }.toKotlinArray().round(),
        toKTGrad().power(10) { a, b -> (a * b) as Mat<DReal, D100, D100> }.toKotlinArray().round()
      )
    }

    powered.reduce { acc, i -> acc.also { assertArrayEquals(it, i) } }
  }

  fun mapper(rows: Int, cols: Int, e: (Int, Int) -> Double) =
    Array(rows) { i -> DoubleArray(cols) { j -> e(i, j) } }

  fun F64Array.toKotlinArray() = toGenericArray().map { it as DoubleArray }.toTypedArray()
  fun RealMatrix.toKotlinArray() = data
  fun DMatrix.toKotlinArray() = mapper(numRows, numCols) { i, j -> this[i, j] }
  fun Structure2D<out Number>.toKotlinArray() = mapper(rowNum, colNum) { i, j -> this[i, j].toDouble() }
  fun Tensor.toKotlinArray() = mapper(this[0].length(), this[1].length()) { i, j -> Get(i, j).number().toDouble() }
  fun Mat<DReal, *, *>.toKotlinArray() = mapper(numRows, numCols) { i, j -> this[i, j]().toDouble() }

  fun Array<DoubleArray>.toEJMLSparse() = DMatrixSparseCSC(size, size, sumBy { it.count { it == 0.0 } })
    .also { s -> for (i in 0 until size) for (j in 0 until size) this[i][j].let { if (0 < it) s[i, j] = it } }
  fun Array<DoubleArray>.toEJMLDense() = DMatrixRMaj(this)
  fun Array<DoubleArray>.toApacheCommons() = MatrixUtils.createRealMatrix(this)
  fun Array<DoubleArray>.toViktor() = F64Array(size, size) { i, j -> this[i][j] }
  fun Array<DoubleArray>.toKMath() = VirtualMatrix(size, size) { i, j -> this[i][j] } as Matrix<Double>
  fun Array<DoubleArray>.toKTGrad() = with(DoublePrecision) { Mat(D100, D100) { a, b -> this@toKTGrad[a][b] } }
  fun Array<DoubleArray>.toTensor() = Tensors.matrixDouble(this)
  fun Array<DoubleArray>.round(precision: Int = 3) =
    map { it.map { it.round(precision) }.toDoubleArray() }.toTypedArray()

  fun <T> powBench(constructor: T, matmul: (T, T) -> T): Long =
    measureTimeMillis { constructor.power(100, matmul) }

  private fun <T> T.power(exp: Int, matmul: (T, T) -> T) =
    (0..exp).fold(this) { acc, i -> matmul(acc, this) }

  infix fun F64Array.matmul(f: F64Array) =
    F64Array(shape[0], shape[1]) { i, j -> view(i) dot f.view(j, 1) }
}