package edu.umontreal.kotlingrad.perf

import ch.ethz.idsc.tensor.*
import edu.mcgill.kaliningraph.*
import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.round
import edu.umontreal.kotlingrad.utils.*
import org.apache.commons.math3.linear.*
import org.ejml.data.*
import org.ejml.kotlin.times
import org.jetbrains.bio.viktor.F64Array
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
//import org.tensorflow.ndarray.*
//import org.tensorflow.ndarray.buffer.DataBuffers
import scientifik.kmath.linear.*
import scientifik.kmath.structures.*
import scientifik.kmath.structures.Matrix
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class SparseTest {
  val m = 100
  val sparsity = 0.1
  val fill = { if (DEFAULT_RANDOM.nextDouble() < sparsity) DEFAULT_RANDOM.nextDouble() else 0.0 }
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
   */

  @Test
  fun testDoubleMatrixMultiplication() {
    var baseline = 1000L
    contents.run {
      arrayOf(
        "EJML/S" to powBench(toEJMLSparse()) { a, b -> a * b },
        "EJML/D" to powBench(toEJMLDense()) { a, b -> a * b },
        "ND4J/N" to powBench(toNd4j()) { a, b -> a.mmul(b) },
        "APACHE" to powBench(toApacheCommons()) { a, b -> a.multiply(b) },
        "VIKTOR" to powBench(toViktor()) { a, b -> a matmul b }.also { baseline = it },
        "KTGRAD" to powBench(toKTGrad()) { a, b -> (a * b) as MConst<DReal, D100, D100> },
        "KTMATH" to powBench(toKMath()) { a, b -> a dot b }
          .also { assert(it < 20 * baseline) { "Perf regression: $it ms" } },
        "TENSOR" to powBench(toTensor()) { a, b -> a.dot(b) }
//        "TF4J/N" to powBench(toTf4j()) { a, b ->
//          EagerSession.create().use {
//            val tf = Ops.create(it)
//            tf.math.mul(a, b)
//          }
//        }
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
        toNd4j().power(10) { a, b -> a.mmul(b) }.toKotlinArray().round(),
        toViktor().power(10) { a, b -> a matmul b }.toKotlinArray().round(),
        toKTGrad().power(10) { a, b -> (a * b) as MConst<DReal, D100, D100> }.toKotlinArray().round(),
        toKMath().power(10) { a, b -> a.dot(b) }.toKotlinArray().round(),
        toTensor().power(10) { a, b -> a.dot(b) }.toKotlinArray().round()
      )
    }

    powered.reduce { acc, i -> acc.also { assertArrayEquals(it, i) } }
  }

  fun mapper(rows: Int, cols: Int, e: (Int, Int) -> Double) =
    Array(rows) { i -> DoubleArray(cols) { j -> e(i, j) } }

  fun RealMatrix.toKotlinArray() = data
  fun DMatrix.toKotlinArray() = mapper(numRows, numCols) { i, j -> this[i, j] }
  fun Structure2D<out Number>.toKotlinArray() = mapper(rowNum, colNum) { i, j -> this[i, j].toDouble() }
  fun Tensor.toKotlinArray() = mapper(this[0].length(), this[1].length()) { i, j -> Get(i, j).number().toDouble() }
  fun Mat<DReal, *, *>.toKotlinArray() = mapper(numRows, numCols) { i, j -> this[i, j]().toDouble() }
  fun INDArray.toKotlinArray() = mapper(rows(), columns()) { i, j -> getDouble(i, j) }
//  fun DoubleNdArray.toKotlinArray() = mapper(shape().size(0).toInt(), shape().size(1).toInt()) { i, j -> getDouble(i.toLong(), j.toLong()) }

  fun Array<DoubleArray>.toApacheCommons() = MatrixUtils.createRealMatrix(this)
  fun Array<DoubleArray>.toViktor() = F64Array(size, size) { i, j -> this[i][j] }
  fun Array<DoubleArray>.toKMath() = VirtualMatrix(size, size) { i, j -> this[i][j] } as Matrix<Double>
  fun Array<DoubleArray>.toKTGrad() = DoublePrecision.Mat(D100, D100) { a, b -> this@toKTGrad[a][b] }
  fun Array<DoubleArray>.toTensor() = Tensors.matrixDouble(this)
  fun Array<DoubleArray>.toNd4j() = Nd4j.create(this)
//  fun Array<DoubleArray>.toTf4j() = NdArrays.ofDoubles(Shape.of(size.toLong(), size.toLong()))
//    .also { it.write(DataBuffers.of(*flatMap { it.toList() }.toDoubleArray())) }

  fun Array<DoubleArray>.round(precision: Int = 3) =
    map { it.map { it.round(precision) }.toDoubleArray() }.toTypedArray()

  fun <T> powBench(constructor: T, matmul: (T, T) -> T): Long =
    measureTimeMillis { constructor.power(100, matmul) }

  private fun <T> T.power(exp: Int, matmul: (T, T) -> T) =
    (0..exp).fold(this) { acc, i -> matmul(acc, this) }
}