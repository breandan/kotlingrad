package edu.umontreal.kotlingrad.dependent

open class Mat<T, MaxRows: `4`, MaxCols: `4`> internal constructor(val contents: List<Vec<T, MaxCols>> = arrayListOf()) {
  companion object {
    @JvmName("m1x1") private operator fun <T, C: `1`, V: Vec<T, C>> invoke(t: V): Mat<T, `1`, C> = Mat(arrayListOf(t))
    @JvmName("m1x2") private operator fun <T, C: `2`, V: Vec<T, C>> invoke(t: V): Mat<T, `1`, C> = Mat(arrayListOf(t))
    @JvmName("m1x3") private operator fun <T, C: `3`, V: Vec<T, C>> invoke(t: V): Mat<T, `1`, C> = Mat(arrayListOf(t))
    @JvmName("m1x4") private operator fun <T, C: `4`, V: Vec<T, C>> invoke(t: V): Mat<T, `1`, C> = Mat(arrayListOf(t))
    @JvmName("m2x1") private operator fun <T, C: `1`, V: Vec<T, C>> invoke(t0: V, t1: V): Mat<T, `2`, C> = Mat(arrayListOf(t0, t1))
    @JvmName("m2x2") private operator fun <T, C: `2`, V: Vec<T, C>> invoke(t0: V, t1: V): Mat<T, `2`, C> = Mat(arrayListOf(t0, t1))
    @JvmName("m2x3") private operator fun <T, C: `3`, V: Vec<T, C>> invoke(t0: V, t1: V): Mat<T, `2`, C> = Mat(arrayListOf(t0, t1))
    @JvmName("m2x4") private operator fun <T, C: `4`, V: Vec<T, C>> invoke(t0: V, t1: V): Mat<T, `2`, C> = Mat(arrayListOf(t0, t1))
    @JvmName("m3x1") private operator fun <T, C: `1`, V: Vec<T, C>> invoke(t0: V, t1: V, t2: V): Mat<T, `3`, C> = Mat(arrayListOf(t0, t1, t2))
    @JvmName("m3x2") private operator fun <T, C: `2`, V: Vec<T, C>> invoke(t0: V, t1: V, t2: V): Mat<T, `3`, C> = Mat(arrayListOf(t0, t1, t2))
    @JvmName("m3x3") private operator fun <T, C: `3`, V: Vec<T, C>> invoke(t0: V, t1: V, t2: V): Mat<T, `3`, C> = Mat(arrayListOf(t0, t1, t2))
    @JvmName("m3x4") private operator fun <T, C: `4`, V: Vec<T, C>> invoke(t0: V, t1: V, t2: V): Mat<T, `3`, C> = Mat(arrayListOf(t0, t1, t2))
    @JvmName("m4x1") private operator fun <T, C: `1`, V: Vec<T, C>> invoke(t0: V, t1: V, t2: V, t3: V): Mat<T, `4`, C> = Mat(arrayListOf(t0, t1, t2, t3))
    @JvmName("m4x2") private operator fun <T, C: `2`, V: Vec<T, C>> invoke(t0: V, t1: V, t2: V, t3: V): Mat<T, `4`, C> = Mat(arrayListOf(t0, t1, t2, t3))
    @JvmName("m4x3") private operator fun <T, C: `3`, V: Vec<T, C>> invoke(t0: V, t1: V, t2: V, t3: V): Mat<T, `4`, C> = Mat(arrayListOf(t0, t1, t2, t3))
    @JvmName("m4x4") private operator fun <T, C: `4`, V: Vec<T, C>> invoke(t0: V, t1: V, t2: V, t3: V): Mat<T, `4`, C> = Mat(arrayListOf(t0, t1, t2, t3))

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

  @JvmName("matAddLong") infix fun <R: MaxRows, C: MaxCols, M: Mat<Long, R, C>> M.add(v: M) = Mat<Long, R, C>(contents.zip(v.contents).map { it.first + it.second })
  @JvmName("matAddInt") infix fun <R: MaxRows, C: MaxCols, M: Mat<Int, R, C>> M.add(v: M) = Mat<Int, R, C>(contents.zip(v.contents).map { it.first + it.second })
  @JvmName("matAddShort") infix fun <R: MaxRows, C: MaxCols, M: Mat<Short, R, C>> M.add(v: M) = Mat<Short, R, C>(contents.zip(v.contents).map { it.first + it.second })
  @JvmName("matAddByte") infix fun <R: MaxRows, C: MaxCols, M: Mat<Byte, R, C>> M.add(v: M) = Mat<Byte, R, C>(contents.zip(v.contents).map { it.first + it.second })
  @JvmName("matAddDouble") infix fun <R: MaxRows, C: MaxCols, M: Mat<Double, R, C>> M.add(v: M) = Mat<Double, R, C>(contents.zip(v.contents).map { it.first + it.second })
  @JvmName("matAddFloat") infix fun <R: MaxRows, C: MaxCols, M: Mat<Float, R, C>> M.add(v: M) = Mat<Float, R, C>(contents.zip(v.contents).map { it.first + it.second })

  override fun toString() = "$contents"
}

@JvmName("matPlusLong") infix operator fun <R: `4`, C: `4`, M: Mat<Long, R, C>> M.plus(v: M): Mat<Long, R, C> = add(v)
@JvmName("matPlusInt") infix operator fun <R: `4`, C: `4`, M: Mat<Int, R, C>> M.plus(v: M): Mat<Int, R, C> = add(v)
@JvmName("matPlusShort") infix operator fun <R: `4`, C: `4`, M: Mat<Short, R, C>> M.plus(v: M): Mat<Short, R, C> = add(v)
@JvmName("matPlusByte") infix operator fun <R: `4`, C: `4`, M: Mat<Byte, R, C>> M.plus(v: M): Mat<Byte, R, C> = add(v)
@JvmName("matPlusDouble") infix operator fun <R: `4`, C: `4`, M: Mat<Double, R, C>> M.plus(v: M): Mat<Double, R, C> = add(v)
@JvmName("matPlusFloat") infix operator fun <R: `4`, C: `4`, M: Mat<Float, R, C>> M.plus(v: M): Mat<Float, R, C> = add(v)

// TODO: implement matrix multiplication semantics
operator fun <T: Number, M: `4`, N: `4`, P: `4`> Mat<T, M, N>.times(m: Mat<T, N, P>): Mat<T, M, P> = Mat()