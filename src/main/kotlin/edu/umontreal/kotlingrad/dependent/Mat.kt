package edu.umontreal.kotlingrad.dependent

open class Mat<T, MaxRows: `4`, MaxCols: `4`> internal constructor(/**TODO: Make contents a Vec<Vec<T, MaxCols>, MaxRows>**/val contents: List<Vec<T, MaxCols>> = arrayListOf(), val rows: Nat<MaxRows>, val cols: Nat<MaxCols>) /**TODO: Maybe extend Vec? **/ {
  companion object {
    @JvmName("m1x1") private operator fun <T, V: Vec<T, `1`>> invoke(t: V): Mat<T, `1`, `1`> = Mat(arrayListOf(t), `1`, `1`)
    @JvmName("m1x2") private operator fun <T, V: Vec<T, `2`>> invoke(t: V): Mat<T, `1`, `2`> = Mat(arrayListOf(t), `1`, `2`)
    @JvmName("m1x3") private operator fun <T, V: Vec<T, `3`>> invoke(t: V): Mat<T, `1`, `3`> = Mat(arrayListOf(t), `1`, `3`)
    @JvmName("m1x4") private operator fun <T, V: Vec<T, `4`>> invoke(t: V): Mat<T, `1`, `4`> = Mat(arrayListOf(t), `1`, `4`)
    @JvmName("m2x1") private operator fun <T, V: Vec<T, `1`>> invoke(t0: V, t1: V): Mat<T, `2`, `1`> = Mat(arrayListOf(t0, t1), `2`, `1`)
    @JvmName("m2x2") private operator fun <T, V: Vec<T, `2`>> invoke(t0: V, t1: V): Mat<T, `2`, `2`> = Mat(arrayListOf(t0, t1), `2`, `2`)
    @JvmName("m2x3") private operator fun <T, V: Vec<T, `3`>> invoke(t0: V, t1: V): Mat<T, `2`, `3`> = Mat(arrayListOf(t0, t1), `2`, `3`)
    @JvmName("m2x4") private operator fun <T, V: Vec<T, `4`>> invoke(t0: V, t1: V): Mat<T, `2`, `4`> = Mat(arrayListOf(t0, t1), `2`, `4`)
    @JvmName("m3x1") private operator fun <T, V: Vec<T, `1`>> invoke(t0: V, t1: V, t2: V): Mat<T, `3`, `1`> = Mat(arrayListOf(t0, t1, t2), `3`, `1`)
    @JvmName("m3x2") private operator fun <T, V: Vec<T, `2`>> invoke(t0: V, t1: V, t2: V): Mat<T, `3`, `2`> = Mat(arrayListOf(t0, t1, t2), `3`, `2`)
    @JvmName("m3x3") private operator fun <T, V: Vec<T, `3`>> invoke(t0: V, t1: V, t2: V): Mat<T, `3`, `3`> = Mat(arrayListOf(t0, t1, t2), `3`, `3`)
    @JvmName("m3x4") private operator fun <T, V: Vec<T, `4`>> invoke(t0: V, t1: V, t2: V): Mat<T, `3`, `4`> = Mat(arrayListOf(t0, t1, t2), `3`, `4`)
    @JvmName("m4x1") private operator fun <T, V: Vec<T, `1`>> invoke(t0: V, t1: V, t2: V, t3: V): Mat<T, `4`, `1`> = Mat(arrayListOf(t0, t1, t2, t3), `4`, `1`)
    @JvmName("m4x2") private operator fun <T, V: Vec<T, `2`>> invoke(t0: V, t1: V, t2: V, t3: V): Mat<T, `4`, `2`> = Mat(arrayListOf(t0, t1, t2, t3), `4`, `2`)
    @JvmName("m4x3") private operator fun <T, V: Vec<T, `3`>> invoke(t0: V, t1: V, t2: V, t3: V): Mat<T, `4`, `3`> = Mat(arrayListOf(t0, t1, t2, t3), `4`, `3`)
    @JvmName("m4x4") private operator fun <T, V: Vec<T, `4`>> invoke(t0: V, t1: V, t2: V, t3: V): Mat<T, `4`, `4`> = Mat(arrayListOf(t0, t1, t2, t3), `4`, `4`)

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

  @JvmName("longMatAdd") infix fun <R: MaxRows, C: MaxCols, M: Mat<Long, R, C>> M.add(v: M) = Mat<Long, R, C>(contents.zip(v.contents).map { it.first + it.second }, rows, cols)
  @JvmName("intMatAdd") infix fun <R: MaxRows, C: MaxCols, M: Mat<Int, R, C>> M.add(v: M) = Mat<Int, R, C>(contents.zip(v.contents).map { it.first + it.second }, rows, cols)
  @JvmName("shortMatAdd") infix fun <R: MaxRows, C: MaxCols, M: Mat<Short, R, C>> M.add(v: M) = Mat<Short, R, C>(contents.zip(v.contents).map { it.first + it.second }, rows, cols)
  @JvmName("byteMatAdd") infix fun <R: MaxRows, C: MaxCols, M: Mat<Byte, R, C>> M.add(v: M) = Mat<Byte, R, C>(contents.zip(v.contents).map { it.first + it.second }, rows, cols)
  @JvmName("doubleMatAdd") infix fun <R: MaxRows, C: MaxCols, M: Mat<Double, R, C>> M.add(v: M) = Mat<Double, R, C>(contents.zip(v.contents).map { it.first + it.second }, rows, cols)
  @JvmName("floatMatAdd") infix fun <R: MaxRows, C: MaxCols, M: Mat<Float, R, C>> M.add(v: M) = Mat<Float, R, C>(contents.zip(v.contents).map { it.first + it.second }, rows, cols)

  override fun toString() = "$contents"
}

@JvmName("doubleMatTranspose") fun <R: `4`, C: `4`, M: Mat<Double, R, C>> M.transpose() =
  Mat<Double, C, R>(contents = (0 until contents.size).map { i -> Vec(contents = contents.map { it[i] }, length = rows) }, rows = cols, cols = rows)

// TODO: implement matrix multiplication semantics
operator fun <T: Number, M: `4`, N: `4`, P: `4`> Mat<T, M, N>.times(m: Mat<T, N, P>): Mat<T, M, P> = Mat(listOf(), rows, m.cols)

@JvmName("longMatPlus") infix operator fun <R: `4`, C: `4`, M: Mat<Long, R, C>> M.plus(v: M): Mat<Long, R, C> = add(v)
@JvmName("intMatPlus") infix operator fun <R: `4`, C: `4`, M: Mat<Int, R, C>> M.plus(v: M): Mat<Int, R, C> = add(v)
@JvmName("shortMatPlus") infix operator fun <R: `4`, C: `4`, M: Mat<Short, R, C>> M.plus(v: M): Mat<Short, R, C> = add(v)
@JvmName("byteMatPlus") infix operator fun <R: `4`, C: `4`, M: Mat<Byte, R, C>> M.plus(v: M): Mat<Byte, R, C> = add(v)
@JvmName("doubleMatPlus") infix operator fun <R: `4`, C: `4`, M: Mat<Double, R, C>> M.plus(v: M): Mat<Double, R, C> = add(v)
@JvmName("floatMatPlus") infix operator fun <R: `4`, C: `4`, M: Mat<Float, R, C>> M.plus(v: M): Mat<Float, R, C> = add(v)