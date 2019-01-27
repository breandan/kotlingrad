package edu.umontreal.kotlingrad.dependent

open class Mat<T, Rows: `4`, Cols: `4`> internal constructor(
  val rowT: Nat<Rows>,
  val colT: Nat<Cols>,
  /**TODO: Make contents a Vec<Vec<T, MaxCols>, MaxRows>**/
  val contents: List<Vec<T, Cols>> = arrayListOf()
) /**TODO: Maybe extend Vec? **/ {
  val numRows: Int = rowT.i
  val numCols: Int = colT.i
  operator fun get(i: Int): Vec<T, Cols> = contents[i]

  companion object {
    @JvmName("m1x1") private operator fun <T, V: Vec<T, `1`>> invoke(t: V): Mat<T, `1`, `1`> = Mat(`1`, `1`, arrayListOf(t))
    @JvmName("m1x2") private operator fun <T, V: Vec<T, `2`>> invoke(t: V): Mat<T, `1`, `2`> = Mat(`1`, `2`, arrayListOf(t))
    @JvmName("m1x3") private operator fun <T, V: Vec<T, `3`>> invoke(t: V): Mat<T, `1`, `3`> = Mat(`1`, `3`, arrayListOf(t))
    @JvmName("m1x4") private operator fun <T, V: Vec<T, `4`>> invoke(t: V): Mat<T, `1`, `4`> = Mat(`1`, `4`, arrayListOf(t))
    @JvmName("m2x1") private operator fun <T, V: Vec<T, `1`>> invoke(t0: V, t1: V): Mat<T, `2`, `1`> = Mat(`2`, `1`, arrayListOf(t0, t1))
    @JvmName("m2x2") private operator fun <T, V: Vec<T, `2`>> invoke(t0: V, t1: V): Mat<T, `2`, `2`> = Mat(`2`, `2`, arrayListOf(t0, t1))
    @JvmName("m2x3") private operator fun <T, V: Vec<T, `3`>> invoke(t0: V, t1: V): Mat<T, `2`, `3`> = Mat(`2`, `3`, arrayListOf(t0, t1))
    @JvmName("m2x4") private operator fun <T, V: Vec<T, `4`>> invoke(t0: V, t1: V): Mat<T, `2`, `4`> = Mat(`2`, `4`, arrayListOf(t0, t1))
    @JvmName("m3x1") private operator fun <T, V: Vec<T, `1`>> invoke(t0: V, t1: V, t2: V): Mat<T, `3`, `1`> = Mat(`3`, `1`, arrayListOf(t0, t1, t2))
    @JvmName("m3x2") private operator fun <T, V: Vec<T, `2`>> invoke(t0: V, t1: V, t2: V): Mat<T, `3`, `2`> = Mat(`3`, `2`, arrayListOf(t0, t1, t2))
    @JvmName("m3x3") private operator fun <T, V: Vec<T, `3`>> invoke(t0: V, t1: V, t2: V): Mat<T, `3`, `3`> = Mat(`3`, `3`, arrayListOf(t0, t1, t2))
    @JvmName("m3x4") private operator fun <T, V: Vec<T, `4`>> invoke(t0: V, t1: V, t2: V): Mat<T, `3`, `4`> = Mat(`3`, `4`, arrayListOf(t0, t1, t2))
    @JvmName("m4x1") private operator fun <T, V: Vec<T, `1`>> invoke(t0: V, t1: V, t2: V, t3: V): Mat<T, `4`, `1`> = Mat(`4`, `1`, arrayListOf(t0, t1, t2, t3))
    @JvmName("m4x2") private operator fun <T, V: Vec<T, `2`>> invoke(t0: V, t1: V, t2: V, t3: V): Mat<T, `4`, `2`> = Mat(`4`, `2`, arrayListOf(t0, t1, t2, t3))
    @JvmName("m4x3") private operator fun <T, V: Vec<T, `3`>> invoke(t0: V, t1: V, t2: V, t3: V): Mat<T, `4`, `3`> = Mat(`4`, `3`, arrayListOf(t0, t1, t2, t3))
    @JvmName("m4x4") private operator fun <T, V: Vec<T, `4`>> invoke(t0: V, t1: V, t2: V, t3: V): Mat<T, `4`, `4`> = Mat(`4`, `4`, arrayListOf(t0, t1, t2, t3))

    @JvmName("mf1x1") operator fun <T> invoke(m: Nat<`1`>, n: Nat<`1`>, d0: T) = Mat(Vec(d0))
    @JvmName("mf1x2") operator fun <T> invoke(m: Nat<`1`>, n: Nat<`2`>, d0: T, d1: T) = Mat(Vec(d0, d1))
    @JvmName("mf1x3") operator fun <T> invoke(m: Nat<`1`>, n: Nat<`3`>, d0: T, d1: T, d2: T) = Mat(Vec(d0, d1, d2))
    @JvmName("mf1x3") operator fun <T> invoke(m: Nat<`1`>, n: Nat<`4`>, d0: T, d1: T, d2: T, d3: T) = Mat(Vec(d0, d1, d2, d3))
    @JvmName("mf2x1") operator fun <T> invoke(m: Nat<`2`>, n: Nat<`1`>, d0: T, d1: T) = Mat(Vec(d0), Vec(d1))
    @JvmName("mf2x2") operator fun <T> invoke(m: Nat<`2`>, n: Nat<`2`>, d0: T, d1: T, d2: T, d3: T) = Mat(Vec(d0, d1), Vec(d2, d3))
    @JvmName("mf2x3") operator fun <T> invoke(m: Nat<`2`>, n: Nat<`3`>, d0: T, d1: T, d2: T, d3: T, d4: T, d5: T) = Mat(Vec(d0, d1, d2), Vec(d3, d4, d5))
    @JvmName("mf2x4") operator fun <T> invoke(m: Nat<`2`>, n: Nat<`4`>, d0: T, d1: T, d2: T, d3: T, d4: T, d5: T, d6: T, d7: T) = Mat(Vec(d0, d1, d2, d3), Vec(d4, d5, d6, d7))
    @JvmName("mf3x1") operator fun <T> invoke(m: Nat<`3`>, n: Nat<`1`>, d0: T, d1: T, d2: T) = Mat(Vec(d0), Vec(d1), Vec(d2))
    @JvmName("mf3x2") operator fun <T> invoke(m: Nat<`3`>, n: Nat<`2`>, d0: T, d1: T, d2: T, d3: T, d4: T, d5: T) = Mat(Vec(d0, d1), Vec(d2, d3), Vec(d4, d5))
    @JvmName("mf3x3") operator fun <T> invoke(m: Nat<`3`>, n: Nat<`3`>, d0: T, d1: T, d2: T, d3: T, d4: T, d5: T, d6: T, d7: T, d8: T) = Mat(Vec(d0, d1, d2), Vec(d3, d4, d5), Vec(d6, d7, d8))
    @JvmName("mf3x4") operator fun <T> invoke(m: Nat<`3`>, n: Nat<`4`>, d0: T, d1: T, d2: T, d3: T, d4: T, d5: T, d6: T, d7: T, d8: T, d9: T, d10: T, d11: T) = Mat(Vec(d0, d1, d2, d3), Vec(d4, d5, d6, d7), Vec(d8, d9, d10, d11))
    @JvmName("mf4x1") operator fun <T> invoke(m: Nat<`4`>, n: Nat<`1`>, d0: T, d1: T, d2: T, d3: T) = Mat(Vec(d0), Vec(d1), Vec(d2), Vec(d3))
    @JvmName("mf4x2") operator fun <T> invoke(m: Nat<`4`>, n: Nat<`2`>, d0: T, d1: T, d2: T, d3: T, d4: T, d5: T, d6: T, d7: T) = Mat(Vec(d0, d1), Vec(d2, d3), Vec(d4, d5), Vec(d6, d7))
    @JvmName("mf4x3") operator fun <T> invoke(m: Nat<`4`>, n: Nat<`3`>, d0: T, d1: T, d2: T, d3: T, d4: T, d5: T, d6: T, d7: T, d8: T, d9: T, d10: T, d11: T) = Mat(Vec(d0, d1, d2), Vec(d3, d4, d5), Vec(d6, d7, d8), Vec(d9, d10, d11))
    @JvmName("mf4x4") operator fun <T> invoke(m: Nat<`4`>, n: Nat<`4`>, d0: T, d1: T, d2: T, d3: T, d4: T, d5: T, d6: T, d7: T, d8: T, d9: T, d10: T, d11: T, d12: T, d13: T, d14: T, d15: T) = Mat(Vec(d0, d1, d2, d3), Vec(d4, d5, d6, d7), Vec(d8, d9, d10, d11), Vec(d12, d13, d14, d15))
  }

  @JvmName("longMatAdd") infix fun <R: Rows, C: Cols, M: Mat<Long, R, C>> M.add(v: M) = Mat<Long, R, C>(rowT, colT, contents.zip(v.contents).map { it.first + it.second })
  @JvmName("intMatAdd") infix fun <R: Rows, C: Cols, M: Mat<Int, R, C>> M.add(v: M) = Mat<Int, R, C>(rowT, colT, contents.zip(v.contents).map { it.first + it.second })
  @JvmName("shortMatAdd") infix fun <R: Rows, C: Cols, M: Mat<Short, R, C>> M.add(v: M) = Mat<Short, R, C>(rowT, colT, contents.zip(v.contents).map { it.first + it.second })
  @JvmName("byteMatAdd") infix fun <R: Rows, C: Cols, M: Mat<Byte, R, C>> M.add(v: M) = Mat<Byte, R, C>(rowT, colT, contents.zip(v.contents).map { it.first + it.second })
  @JvmName("doubleMatAdd") infix fun <R: Rows, C: Cols, M: Mat<Double, R, C>> M.add(v: M) = Mat<Double, R, C>(rowT, colT, contents.zip(v.contents).map { it.first + it.second })
  @JvmName("floatMatAdd") infix fun <R: Rows, C: Cols, M: Mat<Float, R, C>> M.add(v: M) = Mat<Float, R, C>(rowT, colT, contents.zip(v.contents).map { it.first + it.second })

  @JvmName("doubleMatMult") infix fun <R1: Rows, C1: Cols, C2: `4`> Mat<Double, R1, C1>.mult(v: Mat<Double, C1, C2>): Mat<Double, R1, C2> {
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

  override fun toString() = "($numRows x $numCols) $contents"
}

@JvmName("doubleMatTranspose") fun <R: `4`, C: `4`, M: Mat<Double, R, C>> M.transpose() =
  Mat<Double, C, R>(contents = (0 until numCols).map { i -> Vec(contents = contents.map { it[i] }, length = rowT) }, rowT = colT, colT = rowT)

// TODO: implement matrix multiplication semanticslist of lists matrix product map flat
operator fun <M: `4`, N: `4`, P: `4`> Mat<Double, M, N>.times(v: Mat<Double, N, P>): Mat<Double, M, P> = mult(v)


@JvmName("longMatPlus") infix operator fun <R: `4`, C: `4`, M: Mat<Long, R, C>> M.plus(v: M): Mat<Long, R, C> = add(v)
@JvmName("intMatPlus") infix operator fun <R: `4`, C: `4`, M: Mat<Int, R, C>> M.plus(v: M): Mat<Int, R, C> = add(v)
@JvmName("shortMatPlus") infix operator fun <R: `4`, C: `4`, M: Mat<Short, R, C>> M.plus(v: M): Mat<Short, R, C> = add(v)
@JvmName("byteMatPlus") infix operator fun <R: `4`, C: `4`, M: Mat<Byte, R, C>> M.plus(v: M): Mat<Byte, R, C> = add(v)
@JvmName("doubleMatPlus") infix operator fun <R: `4`, C: `4`, M: Mat<Double, R, C>> M.plus(v: M): Mat<Double, R, C> = add(v)
@JvmName("floatMatPlus") infix operator fun <R: `4`, C: `4`, M: Mat<Float, R, C>> M.plus(v: M): Mat<Float, R, C> = add(v)