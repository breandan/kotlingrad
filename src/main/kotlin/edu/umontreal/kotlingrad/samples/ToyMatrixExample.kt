@file:Suppress("DuplicatedCode", "LocalVariableName", "UNUSED_PARAMETER", "NonAsciiCharacters", "FunctionName")

package edu.umontreal.kotlingrad.samples

fun main() {
  with(DoublePrecision) {
    val x = vrb("x")
    val y = vrb("y")

    val f = x pow 2
    println(f(x to 3.0))
    println("f(x) = $f")
    val df_dx = f.d(x)
    println("f'(x) = $df_dx")

    val g = x pow x
    println("g(x) = $g")
    val dg_dx = g.d(x)
    println("g'(x) = $dg_dx")

    val h = x + y
    println("h(x) = $h")
    val dh_dx = h.d(x)
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
    val mf5 = Mat2x2(
      y * y, x * x,
      x * y, y * y)
    val mf6 = mf4 * mf5 * mf1

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

open class MFun<X: Fun<X>, R: D1, C: D1>(
  open val numRows: Nat<R>,
  open val numCols: Nat<C>,
  open val sVars: Set<Var<X>> = emptySet()
//open val vVars: Set<VVar<X, *>> = emptySet(),
//open val mVars: Set<MVar<X, *, *>> = emptySet()
): (Bindings<X>) -> MFun<X, R, C> {
  constructor(left: MFun<X, R, *>, right: MFun<X, *, C>): this(left.numRows, right.numCols, left.sVars + right.sVars) //, left.vVars + right.vVars, left.mVars + right.mVars)
  constructor(mFun: MFun<X, R, C>): this(mFun.numRows, mFun.numCols, mFun.sVars) //, mFun.vVars, mFun.mVars)

  open val ᵀ: MFun<X, C, R> by lazy { MTranspose(this) }

  override operator fun invoke(bnds: Bindings<X>): MFun<X, R, C> =
    when (this) {
      is MNegative -> -value(bnds)
      is MTranspose -> value(bnds).ᵀ
      is MSum -> left(bnds) + right(bnds)
      is MMProd<X, R, *, C> -> left(bnds) as MFun<X, R, D1> * right(bnds) as MFun<X, D1, C>
      is HProd -> left(bnds) ʘ right(bnds)
      is MSProd -> left(bnds) * right(bnds)
      is SMProd -> left(bnds) * right(bnds)
      is MConst -> MZero(numRows, numCols)
      is Mat -> Mat(numRows, numCols, rows.map { it(bnds) as Vec<X, C> })
      else -> throw IllegalArgumentException("Type ${this::class.java.name} unknown")
    }

// Materializes the concrete matrix from the dataflow graph
  fun coalesce(): Mat<X, R, C> = this(Bindings()) as Mat<X, R, C>

  open operator fun unaryMinus(): MFun<X, R, C> = MNegative(this)
  open operator fun plus(addend: MFun<X, R, C>): MFun<X, R, C> = MSum(this, addend)
  open operator fun times(multiplicand: Fun<X>): MFun<X, R, C> = MSProd(this, multiplicand)
  open operator fun times(multiplicand: VFun<X, C>): VFun<X, R> = MVProd(this, multiplicand)

  // The Hadamard product
  open infix fun ʘ(multiplicand: MFun<X, R, C>): MFun<X, R, C> = HProd(this, multiplicand)
  open operator fun <Q: D1> times(multiplicand: MFun<X, C, Q>): MFun<X, R, Q> = MMProd(this, multiplicand)

  override fun toString() = when(this) {
    is MNegative -> "-($value)"
    is MTranspose -> "($value).T"
    is MSum -> "$left + $right"
    is MMProd<X, R, *, C> -> "$left * $right"
    is HProd -> "$left ʘ $right"
    is MSProd -> "$left * $right"
    is SMProd -> "$left * $right"
    is MConst -> "TODO()"
    is Mat -> "Mat${numRows}x$numCols(${rows.joinToString(", ") { it.contents.joinToString(", ") }})"
    is MDerivative -> "d($mfn) / d($v1)"
    else -> throw IllegalArgumentException("Type ${this::class.java.name} unknown")
  }
}

class MNegative<X: Fun<X>, R: D1, C: D1>(val value: MFun<X, R, C>): MFun<X, R, C>(value)
class MTranspose<X: Fun<X>, R: D1, C: D1>(val value: MFun<X, R, C>): MFun<X, C, R>(value.numCols, value.numRows, value.sVars)
class MSum<X: Fun<X>, R: D1, C: D1>(val left: MFun<X, R, C>, val right: MFun<X, R, C>): MFun<X, R, C>(left, right)
class MMProd<X: Fun<X>, R: D1, C1: D1, C2: D1>(val left: MFun<X, R, C1>, val right: MFun<X, C1, C2>): MFun<X, R, C2>(left, right)
class HProd<X: Fun<X>, R: D1, C: D1>(val left: MFun<X, R, C>, val right: MFun<X, R, C>): MFun<X, R, C>(left, right)
class MSProd<X: Fun<X>, R: D1, C: D1>(val left: MFun<X, R, C>, val right: Fun<X>): MFun<X, R, C>(left)
class SMProd<X: Fun<X>, R: D1, C: D1>(val left: Fun<X>, val right: MFun<X, R, C>): MFun<X, R, C>(right)

// TODO: Generalize tensor derivatives
class MDerivative<X : Fun<X>, R: D1, C: D1> internal constructor(val mfn: VFun<X, R>, numCols: Nat<C>, val v1: Var<X>) : MFun<X, R, C>(mfn.numRows, numCols, mfn.sVars) {
  fun MFun<X, R, C>.df(): MFun<X, R, C> = when (this) {
    is MNegative -> -value.df()
    is MTranspose -> (value as MFun<X, R, C>).df().ᵀ as MFun<X, R, C>
    is MSum -> left.df() + right.df()
    // Casting here is necessary because of type erasure (we loose the inner dimension when MMProd<X, R, C1, C2> is boxed as MFun<X, R, C2>)
    is MMProd<X, R, *, C> -> (left as MFun<X, R, C>).df() * (right as MFun<X, C, C>) + left * ((right as MFun<X, R, C>).df() as MFun<X, C, C>)
    is MSProd -> left.df() * right + left * right.d(v1)
    is SMProd -> left.d(v1) * right + left * right.df()
    is HProd -> left.df() ʘ right + left ʘ right.df()
    is Mat -> Mat(numRows, numCols, rows.map { it.d(v1)() })
    is MConst -> MZero(numRows, numCols)
    else -> throw IllegalArgumentException("Unable to differentiate expression of type ${this::class.java.name}")
  }
}

//class MVar<X: Fun<X>, R: D1, C: D1>(override val name: String, numRows: Nat<R>, numCols: Nat<C>):
//  Variable, MFun<X, R, C>(numRows, numCols) { override val mVars: Set<MVar<X, *, *>> = setOf(this) }
open class MConst<X: Fun<X>, R: D1, C: D1>(numRows: Nat<R>, numCols: Nat<C>): MFun<X, R, C>(numRows, numCols)
class MZero<X: Fun<X>, R: D1, C: D1>(rows: Nat<R>, cols: Nat<C>): MConst<X, R, C>(rows, cols)
class MOne<X: Fun<X>, R: D1, C: D1>(rows: Nat<R>, cols: Nat<C>): MConst<X, R, C>(rows, cols)

open class Mat<X: Fun<X>, R: D1, C: D1>(final override val numRows: Nat<R>,
                                          final override val numCols: Nat<C>,
                                          override val sVars: Set<Var<X>> = emptySet(),
//                                   override val vVars: Set<VVar<X, *>> = emptySet(),
//                                   override val mVars: Set<MVar<X, *>> = emptySet(),
                                          vararg val rows: Vec<X, C>): MFun<X, R, C>(numRows, numCols) {
  constructor(numRows: Nat<R>, numCols: Nat<C>, vararg rows: Vec<X, C>): this(numRows, numCols, rows.flatMap { it.sVars }.toSet(), *rows)
  constructor(numRows: Nat<R>, numCols: Nat<C>, contents: List<Vec<X, C>>): this(numRows, numCols, contents.flatMap { it.sVars }.toSet(), *contents.toTypedArray())

  init {
    require(numRows.i == rows.size) { "Declared rows, $numRows != ${rows.size}" }
  }

  override val ᵀ: Mat<X, C, R> by lazy { Mat(numCols, numRows, *(0 until numCols.i).map { i -> Vec(numRows, rows.map { it[i] }) }.toTypedArray()) }

  override operator fun unaryMinus() = Mat(numRows, numCols, rows.map { -it })

  override operator fun plus(addend: MFun<X, R, C>) =
    when (addend) {
      is Mat -> Mat(numRows, numCols, rows.mapIndexed { i, r -> (r + addend[i]) as Vec<X, C> })
      else -> super.plus(addend)
    }

  operator fun get(i: Int): VFun<X, C> = rows[i]

  override operator fun times(multiplicand: Fun<X>): Mat<X, R, C> = Mat(numRows, numCols, rows.map { it * multiplicand })

  override operator fun times(multiplicand: VFun<X, C>) =
    when (multiplicand) {
      is Vec -> Vec(numRows, rows.map { r -> r dot multiplicand })
      else -> super.times(multiplicand)
    }

  override operator fun <Q: D1> times(multiplicand: MFun<X, C, Q>) =
    when (multiplicand) {
      is Mat -> Mat(numRows, multiplicand.numCols, (0 until numRows.i).map { i ->
        Vec(multiplicand.numCols, (0 until multiplicand.numCols.i).map { j ->
          rows[i] dot multiplicand.ᵀ[j]
        })
      })
      else -> super.times(multiplicand)
    }
}

class Mat1x1<X: Fun<X>>(v0: Vec<X, D1>): Mat<X, D1, D1>(D1, D1, v0) { constructor(f0: Fun<X>): this(Vec(f0)) }
class Mat2x1<X: Fun<X>>(v0: Vec<X, D1>, v1: Vec<X, D1>): Mat<X, D2, D1>(D2, D1, v0, v1) { constructor(f0: Fun<X>, f1: Fun<X>): this(Vec(f0), Vec(f1)) }
class Mat3x1<X: Fun<X>>(v0: Vec<X, D1>, v1: Vec<X, D1>, v2: Vec<X, D1>): Mat<X, D3, D1>(D3, D1, v0, v1, v2) { constructor(f0: Fun<X>, f1: Fun<X>, f2: Fun<X>): this(Vec(f0), Vec(f1), Vec(f2)) }

class Mat1x2<X: Fun<X>>(v0: Vec<X, D2>): Mat<X, D1, D2>(D1, D2, v0) { constructor(f0: Fun<X>, f1: Fun<X>): this(Vec(f0, f1)) }
class Mat2x2<X: Fun<X>>(v0: Vec<X, D2>, v1: Vec<X, D2>): Mat<X, D2, D2>(D2, D2, v0, v1) { constructor(f0: Fun<X>, f1: Fun<X>, f2: Fun<X>, f3: Fun<X>): this(Vec(f0, f1), Vec(f2, f3)) }
class Mat3x2<X: Fun<X>>(v0: Vec<X, D2>, v1: Vec<X, D2>, v2: Vec<X, D2>): Mat<X, D3, D2>(D3, D2, v0, v1, v2) { constructor(f0: Fun<X>, f1: Fun<X>, f2: Fun<X>, f3: Fun<X>, f4: Fun<X>, f5: Fun<X>): this(Vec(f0, f1), Vec(f2, f3), Vec(f4, f5)) }

class Mat1x3<X: Fun<X>>(v0: Vec<X, D3>): Mat<X, D1, D3>(D1, D3, v0) { constructor(f0: Fun<X>, f1: Fun<X>, f2: Fun<X>, f3: Fun<X>): this(Vec(f0, f1, f3)) }
class Mat2x3<X: Fun<X>>(v0: Vec<X, D3>, v1: Vec<X, D3>): Mat<X, D2, D3>(D2, D3, v0, v1) { constructor(f0: Fun<X>, f1: Fun<X>, f2: Fun<X>, f3: Fun<X>, f4: Fun<X>, f5: Fun<X>): this(Vec(f0, f1, f2), Vec(f3, f4, f5)) }
class Mat3x3<X: Fun<X>>(v0: Vec<X, D3>, v1: Vec<X, D3>, v2: Vec<X, D3>): Mat<X, D3, D3>(D3, D3, v0, v1, v2) { constructor(f0: Fun<X>, f1: Fun<X>, f2: Fun<X>, f3: Fun<X>, f4: Fun<X>, f5: Fun<X>, f6: Fun<X>, f7: Fun<X>, f8: Fun<X>): this(Vec(f0, f1, f2), Vec(f3, f4, f5), Vec(f6, f7, f8)) }