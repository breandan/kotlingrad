package edu.umontreal.kotlingrad.functions.types.dependent

open class Mat<T, MaxRows: `4`, MaxCols: `4`> internal constructor(val contents: List<Vec<T, MaxCols>> = arrayListOf()) {
  companion object {
    operator fun <T> invoke(): Mat<T, `0`, `0`> = Mat()
    @JvmName("m1x1") operator fun <T, C: `1`, V: Vec<T, C>> invoke(t: V): Mat<T, `1`, C> = Mat(arrayListOf(t))
    @JvmName("m1x2") operator fun <T, C: `2`, V: Vec<T, C>> invoke(t: V): Mat<T, `1`, C> = Mat(arrayListOf(t))
    @JvmName("m1x3") operator fun <T, C: `3`, V: Vec<T, C>> invoke(t: V): Mat<T, `1`, C> = Mat(arrayListOf(t))
    @JvmName("m2x1") operator fun <T, C: `1`, V: Vec<T, C>> invoke(t0: V, t1: V): Mat<T, `2`, C> = Mat(arrayListOf(t0, t1))
    @JvmName("m2x2") operator fun <T, C: `2`, V: Vec<T, C>> invoke(t0: V, t1: V): Mat<T, `2`, C> = Mat(arrayListOf(t0, t1))
    @JvmName("m2x3") operator fun <T, C: `3`, V: Vec<T, C>> invoke(t0: V, t1: V): Mat<T, `2`, C> = Mat(arrayListOf(t0, t1))
    @JvmName("m3x1") operator fun <T, C: `1`, V: Vec<T, C>> invoke(t0: V, t1: V, t2: V): Mat<T, `3`, C> = Mat(arrayListOf(t0, t1, t2))
    @JvmName("m3x2") operator fun <T, C: `2`, V: Vec<T, C>> invoke(t0: V, t1: V, t2: V): Mat<T, `3`, C> = Mat(arrayListOf(t0, t1, t2))
    @JvmName("m3x3") operator fun <T, C: `3`, V: Vec<T, C>> invoke(t0: V, t1: V, t2: V): Mat<T, `3`, C> = Mat(arrayListOf(t0, t1, t2))

    @JvmName("mf1x1") operator fun <T> invoke(m: `1`, n: `1`, d0: T) = Mat(Vec(d0))
    @JvmName("mf1x2") operator fun <T> invoke(m: `1`, n: `2`, d0: T, d1: T) = Mat(Vec(d0, d1))
    @JvmName("mf1x3") operator fun <T> invoke(m: `1`, n: `3`, d0: T, d1: T, d2: T) = Mat(Vec(d0, d1, d2))
    @JvmName("mf2x1") operator fun <T> invoke(m: `2`, n: `1`, d0: T, d1: T) = Mat(Vec(d0), Vec(d1))
    @JvmName("mf2x2") operator fun <T> invoke(m: `2`, n: `2`, d0: T, d1: T, d2: T, d3: T) = Mat(Vec(d0, d1), Vec(d2, d3))
    @JvmName("mf2x3") operator fun <T> invoke(m: `2`, n: `3`, d0: T, d1: T, d2: T, d3: T, d4: T, d5: T) = Mat(Vec(d0, d1, d2), Vec(d3, d4, d5))
    @JvmName("mf3x1") operator fun <T> invoke(m: `3`, n: `1`, d0: T, d1: T, d2: T) = Mat(Vec(d0), Vec(d1), Vec(d2))
    @JvmName("mf3x2") operator fun <T> invoke(m: `3`, n: `2`, d0: T, d1: T, d2: T, d3: T, d4: T, d5: T) = Mat(Vec(d0, d1), Vec(d2, d3), Vec(d4, d5))
    @JvmName("mf3x3") operator fun <T> invoke(m: `3`, n: `3`, d0: T, d1: T, d2: T, d3: T, d4: T, d5: T, d6: T, d7: T, d8: T) = Mat(Vec(d0, d1, d2), Vec(d3, d4, d5), Vec(d6, d7, d8))
  }

  override fun toString() = "$contents"
}

// TODO: Implement semantic matrix multiplication
@JvmName("m1x1x1") operator fun <M: `1`, N: `1`, P: `1`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m1x1x2") operator fun <M: `1`, N: `1`, P: `2`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m1x1x3") operator fun <M: `1`, N: `1`, P: `3`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m1x2x1") operator fun <M: `1`, N: `2`, P: `1`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m1x2x2") operator fun <M: `1`, N: `2`, P: `2`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m1x2x3") operator fun <M: `1`, N: `2`, P: `3`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m1x3x1") operator fun <M: `1`, N: `3`, P: `1`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m1x3x2") operator fun <M: `1`, N: `3`, P: `2`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m1x3x3") operator fun <M: `1`, N: `3`, P: `3`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()

@JvmName("m2x1x1") operator fun <M: `2`, N: `1`, P: `1`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m2x1x2") operator fun <M: `2`, N: `1`, P: `2`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m2x1x3") operator fun <M: `2`, N: `1`, P: `3`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m2x2x1") operator fun <M: `2`, N: `2`, P: `1`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m2x2x2") operator fun <M: `2`, N: `2`, P: `2`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m2x2x3") operator fun <M: `2`, N: `2`, P: `3`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m2x3x1") operator fun <M: `2`, N: `3`, P: `1`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m2x3x2") operator fun <M: `2`, N: `3`, P: `2`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m2x3x3") operator fun <M: `2`, N: `3`, P: `3`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()

@JvmName("m3x1x1") operator fun <M: `3`, N: `1`, P: `1`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m3x1x2") operator fun <M: `3`, N: `1`, P: `2`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m3x1x3") operator fun <M: `3`, N: `1`, P: `3`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m3x2x1") operator fun <M: `3`, N: `2`, P: `1`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m3x2x2") operator fun <M: `3`, N: `2`, P: `2`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m3x2x3") operator fun <M: `3`, N: `2`, P: `3`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m3x3x1") operator fun <M: `3`, N: `3`, P: `1`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m3x3x2") operator fun <M: `3`, N: `3`, P: `2`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()
@JvmName("m3x3x3") operator fun <M: `3`, N: `3`, P: `3`> Mat<Double, M, N>.times(m: Mat<Double, N, P>): Mat<Double, M, P> = Mat()