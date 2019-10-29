@file:Suppress("DuplicatedCode", "LocalVariableName", "UNUSED_PARAMETER")

package edu.umontreal.kotlingrad.samples

fun main() {
  with(DoublePrecision) {
    val x = vrb("x")
    val y = vrb("y")

    val f = x pow 2
    println(f(x to 3.0))
    println("f(x) = $f")
    val df_dx = f.df(x)
    println("f'(x) = $df_dx")

    val g = x pow x
    println("g(x) = $g")
    val dg_dx = g.df(x)
    println("g'(x) = $dg_dx")

    val h = x + y
    println("h(x) = $h")
    val dh_dx = h.df(x)
    println("h'(x) = $dh_dx")

    val vf1 = Vec(y + x, y * 2)
    val bh = x * vf1
    val vf2 = Vec(x, y)
    val q = vf1 + vf2
    val z = q(x to 1.0, y to 2.0)
    println("z: $z")

    val mf1 = Mat2x1(
      y * y,
      x * y)
    val mf2 = Mat1x2(vf2)
    val mf3 = Mat3x2(x, x,
      y, x,
      x, x)
    val mf4 = Mat2x2(vf2, vf2)

    println(mf1 * mf2) // 2*1 x 1*2
//    println(mf1 * vf1) // 2*1 x 2
    println(mf2 * vf1) // 1*2 x 2
    println(mf3 * vf1) // 3*2 x 2
//    println(mf3 * mf3) // 3*2 x 3*2
  }
}

/**
 * Matrix function.
 */

open class MFun<X: Fun<X>, R: `1`, C: `1`>(
  open val numRows: Nat<R>,
  open val numCols: Nat<C>,
  open val sVars: Set<Var<X>> = emptySet()
//open val vVars: Set<VVar<X, *>> = emptySet(),
//open val mVars: Set<MVar<X, *, *>> = emptySet()
) {
  constructor(left: MFun<X, R, *>, right: MFun<X, *, C>): this(left.numRows, right.numCols, left.sVars + right.sVars) //, left.vVars + right.vVars, left.mVars + right.mVars)
  constructor(mFun: MFun<X, R, C>): this(mFun.numRows, mFun.numCols, mFun.sVars) //, mFun.vVars, mFun.mVars)

  open val T: MFun<X, C, R> by lazy { MTranspose(this) }

  operator fun invoke(sMap: Map<Var<X>, SConst<X>> = emptyMap()): MFun<X, R, C> =
//                    vMap: Map<VVar<X, *>, Vec<X, *>> = emptyMap(),
//                    mMap: Map<MVar<X, *, *>, MConst<X, *, *>> = emptyMap()): MFun<X, R, C> =
    when (this) {
      else -> TODO()
    }

  open operator fun unaryMinus(): MFun<X, R, C> = MNegative(this)

  open operator fun plus(addend: MFun<X, R, C>): MFun<X, R, C> = MSum(this, addend)

  open operator fun times(multiplicand: Fun<X>): MFun<X, R, C> = MSProd(this, multiplicand)

  open operator fun times(multiplicand: VFun<X, C>): VFun<X, R> = MVProd(this, multiplicand)

  open operator fun <Q: `1`> times(multiplicand: MFun<X, C, Q>): MFun<X, R, Q> = MMProd(this, multiplicand)
}

class MNegative<X: Fun<X>, R: `1`, C: `1`>(val value: MFun<X, R, C>): MFun<X, R, C>(value)
class MTranspose<X: Fun<X>, R: `1`, C: `1`>(val value: MFun<X, R, C>): MFun<X, C, R>(value.numCols, value.numRows, value.sVars)
class MSum<X: Fun<X>, R: `1`, C: `1`>(val left: MFun<X, R, C>, val right: MFun<X, R, C>): MFun<X, R, C>(left, right)
class MMProd<X: Fun<X>, R: `1`, C1: `1`, C2: `1`>(val left: MFun<X, R, C1>, val right: MFun<X, C1, C2>): MFun<X, R, C2>(left, right)
class MSProd<X: Fun<X>, R: `1`, C: `1`>(val left: MFun<X, R, C>, val right: Fun<X>): MFun<X, R, C>(left)
class SMProd<X: Fun<X>, R: `1`, C: `1`>(val left: Fun<X>, val right: MFun<X, R, C>): MFun<X, R, C>(right)

//class MVar<X: Fun<X>, R: `1`, C: `1`>(override val name: String, numRows: Nat<R>, numCols: Nat<C>):
//  Variable, MFun<X, R, C>(numRows, numCols) { override val mVars: Set<MVar<X, *, *>> = setOf(this) }
open class MConst<X: Fun<X>, R: `1`, C: `1`>(numRows: Nat<R>, numCols: Nat<C>): MFun<X, R, C>(numRows, numCols)

open class Mat<X: Fun<X>, R: `1`, C: `1`>(override val numRows: Nat<R>,
                                     override val numCols: Nat<C>,
                                     override val sVars: Set<Var<X>> = emptySet(),
//                                   override val vVars: Set<VVar<X, *>> = emptySet(),
//                                   override val mVars: Set<MVar<X, *>> = emptySet(),
                                     vararg val rows: Vec<X, C>): MFun<X, R, C>(numRows, numCols) {
  constructor(numRows: Nat<R>, numCols: Nat<C>, vararg rows: Vec<X, C>): this(numRows, numCols, rows.flatMap { it.sVars }.toSet(), *rows)
  constructor(numRows: Nat<R>, numCols: Nat<C>, contents: List<Vec<X, C>>): this(numRows, numCols, contents.flatMap { it.sVars }.toSet(), *contents.toTypedArray())

  init {
    require(numRows.i == rows.size) { "Declared rows, $numRows != ${rows.size}" }
  }

  val cols: Array<VFun<X, R>> by lazy { (0 until numCols.i).map { i -> Vec(numRows, rows.map { it[i] }) }.toTypedArray() }

  override operator fun unaryMinus() = Mat(numRows, numCols, rows.map { -it })

  override operator fun plus(addend: MFun<X, R, C>) =
    when(addend) {
      is Mat -> Mat(numRows, numCols, rows.mapIndexed { i, r -> (r + addend[i]) as Vec<X, C> })
      else -> super.plus(addend)
    }

  operator fun get(i: Int): VFun<X, C> = rows[i]

  override operator fun times(multiplicand: Fun<X>): MFun<X, R, C> = Mat(numRows, numCols, rows.map { it * multiplicand })

  override operator fun times(multiplicand: VFun<X, C>): VFun<X, R> =
    when(multiplicand) {
      is Vec -> Vec(numRows, rows.map { r -> r dot multiplicand })
      else -> super.times(multiplicand)
    }

  override operator fun <Q: `1`> times(multiplicand: MFun<X, C, Q>): MFun<X, R, Q> =
    when(multiplicand) {
      is Mat -> Mat(numRows, multiplicand.numCols, (0 until numRows.i).map { i ->
          Vec(multiplicand.numCols, (0 until multiplicand.numCols.i).map { j ->
            rows[i] dot multiplicand.cols[j]
          })
        })
      else -> super.times(multiplicand)
    }

  override fun toString() = "($numRows x $numCols)\n[${rows.joinToString("\n ") { it.contents.joinToString(", ") }}]"
}

class Mat1x1<X: Fun<X>>(v0: Vec<X, `1`>): Mat<X, `1`, `1`>(`1`, `1`, v0) { constructor(f0: Fun<X>): this(Vec(f0)) }
class Mat2x1<X: Fun<X>>(v0: Vec<X, `1`>, v1: Vec<X, `1`>): Mat<X, `2`, `1`>(`2`, `1`, v0, v1) { constructor(f0: Fun<X>, f1: Fun<X>): this(Vec(f0), Vec(f1)) }
class Mat3x1<X: Fun<X>>(v0: Vec<X, `1`>, v1: Vec<X, `1`>, v2: Vec<X, `1`>): Mat<X, `3`, `1`>(`3`, `1`, v0, v1, v2) { constructor(f0: Fun<X>, f1: Fun<X>, f2: Fun<X>): this(Vec(f0), Vec(f1), Vec(f2)) }

class Mat1x2<X: Fun<X>>(v0: Vec<X, `2`>): Mat<X, `1`, `2`>(`1`, `2`, v0) { constructor(f0: Fun<X>, f1: Fun<X>): this(Vec(f0, f1)) }
class Mat2x2<X: Fun<X>>(v0: Vec<X, `2`>, v1: Vec<X, `2`>): Mat<X, `2`, `2`>(`2`, `2`, v0, v1) { constructor(f0: Fun<X>, f1: Fun<X>, f2: Fun<X>, f3: Fun<X>): this(Vec(f0, f1), Vec(f2, f3)) }
class Mat3x2<X: Fun<X>>(v0: Vec<X, `2`>, v1: Vec<X, `2`>, v2: Vec<X, `2`>): Mat<X, `3`, `2`>(`3`, `2`, v0, v1, v2) { constructor(f0: Fun<X>, f1: Fun<X>, f2: Fun<X>, f3: Fun<X>, f4: Fun<X>, f5: Fun<X>): this(Vec(f0, f1), Vec(f2, f3), Vec(f4, f5)) }

class Mat1x3<X: Fun<X>>(v0: Vec<X, `3`>): Mat<X, `1`, `3`>(`1`, `3`, v0) { constructor(f0: Fun<X>, f1: Fun<X>, f2: Fun<X>, f3: Fun<X>): this(Vec(f0, f1, f3)) }
class Mat2x3<X: Fun<X>>(v0: Vec<X, `3`>, v1: Vec<X, `3`>): Mat<X, `2`, `3`>(`2`, `3`, v0, v1) { constructor(f0: Fun<X>, f1: Fun<X>, f2: Fun<X>, f3: Fun<X>, f4: Fun<X>, f5: Fun<X>): this(Vec(f0, f1, f2), Vec(f3, f4, f5)) }
class Mat3x3<X: Fun<X>>(v0: Vec<X, `3`>, v1: Vec<X, `3`>, v2: Vec<X, `3`>): Mat<X, `3`, `3`>(`3`, `3`, v0, v1, v2) { constructor(f0: Fun<X>, f1: Fun<X>, f2: Fun<X>, f3: Fun<X>, f4: Fun<X>, f5: Fun<X>, f6: Fun<X>, f7: Fun<X>, f8: Fun<X>): this(Vec(f0, f1, f2), Vec(f3, f4, f5), Vec(f6, f7, f8)) }