@file:Suppress("DuplicatedCode", "LocalVariableName", "UNUSED_PARAMETER", "NonAsciiCharacters", "FunctionName", "PropertyName", "MemberVisibilityCanBePrivate", "UNUSED_VARIABLE")

package edu.umontreal.kotlingrad.experimental

import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.minus
import guru.nidi.graphviz.model.Factory
import guru.nidi.graphviz.model.MutableNode

/**
 * Matrix function.
 */

open class MFun<X: SFun<X>, R: D1, C: D1>(override val bindings: Bindings<X>): Fun<X> {
  constructor(vararg funs: Fun<X>): this(Bindings(*funs))

  open val ᵀ: MFun<X, C, R> by lazy { MTranspose(this) }

  override fun invoke(newBindings: Bindings<X>): MFun<X, R, C> =
    MComposition(this, newBindings).evaluate//.run { if (newBindings.areReassignmentFree) evaluate else this }

  // Materializes the concrete matrix from the dataflow graph
  operator fun invoke(): Mat<X, R, C> = invoke(bindings) as Mat<X, R, C>

  open fun map(ef: (SFun<X>) -> SFun<X>): MFun<X, R, C> = MMap(this, ef)

  // Materializes the concrete matrix from the dataflow graph
  fun coalesce(): Mat<X, R, C> = this(Bindings()) as Mat<X, R, C>
  open fun d(sVar: Var<X>): MFun<X, R, C> = MDerivative(this, sVar)

  open operator fun unaryMinus(): MFun<X, R, C> = MNegative(this)
  open operator fun plus(addend: MFun<X, R, C>): MFun<X, R, C> = MSum(this, addend)
  open operator fun minus(addend: MFun<X, R, C>): MFun<X, R, C> = MSum(this, -addend)
  open operator fun times(multiplicand: SFun<X>): MFun<X, R, C> = MSProd(this, multiplicand)
  open operator fun times(multiplicand: VFun<X, C>): VFun<X, R> = MVProd(this, multiplicand)
  open infix fun ʘ(multiplicand: MFun<X, R, C>): MFun<X, R, C> = HProd(this, multiplicand)
  open operator fun <Q: D1> times(multiplicand: MFun<X, C, Q>): MFun<X, R, Q> = MMProd(this, multiplicand)

  override fun toGraph(): MutableNode = Factory.mutNode(if (this is MVar) "MVar($name)" else "${hashCode()}").apply {
    when (this@MFun) {
      is MVar -> "$name-MVar$r$c"
      is MGradient -> { sFun.toGraph() - this; Factory.mutNode("$this").apply { add(Label.of(mVar.toString())) } - this; add(Label.of("grad")) }
      is Mat -> { flattened.map { it.toGraph() - this } }
      is BiFun<*> -> { (left.toGraph() - this).add(Color.BLUE); (right.toGraph() - this).add(Color.RED); add(Label.of(opCode())) }
      is UnFun<*> -> { input.toGraph() - this; add(Label.of(opCode())) }
      else -> TODO(this@MFun.javaClass.toString())
    }
  }

  override fun toString() = when (this) {
    is MNegative -> "-($input)"
    is MTranspose -> "($input).T"
    is BiFun<*> -> "$left ${opCode()} $right"
    is UnFun<*> -> "${opCode()} $input"
    is MConst -> "${javaClass.name}()"
    is Mat -> "Mat${numRows}x$numCols(${rows.joinToString(", ") { it.contents.joinToString(", ") }})"
    is MDerivative -> "d($mFun) / d($sVar)"
    is MVar -> "MVar($name)"
    else -> TODO(this.javaClass.name)
  }
}

class MMap<X: SFun<X>, R: D1, C: D1>(val value: MFun<X, R, C>, val ef: (SFun<X>) -> SFun<X>): MFun<X, R, C>(value)
class MNegative<X: SFun<X>, R: D1, C: D1>(override val input: MFun<X, R, C>): MFun<X, R, C>(input), UnFun<X>
class MTranspose<X: SFun<X>, R: D1, C: D1>(override val input: MFun<X, R, C>): MFun<X, C, R>(input), UnFun<X>
class MSum<X: SFun<X>, R: D1, C: D1>(override val left: MFun<X, R, C>, override val right: MFun<X, R, C>): MFun<X, R, C>(left, right), BiFun<X>
class MMProd<X: SFun<X>, R: D1, C1: D1, C2: D1>(override val left: MFun<X, R, C1>, override val right: MFun<X, C1, C2>): MFun<X, R, C2>(left, right), BiFun<X>
class HProd<X: SFun<X>, R: D1, C: D1>(override val left: MFun<X, R, C>, override val right: MFun<X, R, C>): MFun<X, R, C>(left, right), BiFun<X>
class MSProd<X: SFun<X>, R: D1, C: D1>(override val left: MFun<X, R, C>, override val right: SFun<X>): MFun<X, R, C>(left), BiFun<X>
class SMProd<X: SFun<X>, R: D1, C: D1>(override val left: SFun<X>, override val right: MFun<X, R, C>): MFun<X, R, C>(right), BiFun<X>

class MComposition<X: SFun<X>, R: D1, C: D1>(val mFun: MFun<X, R, C>, inputs: Bindings<X>): MFun<X, R, C>(mFun.bindings + inputs) {
  val evaluate: MFun<X, R, C> by lazy { bind(bindings) }

  @Suppress("UNCHECKED_CAST")
  fun MFun<X, R, C>.bind(bindings: Bindings<X>): MFun<X, R, C> =
    bindings[this@bind] ?: when (this@bind) {
      is MVar -> this@bind
      is MConst -> this@bind
      is Mat -> map { it(bindings) }
      is MNegative -> -input.bind(bindings)
      is MTranspose -> input.ᵀ.bind(bindings)
      is MSum -> left.bind(bindings) + right.bind(bindings)
      is MMProd<X, R, *, C> -> left(bindings) as MFun<X, R, D1> * right(bindings) as MFun<X, D1, C>
      is HProd -> left.bind(bindings) ʘ right.bind(bindings)
      is MSProd -> left.bind(bindings) * right(bindings)
      is SMProd -> left(bindings) * right.bind(bindings)
      is MDerivative -> df()(bindings)
      is MGradient -> df()(bindings)
      is MComposition -> mFun.bind(bindings)
      is MMap<X, R, C> -> value.bind(bindings).map { ef(it)(bindings) }
      is Jacobian -> df()(bindings)
      else -> this
    }
}

// TODO: Generalize tensor derivatives? https://en.wikipedia.org/wiki/Tensor_derivative_(continuum_mechanics)
@Suppress("UNCHECKED_CAST")
class MDerivative<X: SFun<X>, R: D1, C: D1>(val mFun: MFun<X, R, C>, val sVar: Var<X>): MFun<X, R, C>(mFun) {
  fun MFun<X, R, C>.df(): MFun<X, R, C> = when (this@df) {
    is MVar -> MGradient(sVar, this@df).df()
    is Mat -> Mat(rows.map { it.d(sVar)() })
    is MConst -> map { Zero() }
    is MNegative -> -input.df()
    is MTranspose -> (input as MFun<X, R, C>).df().ᵀ as MFun<X, R, C>
    is MSum -> left.df() + right.df()
    // Casting here is necessary because of type erasure (we loose the inner dimension when MMProd<X, R, C1, C2> is boxed as MFun<X, R, C2>)
    is MMProd<X, R, *, C> -> (left as MFun<X, R, C>).df() * (right as MFun<X, C, C>) + left * ((right as MFun<X, R, C>).df() as MFun<X, C, C>)
    is MSProd -> left.df() * right + left * right.d(sVar)
    is SMProd -> left.d(sVar) * right + left * right.df()
    is HProd -> left.df() ʘ right + left ʘ right.df()
    is MMap -> value.df().map(ef).map { it * ef(it) } // Chain rule
    else -> TODO(this@df.javaClass.name)
  }
}

class MGradient<X : SFun<X>, R: D1, C: D1>(val sFun: SFun<X>, val mVar: MVar<X, R, C>): MFun<X, R, C>(sFun) {
  fun df() = sFun.df()
  fun SFun<X>.df(): MFun<X, R, C> = when (this@df) {
    is Var -> map { if (it == this@df) One() else Zero() }
    is SConst -> map { Zero() }
    is Sum -> left.df() + right.df()
    is Prod -> left.df() * right + left * right.df()
    is Power ->
      if (right.bindings.sVars.isEmpty()) right * left.pow(right - One()) * left.df()
      else (left.df() * right * (One<X>() / left) + right.df() * left.ln())
    is Negative -> -input.df()
    is Log -> (left pow -One<X>()) * left.df()
    is DProd -> invoke().df()
    is Composition -> evaluate.df()
    else -> TODO(this@df.javaClass.name)
  }
}

class Jacobian<X : SFun<X>, R: D1, C: D1>(val vfn: VFun<X, R>, val vVar: VFun<X, C>): MFun<X, R, C>(vfn) {
  fun df() = vfn.df()
  fun VFun<X, R>.df(): MFun<X, R, C> = when (this@df) {
    is VVar -> Mat(vfn().contents.map { output -> Vec(vVar().contents.map { output.d(it.bindings.sVars.first()) }) })
//    is VConst<X, E> -> Vec(consts.map { Zero() })
//    is VSum -> left.df() + right.df()
//    is VVProd -> left.df() ʘ right + left ʘ right.df()
//    is SVProd -> left.d(v1) * right + left * right.df()
//    is VSProd -> left.df() * right + left * right.d(v1)
//    is VNegative -> -input.df()
//    is VDerivative -> vFun.df().df()
//    is Vec -> Vec(contents.map { it.d(v1) })
//    is MVProd<X, E, *> -> invoke().df()
//    is VMProd<X, *, E> -> invoke().df()
//    is Gradient -> invoke()
//    is VMap -> input.df().map(ssMap).map { it * ssMap(it) } // Chain rule
//    is VComposition -> evaluate.df()
    else -> VVMap(vfn) { vVar.map { it() } }
  }
}

class MVar<X: SFun<X>, R: D1, C: D1>(
  override val name: String = "", val r: R, val c: C,
  val sVars: List<Var<X>> = List(r.i * c.i) { Var("$name[${it / c.i},${it % c.i}]") },
  val sMat: Mat<X, R, C> = Mat(List(r.i) { row -> Vec(List(c.i) { col -> sVars[row * c.i + col] }) })
): Variable<X>, MFun<X, R, C>() {
  override val bindings: Bindings<X> = Bindings(mapOf(this to sMat))
}

open class MConst<X: SFun<X>, R: D1, C: D1>: Mat<X, R, C>()

open class Mat<X: SFun<X>, R: D1, C: D1>(open val rows: List<Vec<X, C>>):
  MFun<X, R, C>(*rows.toTypedArray()), Iterable<Vec<X, C>> by rows {
  constructor(vararg rows: Vec<X, C>): this(rows.asList())

  val flattened: List<SFun<X>> by lazy { rows.flatMap { it.contents } }

  val numRows = rows.size
  val numCols = rows.first().contents.size
  val indices = rows.indices.take(numRows)
  val cols by lazy { indices.map { i -> Vec<X, R>(rows.map { it[i] }) } }

  init {
    rows.indices.zip(rows).filter { it.second.size != numCols }.run {
      require(isEmpty()) { "Declared $numCols cols but row(s) ${map { it.first }} contain(s) ${map { it.second }} inputs, respectively" }
    }
  }

  override val ᵀ: Mat<X, C, R> by lazy { Mat(cols) }

  override fun map(ef: (SFun<X>) -> SFun<X>): Mat<X, R, C> = Mat(rows.map { it.map(ef) })

  override fun unaryMinus(): Mat<X, R, C> = map { -it }

  override fun plus(addend: MFun<X, R, C>): MFun<X, R, C> =
    when (addend) {
      is Mat -> Mat(mapIndexed { i, r -> (r + addend[i]) as Vec<X, C> })
      else -> super.plus(addend)
    }

  operator fun get(i: Int): VFun<X, C> = rows[i]

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