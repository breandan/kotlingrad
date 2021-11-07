package ai.hypergraph.kotlingrad.perf

import ai.hypergraph.kaliningraph.*
import ai.hypergraph.kotlingrad.api.*
import ai.hypergraph.kotlingrad.round
import ai.hypergraph.kotlingrad.shapes.D100
import ai.hypergraph.kotlingrad.utils.*
import org.apache.commons.math3.linear.*
import org.ejml.data.DMatrix
import org.ejml.kotlin.times
import org.jetbrains.bio.viktor.F64Array
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class SparseTest {
  val m = 100
  val sparsity = 0.1
  val fill = { if (Random.Default.nextDouble() < sparsity) Random.Default.nextDouble() else 0.0 }
  val contents = Array(m) { Array(m) { fill() }.toDoubleArray() }
  /**
   * Benchmarking 100x100 sparse matrix powering on a Xeon E3-1575M:
   *
   *
   * EJML/S: 0.021s
   * EJML/D: 0.05s
   * ND4J/N: 0.135s
   * APACHE: 0.061s
   * VIKTOR: 0.224s
   * KTGRAD: 0.484s
   * KTMATH: 0.824s
   * TENSOR: 0.931s
   * KTGRAD: 57.542s(!!) TODO
   * MULTIK: ??????
   */

  @Test
  fun testDoubleMatrixMultiplication() {
    var baseline = 1000L
    contents.run {
      arrayOf(
        "EJML/S" to powBench(toEJMLSparse()) { a, b -> a * b },
        "EJML/D" to powBench(toEJMLDense()) { a, b -> a * b },
        "APACHE" to powBench(toApacheCommons()) { a, b -> a.multiply(b) },
        "VIKTOR" to powBench(toViktor()) { a, b -> a matmul b }.also { baseline = it },
        "KTGRAD" to powBench(toKTGrad()) { a, b -> (a * b) as Mat<DReal, D100, D100> },
//      "KTMATH" to powBench(toKMath()) { a, b -> a dot b }
//        .also { assert(it < 20 * baseline) { "Perf regression: $it ms" } },
//      "TENSOR" to powBench(toTensor()) { a, b -> a.dot(b) }
//      "TF4J/N" to powBench(toTf4j()) { a, b ->
//        EagerSession.create().use {
//          val tf = Ops.create(it)
//          tf.math.mul(a, b)
//        }
//      }
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
        toKTGrad().power(10) { a, b -> (a * b) as Mat<DReal, D100, D100> }.toKotlinArray().round(),
//        toKMath().power(10) { a, b -> a.dot(b) }.toKotlinArray().round(),
//        toTensor().power(10) { a, b -> a.dot(b) }.toKotlinArray().round()
      )
    }

    powered.reduce { acc, i -> acc.also { assertArrayEquals(it, i) } }
  }

  fun mapper(rows: Int, cols: Int, e: (Int, Int) -> Double) =
    Array(rows) { i -> DoubleArray(cols) { j -> e(i, j) } }

  fun RealMatrix.toKotlinArray() = data
  fun DMatrix.toKotlinArray() = mapper(numRows, numCols) { i, j -> this[i, j] }
//  fun Structure2D<out Number>.toKotlinArray() = mapper(rowNum, colNum) { i, j -> this[i, j].toDouble() }
//  fun Tensor.toKotlinArray() = mapper(this[0].length(), this[1].length()) { i, j -> Get(i, j).number().toDouble() }
  fun Mat<DReal, *, *>.toKotlinArray() = mapper(numRows, numCols) { i, j -> this[i, j]().toDouble() }
//  fun DoubleNdArray.toKotlinArray() = mapper(shape().size(0).toInt(), shape().size(1).toInt()) { i, j -> getDouble(i.toLong(), j.toLong()) }

  fun Array<DoubleArray>.toApacheCommons() = MatrixUtils.createRealMatrix(this)
  fun Array<DoubleArray>.toViktor() = F64Array(size, size) { i, j -> this[i][j] }
//  fun Array<DoubleArray>.toKMath() = VirtualMatrix(size, size) { i, j -> this[i][j] } as Matrix<Double>
  fun Array<DoubleArray>.toKTGrad() = DReal.Mat(D100, D100) { a, b -> this@toKTGrad[a][b] }
//  fun Array<DoubleArray>.toTensor() = Tensors.matrixDouble(this)
//  fun Array<DoubleArray>.toTf4j() = NdArrays.ofDoubles(Shape.of(size.toLong(), size.toLong()))
//    .also { it.write(DataBuffers.of(*flatMap { it.toList() }.toDoubleArray())) }

  fun Array<DoubleArray>.round(precision: Int = 3) =
    map { it.map { it.round(precision) }.toDoubleArray() }.toTypedArray()

  fun <T> powBench(constructor: T, matmul: (T, T) -> T): Long =
    measureTimeMillis { constructor.power(100, matmul) }

  private fun <T> T.power(exp: Int, matmul: (T, T) -> T) =
    (0..exp).fold(this) { acc, i -> matmul(acc, this) }
}