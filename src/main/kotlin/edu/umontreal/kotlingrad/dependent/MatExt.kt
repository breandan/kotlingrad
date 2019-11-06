package edu.umontreal.kotlingrad.dependent

fun <T, R : D100, C : D100, M : Mat<T, R, C>> M.transpose() =
  Mat(rowT = colT, colT = rowT, vec = Vec(colT, (0 until numCols).map { i -> Vec(rowT, contents.map { it[i] }) }))

@JvmName("longMatPlus") infix operator fun <R: D100, C: D100, M: Mat<Long, R, C>> M.plus(v: M): Mat<Long, R, C> = Mat(rowT, colT, Vec(rowT, contents.zip(v.contents).map { it.first + it.second }))
@JvmName("intMatPlus") infix operator fun <R: D100, C: D100, M: Mat<Int, R, C>> M.plus(v: M): Mat<Int, R, C> = Mat(rowT, colT, Vec(rowT, contents.zip(v.contents).map { it.first + it.second }))
@JvmName("shortMatPlus") infix operator fun <R: D100, C: D100, M: Mat<Short, R, C>> M.plus(v: M): Mat<Short, R, C> = Mat(rowT, colT, Vec(rowT, contents.zip(v.contents).map { it.first + it.second }))
@JvmName("byteMatPlus") infix operator fun <R: D100, C: D100, M: Mat<Byte, R, C>> M.plus(v: M): Mat<Byte, R, C> = Mat(rowT, colT, Vec(rowT, contents.zip(v.contents).map { it.first + it.second }))
@JvmName("doubleMatPlus") infix operator fun <R: D100, C: D100, M: Mat<Double, R, C>> M.plus(v: M): Mat<Double, R, C> = Mat(rowT, colT, Vec(rowT, contents.zip(v.contents).map { it.first + it.second }))
@JvmName("floatMatPlus") infix operator fun <R: D100, C: D100, M: Mat<Float, R, C>> M.plus(v: M): Mat<Float, R, C> = Mat(rowT, colT, Vec(rowT,  contents.zip(v.contents).map { it.first + it.second }))

@JvmName("longMatMinus") infix operator fun <R: D100, C: D100, M: Mat<Long, R, C>> M.minus(v: M): Mat<Long, R, C> = Mat(rowT, colT, Vec(rowT, contents.zip(v.contents).map { it.first - it.second }))
@JvmName("intMatMinus") infix operator fun <R: D100, C: D100, M: Mat<Int, R, C>> M.minus(v: M): Mat<Int, R, C> = Mat(rowT, colT, Vec(rowT, contents.zip(v.contents).map { it.first - it.second }))
@JvmName("shortMatMinus") infix operator fun <R: D100, C: D100, M: Mat<Short, R, C>> M.minus(v: M): Mat<Short, R, C> = Mat(rowT, colT, Vec(rowT, contents.zip(v.contents).map { it.first - it.second }))
@JvmName("byteMatMinus") infix operator fun <R: D100, C: D100, M: Mat<Byte, R, C>> M.minus(v: M): Mat<Byte, R, C> = Mat(rowT, colT, Vec(rowT, contents.zip(v.contents).map { it.first - it.second }))
@JvmName("doubleMatMinus") infix operator fun <R: D100, C: D100, M: Mat<Double, R, C>> M.minus(v: M): Mat<Double, R, C> = Mat(rowT, colT, Vec(rowT, contents.zip(v.contents).map { it.first - it.second }))
@JvmName("floatMatMinus") infix operator fun <R: D100, C: D100, M: Mat<Float, R, C>> M.minus(v: M): Mat<Float, R, C> = Mat(rowT, colT, Vec(rowT, contents.zip(v.contents).map { it.first - it.second }))

@JvmName("longMatMult") infix operator fun <R1: D100, C1: D100, C2: D100> Mat<Long, R1, C1>.times(v: Mat<Long, C1, C2>): Mat<Long, R1, C2> {
  val m1 = Array(numRows) { LongArray(numCols) }
  for (i in 0 until numRows)
    for (j in 0 until numCols)
      m1[i][j] = this[i][j]

  val m2 = Array(v.numRows) { LongArray(v.numCols) }
  for (i in 0 until v.numRows)
    for (j in 0 until v.numCols)
      m2[i][j] = v[i][j]

  val mat = Array(numRows) { LongArray(v.numCols) }
  for(i in 0 until numRows)
    for(j in 0 until v.numCols)
      for(k in 0 until v.numRows)
        mat[i][j] += this[i][k] * m2[k][j]

  return Mat(rowT, v.colT, Vec(rowT, mat.map { Vec(v.colT, it.toList()) }))
}

@JvmName("intMatMult") infix operator fun <R1: D100, C1: D100, C2: D100> Mat<Int, R1, C1>.times(v: Mat<Int, C1, C2>): Mat<Int, R1, C2> {
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

  return Mat(rowT, v.colT, Vec(rowT, mat.map { Vec(v.colT, it.toList()) }))
}

@JvmName("shortMatMult") infix operator fun <R1: D100, C1: D100, C2: D100> Mat<Short, R1, C1>.times(v: Mat<Short, C1, C2>): Mat<Short, R1, C2> {
  val m1 = Array(numRows) { ShortArray(numCols) }
  for (i in 0 until numRows)
    for (j in 0 until numCols)
      m1[i][j] = this[i][j]

  val m2 = Array(v.numRows) { ShortArray(v.numCols) }
  for (i in 0 until v.numRows)
    for (j in 0 until v.numCols)
      m2[i][j] = v[i][j]

  val mat = Array(numRows) { ShortArray(v.numCols) }
  for(i in 0 until numRows)
    for(j in 0 until v.numCols)
      for(k in 0 until v.numRows)
        mat[i][j] = (mat[i][j] + (this[i][k] * m2[k][j]).toShort()).toShort()


  return Mat(rowT, v.colT, Vec(rowT, mat.map { Vec(v.colT, it.toList()) }))
}

@JvmName("byteMatMult") infix operator fun <R1: D100, C1: D100, C2: D100> Mat<Byte, R1, C1>.times(v: Mat<Byte, C1, C2>): Mat<Byte, R1, C2> {
  val m1 = Array(numRows) { ByteArray(numCols) }
  for (i in 0 until numRows)
    for (j in 0 until numCols)
      m1[i][j] = this[i][j]

  val m2 = Array(v.numRows) { ByteArray(v.numCols) }
  for (i in 0 until v.numRows)
    for (j in 0 until v.numCols)
      m2[i][j] = v[i][j]

  val mat = Array(numRows) { ByteArray(v.numCols) }
  for(i in 0 until numRows)
    for(j in 0 until v.numCols)
      for(k in 0 until v.numRows)
        mat[i][j] = (mat[i][j] + (this[i][k] * m2[k][j]).toByte()).toByte()


  return Mat(rowT, v.colT, Vec(rowT, mat.map { Vec(v.colT, it.toList()) }))
}

@JvmName("doubleMatMult") infix operator fun <R1: D100, C1: D100, C2: D100> Mat<Double, R1, C1>.times(v: Mat<Double, C1, C2>): Mat<Double, R1, C2> {
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

  return Mat(rowT, v.colT, Vec(rowT, mat.map { Vec(v.colT, it.toList()) }))
}

@JvmName("floatMatMult") infix operator fun <R1: D100, C1: D100, C2: D100> Mat<Float, R1, C1>.times(v: Mat<Float, C1, C2>): Mat<Float, R1, C2> {
  val m1 = Array(numRows) { FloatArray(numCols) }
  for (i in 0 until numRows)
    for (j in 0 until numCols)
      m1[i][j] = this[i][j]

  val m2 = Array(v.numRows) { FloatArray(v.numCols) }
  for (i in 0 until v.numRows)
    for (j in 0 until v.numCols)
      m2[i][j] = v[i][j]

  val mat = Array(numRows) { FloatArray(v.numCols) }
  for(i in 0 until numRows)
    for(j in 0 until v.numCols)
      for(k in 0 until v.numRows)
        mat[i][j] += this[i][k] * m2[k][j]

  return Mat(rowT, v.colT, Vec(rowT, mat.map { Vec(v.colT, it.toList()) }))
}
