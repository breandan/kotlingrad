@file:Suppress("DuplicatedCode", "LocalVariableName", "UNUSED_PARAMETER", "NonAsciiCharacters", "FunctionName", "PropertyName", "MemberVisibilityCanBePrivate", "UNUSED_VARIABLE")

package edu.umontreal.kotlingrad.experimental

import guru.nidi.graphviz.attribute.Color.BLUE
import guru.nidi.graphviz.attribute.Color.RED
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.model.Factory.*
import guru.nidi.graphviz.model.MutableNode
import org.jetbrains.bio.viktor.F64Array
import kotlin.reflect.KProperty

/**
 * Matrix function.
 */

open class MFun<X: SFun<X>, R: D1, C: D1>(override val bindings: Bindings<X>): Fun<X> {
  constructor(vararg funs: Fun<X>): this(Bindings(*funs))
  override val proto by lazy { bindings.proto }

  open val ᵀ: MFun<X, C, R> by lazy { MTranspose(this) }

  override fun invoke(newBindings: Bindings<X>): MFun<X, R, C> =
    MComposition(this, newBindings)
      .run { if (bindings.complete || newBindings.readyToBind || EAGER) evaluate else this }

  // Materializes the concrete matrix from the dataflow graph
  operator fun invoke(): Mat<X, R, C> =
    MComposition(this).evaluate.let {
      try {
        it as Mat<X, R, C>
      } catch (e: ClassCastException) {
        show("before"); it.show("after")
        throw NumberFormatException("Matrix function has unbound free variables: ${bindings.allFreeVariables.keys}")
      }
    }

  val mapInput by lazy { SVar(bindings.proto, "mapInput") }
  open fun map(ef: (SFun<X>) -> SFun<X>): MFun<X, R, C> = MMap(this, ef(mapInput), mapInput)

  open fun d(sVar: SVar<X>): MFun<X, R, C> = MDerivative(this, sVar).let { if(EAGER) it.df() else it }

  open operator fun unaryMinus(): MFun<X, R, C> = MNegative(this)
  open operator fun plus(addend: MFun<X, R, C>): MFun<X, R, C> = MSum(this, addend)
  open operator fun minus(addend: MFun<X, R, C>): MFun<X, R, C> = MSum(this, -addend)
  open operator fun times(multiplicand: SFun<X>): MFun<X, R, C> = MSProd(this, multiplicand)
  open operator fun times(multiplicand: VFun<X, C>): VFun<X, R> = MVProd(this, multiplicand)
  open infix fun ʘ(multiplicand: MFun<X, R, C>): MFun<X, R, C> = HProd(this, multiplicand)
  open operator fun <Q: D1> times(multiplicand: MFun<X, C, Q>): MFun<X, R, Q> = MMProd(this, multiplicand)

  operator fun times(multiplicand: Number): MFun<X, R, C> = this * wrap(multiplicand)

  open fun sum(): VFun<X, C> = MSumRows(this)

  override fun toGraph(): MutableNode = mutNode(if (this is MVar) "MVar($name)" else "${hashCode()}").apply {
    when (this@MFun) {
      is MVar -> "$name-MVar$r$c"
      is MGradient -> { sFun.toGraph() - this; mutNode("$this").apply { add(Label.of(mVar.toString())) } - this; add(Label.of("grad")) }
      is Mat -> { rows.map { it.toGraph() - this }; add(Label.of("Mat")) }
      is BiFun<*> -> { (left.toGraph() - this).add(BLUE); (right.toGraph() - this).add(RED); add(Label.of(opCode())) }
      is UnFun<*> -> { input.toGraph() - this; add(Label.of(opCode())) }
      is MComposition -> { mFun.toGraph() - this; mutNode("$this").apply { add(Label.of(bindings.allFreeVariables.keys.toString())) } - this; add(Label.of("MComp")) }
      is Jacobian -> { vfn.toGraph() - this; mutNode("$this").apply { add(Label.of(vVar.toString())) } - this; add(Label.of("Jacobian")) }
      is MDerivative -> { mFun.toGraph() - this; mutNode("$this").apply { add(Label.of(sVar.toString())) } - this; add(Label.of("MDerivative")) }
      is VVMap<*, *, *> -> { (input.toGraph() - this).add(BLUE); (svMap.toGraph() - this).add(RED); add(Label.of(opCode())) }
      else -> TODO(this@MFun.javaClass.toString())
    }
  }

  override fun toString() = when (this) {
    is MNegative -> "-($input)"
    is MTranspose -> "($input).T"
    is BiFun<*> -> "$left ${opCode()} $right"
    is UnFun<*> -> "${opCode()} $input"
    is MConst -> "${javaClass.name}()"
    is Mat -> "Mat(${rows.joinToString(", ")})"
    is MDerivative -> "d($mFun) / d($sVar)"
    is MVar -> "MVar($name)"
    is MComposition -> "MComp($mFun)$bindings"
    is Jacobian -> "Jacobian($vVar)$bindings"
    else -> javaClass.simpleName
  }

  override operator fun invoke(vararg numbers: Number): MFun<X, R, C> = invoke(bindings.zip(numbers.map { wrap(it) }))
  override operator fun invoke(vararg funs: Fun<X>): MFun<X, R, C> = invoke(bindings.zip(funs.toList()))
  override operator fun invoke(vararg ps: Pair<Fun<X>, Any>): MFun<X, R, C> = invoke(ps.toList().bind())
}

class MMap<X: SFun<X>, R: D1, C: D1>(override val input: MFun<X, R, C>, val ssMap: SFun<X>, placeholder: SVar<X>):
  MFun<X, R, C>(input.bindings + ssMap.bindings - placeholder), UnFun<X>
class MNegative<X: SFun<X>, R: D1, C: D1>(override val input: MFun<X, R, C>): MFun<X, R, C>(input), UnFun<X>
class MTranspose<X: SFun<X>, R: D1, C: D1>(override val input: MFun<X, R, C>): MFun<X, C, R>(input), UnFun<X>
class MSum<X: SFun<X>, R: D1, C: D1>(override val left: MFun<X, R, C>, override val right: MFun<X, R, C>): MFun<X, R, C>(left, right), BiFun<X>
class MMProd<X: SFun<X>, R: D1, C1: D1, C2: D1>(override val left: MFun<X, R, C1>, override val right: MFun<X, C1, C2>): MFun<X, R, C2>(left, right), BiFun<X>
class HProd<X: SFun<X>, R: D1, C: D1>(override val left: MFun<X, R, C>, override val right: MFun<X, R, C>): MFun<X, R, C>(left, right), BiFun<X>
class MSProd<X: SFun<X>, R: D1, C: D1>(override val left: MFun<X, R, C>, override val right: SFun<X>): MFun<X, R, C>(left), BiFun<X>
class SMProd<X: SFun<X>, R: D1, C: D1>(override val left: SFun<X>, override val right: MFun<X, R, C>): MFun<X, R, C>(right), BiFun<X>

class MComposition<X: SFun<X>, R: D1, C: D1>(val mFun: MFun<X, R, C>, inputs: Bindings<X> = Bindings(mFun.proto)): MFun<X, R, C>(mFun.bindings + inputs) {
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
      is MMProd<X, R, *, C> -> left(bnds) as MFun<X, R, D1> * right(bnds) as MFun<X, D1, C>
      is HProd -> left.bind(bnds) ʘ right.bind(bnds)
      is MSProd -> left.bind(bnds) * right(bnds)
      is SMProd -> left(bnds) * right.bind(bnds)
      is MDerivative -> df().bind(bnds)
      is MGradient -> df().bind(bnds)
      is MComposition -> mFun.bind(bnds)
      is MMap<X, R, C> -> input.bind(bnds).map { ssMap(mapInput to it)(bnds) }
      is Jacobian -> df().bind(bnds)
      is VVMap -> input(bnds).vMap { svMap(mapInput to it)(bnds) }
      else -> TODO(this@bind.toString() + "/" + bnds)
    }.also { result -> bnds.checkForUnpropagatedVariables(this@bind, result) }
}

// TODO: Generalize tensor derivatives? https://en.wikipedia.org/wiki/Tensor_derivative_(continuum_mechanics)
@Suppress("UNCHECKED_CAST")
class MDerivative<X: SFun<X>, R: D1, C: D1>(val mFun: MFun<X, R, C>, val sVar: SVar<X>): MFun<X, R, C>(mFun) {
  fun df() = mFun.df()
  fun MFun<X, R, C>.df(): MFun<X, R, C> = when (this@df) {
    is MVar -> map { it.d(sVar) }
    is Mat -> map { it.d(sVar) }
    is MConst -> map { Zero() }
    is MNegative -> -input.df()
    is MTranspose -> (input as MFun<X, R, C>).df().ᵀ as MFun<X, R, C>
    is MSum -> left.df() + right.df()
    // Casting here is necessary because of type erasure (we loose the inner dimension when MMProd<X, R, C1, C2> is boxed as MFun<X, R, C2>)
    is MMProd<X, R, *, C> ->
      (left as MFun<X, R, C>).df() * (right as MFun<X, C, C>) +
      left * ((right as MFun<X, R, C>).df() as MFun<X, C, C>)
    is MSProd -> left.df() * right + left * right.d(sVar)
    is SMProd -> left.d(sVar) * right + left * right.df()
    is HProd -> left.df() ʘ right + left ʘ right.df()
    is MMap -> input.df().map { it * ssMap.d(sVar) } // Chain rule
    is VVMap -> input.d(sVar).vMap { it * svMap(mapInput to it).d(sVar) }
    is MDerivative -> mFun.df()
    is MComposition -> evaluate.df()
    else -> TODO(this@df.javaClass.name)
  }
}

class MGradient<X : SFun<X>, R: D1, C: D1>(val sFun: SFun<X>, val mVar: MVar<X, R, C>): MFun<X, R, C>(sFun) {
  fun df() = sFun.df()
  fun SFun<X>.df(): MFun<X, R, C> = when (this@df) {
    is SVar -> mVar.vMat.map { if (it == this@df) One() else Zero() }
    is SConst -> mVar.vMat.map { Zero() }
    is Sum -> left.df() + right.df()
    is Prod -> left.df() * right + left * right.df()
    is Power ->
      if (right.bindings.complete) right * left.pow(right - One()) * left.df()
      else (left.df() * right * (One<X>() / left) + right.df() * left.ln())
    is Negative -> -input.df()
    is Log -> (left pow -One<X>()) * left.df()
    is DProd -> invoke().d(mVar)
//      mVar.vMap { vVar ->
//      (left.d(vVar) as MFun<X, C, C> * (right as VFun<X, C>) +
//        left as VFun<X, C> * right.d(vVar)) }
    is SComposition -> evaluate.df()
    else -> TODO(this@df.javaClass.name)
  }
}

class Jacobian<X : SFun<X>, R: D1, C: D1>(val vfn: VFun<X, R>, val vVar: VVar<X, C>): MFun<X, R, C>(vfn) {
  fun df() = vfn.df()

  fun VFun<X, R>.df(): MFun<X, R, C> = when (this@df) {
    is VSum -> left.df() + right.df()
    is VNegative -> -input.df()
    else -> vfn().vMap { it.d(vVar) }
  }
}

open class MVar<X: SFun<X>, R: D1, C: D1> constructor(
  override val proto: X,
  override val name: String = "", val r: R, val c: C,
  val vVars: List<VVar<X, C>> = List(r.i) { VVar(proto, "$name[$it]", c) },
  val vMat: Mat<X, R, C> = Mat(List(r.i) { row -> vVars[row].sVars })
//  val sVars: List<SVar<X>> = List(r.i * c.i) { SVar("$name[${it / c.i},${it % c.i}]") },
//  val sMat: Mat<X, R, C> = Mat(List(r.i) { row -> Vec(List(c.i) { col -> sVars[row * c.i + col] }) })
): Variable<X>, MFun<X, R, C>() {
  override val bindings: Bindings<X> = Bindings(mapOf(this to vMat))
  fun vMap(ef: (VVar<X, C>) -> VFun<X, C>) = Mat<X, R, C>(vVars.map { ef(it) })
  override fun equals(other: Any?) = other is MVar<*, *, *> && name == other.name
  override fun hashCode(): Int = name.hashCode()
  operator fun getValue(thisRef: Any?, property: KProperty<*>) =
    MVar(proto, if (name.isEmpty()) property.name else name, r, c, vVars, vMat)
}

open class MConst<X: SFun<X>, R: D1, C: D1>(vararg val vConsts: VConst<X, C>): Mat<X, R, C>(vConsts.toList()), Constant<X> {
  val simdVec by lazy { F64Array(numRows, numCols) { row, col -> (vConsts[row][col] as SConst<X>).doubleValue } }
}

open class Mat<X: SFun<X>, R: D1, C: D1>(open val rows: List<VFun<X, C>>):
  MFun<X, R, C>(*rows.toTypedArray()), Iterable<VFun<X, C>> by rows {
  constructor(vararg rows: Vec<X, C>): this(rows.asList())

  fun materialize() = rows.map { it.invoke() }.also { rows ->
    rows.indices.zip(rows).filter { it.second.size != numCols }.run {
      require(isEmpty()) { "Declared $numCols cols but row(s) ${map { it.first }} contain(s) ${map { it.second }} inputs, respectively" }
    }
  }

  val flattened: List<SFun<X>> by lazy { materialize().flatMap { it.contents } }
  val numRows: Int = rows.size
  val numCols: Int by lazy { rows.first().invoke().size }
  val indices: List<Int> = rows.indices.drop(1)
  val cols: List<VFun<X, R>> by lazy { indices.map { i -> Vec(materialize().map { it[i] }) } }

  override fun sum() = reduce { acc, it -> acc + it }

  override val ᵀ: Mat<X, C, R> by lazy { Mat(cols) }

  override fun map(ef: (SFun<X>) -> SFun<X>): Mat<X, R, C> =
    Mat(rows.map { row -> row.map { ef(it)(mapInput to it) } })

  override fun unaryMinus(): Mat<X, R, C> = map { -it }

  override fun plus(addend: MFun<X, R, C>): MFun<X, R, C> =
    when (addend) {
      is Mat -> Mat(mapIndexed { i, r -> r + addend[i] })
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