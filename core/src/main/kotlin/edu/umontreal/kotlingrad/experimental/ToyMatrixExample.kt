@file:Suppress("DuplicatedCode", "LocalVariableName", "UNUSED_PARAMETER", "NonAsciiCharacters", "FunctionName", "PropertyName", "MemberVisibilityCanBePrivate")

package edu.umontreal.kotlingrad.experimental

fun main() {
  with(DoublePrecision) {
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
    val r = q(x to 1.0, y to 2.0)
    println("r: $r")

    val mf1 = Mat2x1(
      y * y,
      x * y)

    val mf2 = Mat1x2(vf2)

    val qr = mf2 * Vec(x, y)

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

open class MFun<X: SFun<X>, R: D1, C: D1>(override val inputs: Inputs<X>): Fun<X>, (Bindings<X>) -> MFun<X, R, C> {
  constructor(vararg funs: Fun<X>): this(Inputs(*funs))

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
      is MConst -> MZero()
      is Mat -> Mat(rows.map { it(bnds) as Vec<X, C> })
      else -> TODO(this::class.java.name)
    }

  // Materializes the concrete matrix from the dataflow graph
  fun coalesce(): Mat<X, R, C> = this(Bindings()) as Mat<X, R, C>

  open operator fun unaryMinus(): MFun<X, R, C> = MNegative(this)
  open operator fun plus(addend: MFun<X, R, C>): MFun<X, R, C> = MSum(this, addend)
  open operator fun times(multiplicand: SFun<X>): MFun<X, R, C> = MSProd(this, multiplicand)
  open operator fun times(multiplicand: VFun<X, C>): VFun<X, R> = MVProd(this, multiplicand)

  // The Hadamard product
  open infix fun ʘ(multiplicand: MFun<X, R, C>): MFun<X, R, C> = HProd(this, multiplicand)

  open operator fun <Q: D1> times(multiplicand: MFun<X, C, Q>): MFun<X, R, Q> = MMProd(this, multiplicand)

  override fun toString() = when (this) {
    is MNegative -> "-($value)"
    is MTranspose -> "($value).T"
    is MSum -> "$left + $right"
    is MMProd<X, R, *, C> -> "$left * $right"
    is HProd -> "$left ʘ $right"
    is MSProd -> "$left * $right"
    is SMProd -> "$left * $right"
    is MConst -> "${javaClass.name}()"
    is Mat -> "Mat${numRows}x$numCols(${rows.joinToString(", ") { it.contents.joinToString(", ") }})"
    is MDerivative -> "d($mFun) / d($v1)"
    else -> TODO(this::class.java.name)
  }
}

class MNegative<X: SFun<X>, R: D1, C: D1>(val value: MFun<X, R, C>): MFun<X, R, C>(value)
class MTranspose<X: SFun<X>, R: D1, C: D1>(val value: MFun<X, R, C>): MFun<X, C, R>(value)
class MSum<X: SFun<X>, R: D1, C: D1>(val left: MFun<X, R, C>, val right: MFun<X, R, C>): MFun<X, R, C>(left, right)
class MMProd<X: SFun<X>, R: D1, C1: D1, C2: D1>(val left: MFun<X, R, C1>, val right: MFun<X, C1, C2>): MFun<X, R, C2>(left, right)
class HProd<X: SFun<X>, R: D1, C: D1>(val left: MFun<X, R, C>, val right: MFun<X, R, C>): MFun<X, R, C>(left, right)
class MSProd<X: SFun<X>, R: D1, C: D1>(val left: MFun<X, R, C>, val right: SFun<X>): MFun<X, R, C>(left)
class SMProd<X: SFun<X>, R: D1, C: D1>(val left: SFun<X>, val right: MFun<X, R, C>): MFun<X, R, C>(right)

// TODO: Generalize tensor derivatives? https://en.wikipedia.org/wiki/Tensor_derivative_(continuum_mechanics)
class MDerivative<X: SFun<X>, R: D1, C: D1> internal constructor(val mFun: VFun<X, R>, numCols: Nat<C>, val v1: Var<X>): MFun<X, R, C>(mFun) {
  fun MFun<X, R, C>.df(): MFun<X, R, C> = when (this@df) {
    is MConst -> MZero()
    is MVar -> MZero()
    is MNegative -> -value.df()
    is MTranspose -> (value as MFun<X, R, C>).df().ᵀ as MFun<X, R, C>
    is MSum -> left.df() + right.df()
    // Casting here is necessary because of type erasure (we loose the inner dimension when MMProd<X, R, C1, C2> is boxed as MFun<X, R, C2>)
    is MMProd<X, R, *, C> -> (left as MFun<X, R, C>).df() * (right as MFun<X, C, C>) + left * ((right as MFun<X, R, C>).df() as MFun<X, C, C>)
    is MSProd -> left.df() * right + left * right.d(v1)
    is SMProd -> left.d(v1) * right + left * right.df()
    is HProd -> left.df() ʘ right + left ʘ right.df()
    is Mat -> Mat(rows.map { it.d(v1)() })
    else -> TODO(this@df::class.java.name)
  }
}

class MGradient<X : SFun<X>, R: D1, C: D1>(val fn: SFun<X>, val mVar: MVar<X, R, C>): MFun<X, R, C>(fn) {
  fun df() = fn.df()
  fun SFun<X>.df(): MFun<X, R, C> = when (this@df) {
    is MVar<*, *, *> -> if (this == mVar) MOne() else MZero()
    is Var -> MZero()
    is SConst -> MZero()
    is Sum -> left.df() + right.df()
    is Prod -> left.df() * right + left * right.df()
    is Power -> this * (exponent * Log(base)).df()
    is Negative -> -value.df()
    is Log -> (logarithmand pow -One<X>()) * logarithmand.df()
//    is Derivative -> fn.df()
    is DProd -> this().df()
    is VMagnitude -> this().df()
    else -> TODO(this@df::class.java.name)
  }
}

class MVar<X: SFun<X>, R: D1, C: D1>(override val name: String = ""): Variable, MFun<X, R, C>()
open class MConst<X: SFun<X>, R: D1, C: D1>: MFun<X, R, C>()

class MZero<X: SFun<X>, R: D1, C: D1>: MConst<X, R, C>()
class MOne<X: SFun<X>, R: D1, C: D1>: MConst<X, R, C>()

open class Mat<X: SFun<X>, R: D1, C: D1>(val rows: List<Vec<X, C>>): MFun<X, R, C>(*rows.toTypedArray()) {
  constructor(vararg rows: Vec<X, C>): this(rows.asList())

  val flatContents: List<SFun<X>> by lazy { rows.flatMap { it.contents } }

  val indices = rows.indices
  val cols by lazy { indices.map { i -> Vec<X, R>(rows.map { it[i] }) } }
  val numCols = rows.first().contents.size
  val numRows = rows.size

  init {
    rows.indices.zip(rows).filter { it.second.size != numCols }.run {
      require(isEmpty()) { "Declared $numCols cols but row(s) ${map { it.first }} contain(s) ${map { it.second }} values, respectively" }
    }
  }

  override val ᵀ: Mat<X, C, R> by lazy { Mat(cols) }

  override operator fun unaryMinus(): Mat<X, R, C> = Mat(rows.map { -it })

  override operator fun plus(addend: MFun<X, R, C>): MFun<X, R, C> =
    when (addend) {
      is Mat -> Mat(rows.mapIndexed { i, r -> (r + addend[i]) as Vec<X, C> })
      else -> super.plus(addend)
    }

  operator fun get(i: Int): VFun<X, C> = rows[i]

  override operator fun times(multiplicand: SFun<X>): Mat<X, R, C> = Mat(rows.map { it * multiplicand })

  override operator fun times(multiplicand: VFun<X, C>): VFun<X, R> =
    when (multiplicand) {
      is Vec -> Vec(rows.map { r -> r dot multiplicand })
      else -> super.times(multiplicand)
    }

  override operator fun <Q: D1> times(multiplicand: MFun<X, C, Q>): MFun<X, R, Q> =
    when (multiplicand) {
      is Mat -> Mat(indices.map { i ->
        Vec(multiplicand.cols.indices.map { j ->
          rows[i] dot multiplicand.cols[j]
        })
      })
      else -> super.times(multiplicand)
    }
}

fun <X: SFun<X>> Mat1x1(v0: Vec<X, D1>): Mat<X, D1, D1> = Mat(v0)
fun <X: SFun<X>> Mat2x1(v0: Vec<X, D1>, v1: Vec<X, D1>): Mat<X, D2, D1> = Mat(v0, v1)
fun <X: SFun<X>> Mat3x1(v0: Vec<X, D1>, v1: Vec<X, D1>, v2: Vec<X, D1>): Mat<X, D3, D1> = Mat(v0, v1, v2)
fun <X: SFun<X>> Mat1x2(v0: Vec<X, D2>): Mat<X, D1, D2> = Mat(v0)
fun <X: SFun<X>> Mat2x2(v0: Vec<X, D2>, v1: Vec<X, D2>): Mat<X, D2, D2> = Mat(v0, v1)
fun <X: SFun<X>> Mat3x2(v0: Vec<X, D2>, v1: Vec<X, D2>, v2: Vec<X, D2>): Mat<X, D3, D2> = Mat(v0, v1, v2)
fun <X: SFun<X>> Mat1x3(v0: Vec<X, D3>): Mat<X, D1, D3> = Mat(v0)
fun <X: SFun<X>> Mat2x3(v0: Vec<X, D3>, v1: Vec<X, D3>): Mat<X, D2, D3> = Mat(v0, v1)
fun <X: SFun<X>> Mat3x3(v0: Vec<X, D3>, v1: Vec<X, D3>, v2: Vec<X, D3>): Mat<X, D3, D3> = Mat(v0, v1, v2)

fun <X: SFun<X>> Mat1x1(d0: SFun<X>): Mat<X, D1, D1> = Mat(Vec(d0))
fun <X: SFun<X>> Mat1x2(d0: SFun<X>, d1: SFun<X>): Mat<X, D1, D2> = Mat(Vec(d0, d1))
fun <X: SFun<X>> Mat1x3(d0: SFun<X>, d1: SFun<X>, d2: SFun<X>): Mat<X, D1, D3> = Mat(Vec(d0, d1, d2))
fun <X: SFun<X>> Mat2x1(d0: SFun<X>, d1: SFun<X>): Mat<X, D2, D1> = Mat(Vec(d0), Vec(d1))
fun <X: SFun<X>> Mat2x2(d0: SFun<X>, d1: SFun<X>, d2: SFun<X>, d3: SFun<X>): Mat<X, D2, D2> = Mat(Vec(d0, d1), Vec(d2, d3))
fun <X: SFun<X>> Mat2x3(d0: SFun<X>, d1: SFun<X>, d2: SFun<X>, d3: SFun<X>, d4: SFun<X>, d5: SFun<X>): Mat<X, D2, D3> = Mat(Vec(d0, d1, d2), Vec(d3, d4, d5))
fun <X: SFun<X>> Mat3x1(d0: SFun<X>, d1: SFun<X>, d2: SFun<X>): Mat<X, D3, D1> = Mat(Vec(d0), Vec(d1), Vec(d2))
fun <X: SFun<X>> Mat3x2(d0: SFun<X>, d1: SFun<X>, d2: SFun<X>, d3: SFun<X>, d4: SFun<X>, d5: SFun<X>): Mat<X, D3, D2> = Mat(Vec(d0, d1), Vec(d2, d3), Vec(d4, d5))
fun <X: SFun<X>> Mat3x3(d0: SFun<X>, d1: SFun<X>, d2: SFun<X>, d3: SFun<X>, d4: SFun<X>, d5: SFun<X>, d6: SFun<X>, d7: SFun<X>, d8: SFun<X>): Mat<X, D3, D3> = Mat(Vec(d0, d1, d2), Vec(d3, d4, d5), Vec(d6, d7, d8))