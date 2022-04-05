@file:Suppress("DuplicatedCode", "LocalVariableName", "UNUSED_PARAMETER", "NonAsciiCharacters", "FunctionName", "PropertyName", "MemberVisibilityCanBePrivate", "UNUSED_VARIABLE")

package ai.hypergraph.kotlingrad.api

import ai.hypergraph.kaliningraph.graphs.*
import ai.hypergraph.kaliningraph.tensor.*
import ai.hypergraph.kotlingrad.EAGER
import ai.hypergraph.kotlingrad.api.VFun.Companion.KG_IT
import ai.hypergraph.kotlingrad.shapes.D1
import ai.hypergraph.kotlingrad.shapes.DN
import ai.hypergraph.kotlingrad.shapes.Nat
import kotlin.reflect.KProperty

/**
 * Matrix function.
 */

open class MFun<X, R, C>(override vararg val inputs: Fun<X>): Fun<X>
  where X: SFun<X>, R: D1, C: D1 {
  open val ᵀ: MFun<X, C, R> by lazy { MTranspose(this) }

  override fun invoke(newBindings: Bindings<X>): MFun<X, R, C> =
    MComposition(this, newBindings)
      .run { if (bindings.complete || newBindings.readyToBind || EAGER) evaluate else this }

  // Materializes the concrete matrix from the dataflow graph
  override operator fun invoke(): Mat<X, R, C> =
    MComposition(this).evaluate.let {
      try {
        it as Mat<X, R, C>
      } catch (e: ClassCastException) {
        //show("before"); it.show("after")
        throw NumberFormatException("Matrix function has unbound free variables: ${bindings.allFreeVariables.keys}")
      }
    }

  override operator fun invoke(vararg numbers: Number): MFun<X, R, C> =
    invoke(bindings.zip(numbers.map { wrap(it) }))

  override operator fun invoke(vararg funs: Fun<X>): MFun<X, R, C> =
    invoke(bindings.zip(funs.toList()))

  override operator fun invoke(vararg ps: FunToAny<X>): MFun<X, R, C> =
    invoke(ps.toList().bind())

  val mapInput = SVar<X>(KG_IT)
  open fun map(ef: (SFun<X>) -> SFun<X>): MFun<X, R, C> = MMap(this, ef(mapInput), mapInput)

  open fun d(sVar: SVar<X>): MFun<X, R, C> = MDerivative(this, sVar).let { if (EAGER) it.df() else it }

  open operator fun unaryMinus(): MFun<X, R, C> = MNegative(this)
  open operator fun plus(addend: MFun<X, R, C>): MFun<X, R, C> = MSum(this, addend)
  open operator fun minus(addend: MFun<X, R, C>): MFun<X, R, C> = MSum(this, -addend)
  open operator fun times(multiplicand: SFun<X>): MFun<X, R, C> = MSProd(this, multiplicand)
  open operator fun times(multiplicand: VFun<X, C>): VFun<X, R> = MVProd(this, multiplicand)
  open infix fun ʘ(multiplicand: MFun<X, R, C>): MFun<X, R, C> = HProd(this, multiplicand)
  open operator fun <Q: D1> times(multiplicand: MFun<X, C, Q>): MFun<X, R, Q> = MMProd(this, multiplicand)

  operator fun times(multiplicand: Number): MFun<X, R, C> = this * wrap(multiplicand)

  open fun sum(): VFun<X, C> = MSumRows(this)

  override fun toString() = when (this) {
    is MNegative -> "-($input)"
    is MTranspose -> "($input).T"
    is MConst -> "${this::class.simpleName}($numRows x $numCols)"
    is Mat -> "Mat(${rows.joinToString()})"
    is MDerivative<X, *, *> -> "d($input) / d($vrb)"
    is MVar -> "$name: Var${r}x${c}"
    is MComposition -> "MComp($input)$bindings"
    is Jacobian -> "Jacobian($vrb)$bindings"
    else -> asString()
  }

  override val op: Op = when (this) {
    is MNegative -> Ops.sub
    is MTranspose -> Ops.transpose
    is HProd<X, *, *> -> Ops.odot
    is MMProd<X, *, *, *> -> Ops.prod
    is MSum<X, *, *> -> Ops.sum
    is SMProd<X, *, *> -> Ops.dot
    is MComposition<X, *, *> -> Ops.λ
    is VVMap<X, *, *> -> Ops.map
    is MMap<X, *, *> -> Ops.map
    else -> Ops.id
  }
}

class MMap<X: SFun<X>, R: D1, C: D1>(
  val input: MFun<X, R, C>,
  val ssMap: SFun<X>,
  placeholder: SVar<X>
): MFun<X, R, C>(input, ssMap), PolyFun<X> {
  override val bindings: Bindings<X> = input.bindings + ssMap.bindings - placeholder
  override val inputs: Array<out Fun<X>> = arrayOf(input, ssMap)
}

class MNegative<X: SFun<X>, R: D1, C: D1>(override val input: MFun<X, R, C>): MFun<X, R, C>(input), UnFun<X>
class MTranspose<X: SFun<X>, R: D1, C: D1>(override val input: MFun<X, R, C>): MFun<X, C, R>(input), UnFun<X>
class MSum<X: SFun<X>, R: D1, C: D1>(override val left: MFun<X, R, C>, override val right: MFun<X, R, C>): MFun<X, R, C>(left, right), BiFun<X>
class MMProd<X: SFun<X>, R: D1, C1: D1, C2: D1>(override val left: MFun<X, R, C1>, override val right: MFun<X, C1, C2>): MFun<X, R, C2>(left, right), BiFun<X>
class HProd<X: SFun<X>, R: D1, C: D1>(override val left: MFun<X, R, C>, override val right: MFun<X, R, C>): MFun<X, R, C>(left, right), BiFun<X>
class MSProd<X: SFun<X>, R: D1, C: D1>(override val left: MFun<X, R, C>, override val right: SFun<X>): MFun<X, R, C>(left, right), BiFun<X>
class SMProd<X: SFun<X>, R: D1, C: D1>(override val left: SFun<X>, override val right: MFun<X, R, C>): MFun<X, R, C>(right, right), BiFun<X>

class MComposition<X: SFun<X>, R: D1, C: D1>(
  override val input: MFun<X, R, C>,
  arguments: Bindings<X> = Bindings(input)
): MFun<X, R, C>(input), UnFun<X> {
  override val bindings: Bindings<X> = input.bindings + arguments
  val evaluate: MFun<X, R, C> by lazy { bind(bindings) }

  @Suppress("UNCHECKED_CAST")
  fun MFun<X, R, C>.bind(bnds: Bindings<X>): MFun<X, R, C> =
    bnds[this@bind] ?: when (this@bind) {
      is MConst -> this@bind
      is MVar -> vMat
      is Mat -> Mat(rows.map { it(bnds) })
      is MNegative -> -input.bind(bnds)
      is MTranspose -> input.ᵀ.bind(bnds)
      is MSum -> left.bind(bnds) + right.bind(bnds)
      is MMProd<X, R, *, C> -> left(bnds) as MFun<X, R, DN> * right(bnds) as MFun<X, DN, C>
      is HProd -> left.bind(bnds) ʘ right.bind(bnds)
      is MSProd -> left.bind(bnds) * right(bnds)
      is SMProd -> left(bnds) * right.bind(bnds)
      is MDerivative -> df().bind(bnds)
      is MGradient<X, R, C> -> df().bind(bnds)
      is MComposition -> input.bind(bnds)
      is MMap<X, R, C> -> input.bind(bnds).map { ssMap(mapInput to it)(bnds) }
      is Jacobian<X, R, C> -> df().bind(bnds)
      is VVMap -> input(bnds).vMap { svMap(mapInput to it)(bnds) }
      else -> TODO(this@bind.toString() + "/" + bnds)
    }.also { result -> bnds.checkForUnpropagatedVariables(this@bind, result) }
}

// TODO: Generalize tensor derivatives? https://en.wikipedia.org/wiki/Tensor_derivative_(continuum_mechanics)
@Suppress("UNCHECKED_CAST")
class MDerivative<X: SFun<X>, R: D1, C: D1>(override val input: MFun<X, R, C>, override val vrb: SVar<X>): MFun<X, R, C>(input, vrb), Grad<X> {
  fun df() = input.df()
  fun MFun<X, R, C>.df(): MFun<X, R, C> = when (this@df) {
    is MVar -> map { it.d(vrb) }
    is Mat -> map { it.d(vrb) }
    is MConst -> map { Zero() }
    is MNegative -> -input.df()
    is MTranspose -> (input as MFun<X, R, C>).df().ᵀ as MFun<X, R, C>
    is MSum -> left.df() + right.df()
    // Casting here is necessary because of type erasure (we loose the inner dimension when MMProd<X, R, C1, C2> is boxed as MFun<X, R, C2>)
    is MMProd<X, R, *, C> ->
      (left as MFun<X, R, C>).df() * (right as MFun<X, C, C>) +
        left * ((right as MFun<X, R, C>).df() as MFun<X, C, C>)
    is MSProd -> left.df() * right + left * right.d(vrb)
    is SMProd -> left.d(vrb) * right + left * right.df()
    is HProd -> left.df() ʘ right + left ʘ right.df()
    is MMap -> input.df().map { it * ssMap.d(vrb) } // Chain rule
    is VVMap -> input.d(vrb).vMap { it * svMap(mapInput to it).d(vrb) }
    is MDerivative -> input.df()
    is MComposition -> evaluate.df()
    else -> TODO(this@df::class.simpleName!!)
  }
}

class MGradient<X: SFun<X>, R: D1, C: D1>(override val input: SFun<X>, override val vrb: MVar<X, R, C>): MFun<X, R, C>(input, vrb), Grad<X> {
  fun df() = input.df()
  fun SFun<X>.df(): MFun<X, R, C> = when (this@df) {
    is SVar -> vrb.vMat.map { if (it == this@df) One() else Zero() }
    is SConst -> vrb.vMat.map { Zero() }
    is Sum -> left.df() + right.df()
    is Prod -> left.df() * right + left * right.df()
    is Power ->
      if (right.bindings.complete) right * left.pow(right - One()) * left.df()
      else (left.df() * right * (One<X>() / left) + right.df() * left.ln())
    is Negative -> -input.df()
    is Log -> (left pow -One<X>()) * left.df()
    is DProd -> vrb.map { q -> invoke().d(q as SVar<X>) }
    is VSumAll<*, *> -> vrb.map { q -> invoke().d(q as SVar<X>) }
    is SComposition -> evaluate.df()
    else -> TODO(this@df::class.simpleName!!)
  }
}

class Jacobian<X: SFun<X>, R: D1, C: D1>(override val input: VFun<X, R>, override val vrb: VVar<X, C>): MFun<X, R, C>(input, vrb), Grad<X> {
  fun df() = input.df()

  fun VFun<X, R>.df(): MFun<X, R, C> = when (this@df) {
    is VSum -> left.df() + right.df()
    is VNegative -> -input.df()
    else -> input.vMap { it.d(vrb) }
  }
}

open class MVar<X: SFun<X>, R: D1, C: D1> constructor(
  val r: Nat<R>, val c: Nat<C>,
  override val name: String = "",
  val vVars: List<VVar<X, C>> = List(r.i) { VVar(c, "$name[$it]") },
  val vMat: Mat<X, R, C> = Mat(List(r.i) { row -> vVars[row].sVars })
): Variable<X>, MFun<X, R, C>() {
  override val bindings: Bindings<X> = Bindings(mapOf(this to vMat))
  override fun equals(other: Any?) = other is MVar<*, *, *> && name == other.name
  override fun hashCode(): Int = name.hashCode()
  operator fun getValue(thisRef: Any?, property: KProperty<*>) =
    MVar<X, R, C>(r, c, name.ifEmpty { property.name })
}

open class MConst<X: SFun<X>, R: D1, C: D1>(vararg val vConsts: VConst<X, C>): Mat<X, R, C>(vConsts.toList()), Constant<X> {
  constructor(fVec: DoubleMatrix): this(*fVec.rows.map { VConst<X, C>(*it.toTypedArray()) }.toTypedArray())

  val mat by lazy { DoubleMatrix(numRows, numCols) { row, col -> (vConsts[row][col] as SConst<X>).doubleValue } }

  override fun times(multiplicand: SFun<X>): Mat<X, R, C> =
    when (multiplicand) {
      is SConst -> MConst(mat * multiplicand.doubleValue)
      else -> super.times(multiplicand)
    }

  override fun times(multiplicand: VFun<X, C>): VFun<X, R> =
    when (multiplicand) {
      is VConst -> multiplicand * this
      else -> super.times(multiplicand)
    }

  override operator fun <Q: D1> times(multiplicand: MFun<X, C, Q>): MFun<X, R, Q> = when (multiplicand) {
    is MConst -> MConst(mat * multiplicand.mat)
    else -> super.times(multiplicand)
  }
}

open class Mat<X: SFun<X>, R: D1, C: D1>(open val rows: List<VFun<X, C>>):
  MFun<X, R, C>(*rows.toTypedArray()), NilFun<X> {
  constructor(vararg rows: Vec<X, C>): this(rows.asList())
//  constructor(prototype: X, r: Nat<R>, c: Nat<C>, gen: (Int, Int) -> Any): this(
//    (0 until r.i).map { i ->
//      (0 until c.i).map { j ->
//        prototype.wrapOrError(gen(i, j)) as SFun<X>
//      }
//    }.map { Vec<X, C>(it) }
//  )

  fun materialize() = rows.map { it.invoke() }.also { rows ->
    rows.indices.zip(rows).filter { it.second.size != numCols }.run {
      require(isEmpty()) { "Declared $numCols cols but row(s) ${map { it.first }} contain(s) ${map { it.second }} inputs, respectively" }
    }
  }

  val flattened: List<SFun<X>> by lazy { materialize().flatMap { it.contents } }
  val numRows: Int = rows.size
  val numCols: Int by lazy { rows.first().invoke().size }
  val indices: List<Int> = rows.indices.toList()
  val cols: List<VFun<X, R>> by lazy { (0 until numCols).map { i -> Vec(materialize().map { it[i] }) } }

  override fun sum() = rows.reduce { acc, it -> acc + it }

  override val ᵀ: Mat<X, C, R> by lazy { Mat(cols) }

  override fun map(ef: (SFun<X>) -> SFun<X>): Mat<X, R, C> =
    Mat(rows.map { row -> row.map { ef(it)(mapInput to it) } })

  override fun unaryMinus(): Mat<X, R, C> = map { -it }

  override fun plus(addend: MFun<X, R, C>): MFun<X, R, C> =
    when (addend) {
      is Mat -> Mat(rows.mapIndexed { i, r -> r + addend[i] })
      else -> super.plus(addend)
    }

  operator fun get(i: Int): VFun<X, C> = rows[i]
  operator fun get(i: Int, j: Int): SFun<X> = (rows[i] as Vec<X, C>)[j]

  override fun times(multiplicand: SFun<X>): Mat<X, R, C> = map { it * multiplicand }

  override fun times(multiplicand: VFun<X, C>): VFun<X, R> =
    when (multiplicand) {
      is Vec -> Vec(rows.map { r -> r dot multiplicand })
      else -> super.times(multiplicand)
    }

  override fun <Q: D1> times(multiplicand: MFun<X, C, Q>): MFun<X, R, Q> =
    when (multiplicand) {
      is Mat -> Mat(indices.map { i ->
        Vec(multiplicand.cols.indices.map { j ->
          rows[i] dot multiplicand.cols[j]
        })
      })
      else -> super.times(multiplicand)
    }
}