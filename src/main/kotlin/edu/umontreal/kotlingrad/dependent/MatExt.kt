package edu.umontreal.kotlingrad.dependent

@JvmName("doubleMatTranspose") fun <R: `9`, C: `9`, M: Mat<Double, R, C>> M.transpose() =
  Mat<Double, C, R>(contents = (0 until numCols).map { i -> Vec(contents = contents.map { it[i] }, length = rowT) }, rowT = colT, colT = rowT)

@JvmName("longMatPlus") infix operator fun <R: `9`, C: `9`, M: Mat<Long, R, C>> M.plus(v: M): Mat<Long, R, C> = Mat<Long, R, C>(rowT, colT, contents.zip(v.contents).map { it.first + it.second })
@JvmName("intMatPlus") infix operator fun <R: `9`, C: `9`, M: Mat<Int, R, C>> M.plus(v: M): Mat<Int, R, C> = Mat<Int, R, C>(rowT, colT, contents.zip(v.contents).map { it.first + it.second })
@JvmName("shortMatPlus") infix operator fun <R: `9`, C: `9`, M: Mat<Short, R, C>> M.plus(v: M): Mat<Short, R, C> = Mat<Short, R, C>(rowT, colT, contents.zip(v.contents).map { it.first + it.second })
@JvmName("byteMatPlus") infix operator fun <R: `9`, C: `9`, M: Mat<Byte, R, C>> M.plus(v: M): Mat<Byte, R, C> = Mat<Byte, R, C>(rowT, colT, contents.zip(v.contents).map { it.first + it.second })
@JvmName("doubleMatPlus") infix operator fun <R: `9`, C: `9`, M: Mat<Double, R, C>> M.plus(v: M): Mat<Double, R, C> = Mat<Double, R, C>(rowT, colT, contents.zip(v.contents).map { it.first + it.second })
@JvmName("floatMatPlus") infix operator fun <R: `9`, C: `9`, M: Mat<Float, R, C>> M.plus(v: M): Mat<Float, R, C> = Mat<Float, R, C>(rowT, colT, contents.zip(v.contents).map { it.first + it.second })

@JvmName("intMatMult") infix operator fun <R1: `9`, C1: `9`, C2: `9`> Mat<Int, R1, C1>.times(v: Mat<Int, C1, C2>): Mat<Int, R1, C2> {
  val m1 = Array(numRows) { IntArray(numCols) }
  for (i in 0 until numRows)
    for (j in 0 until numCols)
      m1[i][j] = this[i][j]

  val m2 = Array(v.numRows) { IntArray(v.numCols) }
  for (i in 0 until v.numRows)
    for (j in 0 until v.numCols)
      m2[i][j] = v[i][j]

  val mat = Array(numRows) { IntArray(v.numCols) }
  for(i in 0 until numRows)
    for(j in 0 until v.numCols)
      for(k in 0 until v.numRows)
        mat[i][j] += this[i][k] * m2[k][j]

  return Mat(rowT, v.colT, mat.map { Vec(v.colT, it.toList()) })
}

@JvmName("doubleMatMult") infix operator fun <R1: `9`, C1: `9`, C2: `9`> Mat<Double, R1, C1>.times(v: Mat<Double, C1, C2>): Mat<Double, R1, C2> {
  val m1 = Array(numRows) { DoubleArray(numCols) }
  for (i in 0 until numRows)
    for (j in 0 until numCols)
      m1[i][j] = this[i][j]

  val m2 = Array(v.numRows) { DoubleArray(v.numCols) }
  for (i in 0 until v.numRows)
    for (j in 0 until v.numCols)
      m2[i][j] = v[i][j]

  val mat = Array(numRows) { DoubleArray(v.numCols) }
  for(i in 0 until numRows)
    for(j in 0 until v.numCols)
      for(k in 0 until v.numRows)
        mat[i][j] += this[i][k] * m2[k][j]

  return Mat(rowT, v.colT, mat.map { Vec(v.colT, it.toList()) })
}