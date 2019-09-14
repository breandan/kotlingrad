package edu.umontreal.kotlingrad.functions

//
//import edu.umontreal.kotlingrad.algebra.AbelianGroup
//import edu.umontreal.kotlingrad.algebra.Field
//import edu.umontreal.kotlingrad.dependent.*
//import java.util.*
//
//// VFun should not be a List or the concatenation operator + will conflict with vector addition
//open class VectorFun<X: Function<X>, MaxLength: `100`> protected constructor(
//  open val length: Nat<MaxLength>, val variables: Set<Var<X>> = emptySet()):
//  AbelianGroup<VectorFun<X, MaxLength>> {
//
//  companion object {
//    operator fun <T: Function<T>> invoke(): VectorConst<T, `0`> = VectorConst(`0`, arrayListOf())
//    operator fun <T: Function<T>> invoke(t: T): VectorConst<T, `1`> = VectorConst(`1`, arrayListOf(t))
//    operator fun <T: Function<T>> invoke(t0: T, t1: T): VectorConst<T, `2`> = VectorConst(`2`, arrayListOf(t0, t1))
//    operator fun <T: Function<T>> invoke(t0: T, t1: T, t2: T): VectorConst<T, `3`> = VectorConst(`3`, arrayListOf(t0, t1, t2))
//    operator fun <T: Function<T>> invoke(t0: T, t1: T, t2: T, t3: T): VectorConst<T, `4`> = VectorConst(`4`, arrayListOf(t0, t1, t2, t3))
//    operator fun <T: Function<T>> invoke(t0: T, t1: T, t2: T, t3: T, t4: T): VectorConst<T, `5`> = VectorConst(`5`, arrayListOf(t0, t1, t2, t3, t4))
//    operator fun <T: Function<T>> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T): VectorConst<T, `6`> = VectorConst(`6`, arrayListOf(t0, t1, t2, t3, t4, t5))
//    operator fun <T: Function<T>> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T): VectorConst<T, `7`> = VectorConst(`7`, arrayListOf(t0, t1, t2, t3, t4, t5, t6))
//    operator fun <T: Function<T>> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T): VectorConst<T, `8`> = VectorConst(`8`, arrayListOf(t0, t1, t2, t3, t4, t5, t6, t7))
//    operator fun <T: Function<T>> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T): VectorConst<T, `9`> = VectorConst(`9`, arrayListOf(t0, t1, t2, t3, t4, t5, t6, t7, t8))
//  }
//
//  override val one: VectorConst<X, MaxLength>
//    get() = VectorConst(length, (0..length.i).map { this().first().one })
//  override val zero: VectorConst<X, MaxLength>
//    get() = VectorConst(length, (0..length.i).map { this().first().zero })
//
////  private constructor(length: Nat<MaxLength>, vector: Collection<X>): this(length, vector.mapTo(ArrayList<X>(vector.size)) { it })
////  private constructor(length: Nat<MaxLength>, vararg vector: X): this(length, arrayListOf(*vector))
////
////  override operator fun invoke(map: Map<Var<X>, X>) = when (this) {
////    is VectorProduct -> merge(multiplicand) { it.first(map) * it.second(map) }
////    is VectorSum -> merge(addend) { it.first(map) + it.second(map) }
////    else -> throw Exception("Unknown")
////  }
//
//  open operator fun invoke(map: Map<Var<X>, X> = emptyMap()): VectorConst<X, MaxLength> = when (this) {
//    is VectorSum<X, MaxLength> -> augend(map) + addend(map)
//    is VectorProduct<X, MaxLength> -> multiplicator(map) * multiplicand(map)
////    is VectorDotProduct<X, MaxLength> -> multiplicator(map) dot multiplicand(map)
//    is VectorNeg<X, MaxLength> -> argument(map)
//    else -> TODO()
//  }
//
//  override fun unaryMinus() = VectorNeg(this)
//  override fun plus(addend: VectorFun<X, MaxLength>) = VectorSum(this, addend)
//  override fun minus(subtrahend: VectorFun<X, MaxLength>) = VectorSum(this, subtrahend)
//  override fun times(multiplicand: VectorFun<X, MaxLength>) = VectorProduct(this, multiplicand)
//  infix fun dot(multiplicand: VectorFun<X, MaxLength>) = VectorDotProduct(this, multiplicand)
//
//  override fun toString(): String = when (this) {
//    is VectorProduct -> "$multiplicator*$multiplicand"
//    is VectorSum -> "$augend+$addend"
//    is VectorDotProduct -> "$multiplicator+$multiplicand"
//    is VectorNeg -> "-$argument"
//    else -> "UNKNOWN"
//  }
//}
//
//class VectorNeg<X: Function<X>, MaxLength: `100`> internal constructor(
//  val argument: VectorFun<X, MaxLength>
//): VectorFun<X, MaxLength>(argument.length, argument.variables)
//
//class VectorProduct<X: Function<X>, MaxLength: `100`> internal constructor(
//  val multiplicator: VectorFun<X, MaxLength>,
//  val multiplicand: VectorFun<X, MaxLength>
//): VectorFun<X, MaxLength>(multiplicator.length, multiplicator.variables + multiplicand.variables)
//
//class VectorDotProduct<X: Function<X>, MaxLength: `100`> internal constructor(
//  val multiplicator: VectorFun<X, MaxLength>,
//  val multiplicand: VectorFun<X, MaxLength>
//): VectorFun<X, MaxLength>(multiplicator.length, multiplicator.variables + multiplicand.variables)
//
//class VectorSum<X: Function<X>, MaxLength: `100`> internal constructor(
//  val augend: VectorFun<X, MaxLength>,
//  val addend: VectorFun<X, MaxLength>
//): VectorFun<X, MaxLength>(augend.length, augend.variables + addend.variables)
//
//class VectorConst<X: Function<X>, MaxLength: `100`> internal constructor(
//  override val length: Nat<MaxLength>,
//  private val contents: List<X>): VectorFun<X, MaxLength>(length), List<X> by contents {
//  init {
//    if (length.i != contents.size) throw IllegalArgumentException("Declared $length, but found ${contents.size}")
//  }
//
////  override operator fun invoke(map: Map<Var<X>, X>): VectorConst<X, MaxLength> = VectorConst(length, contents.map { it(map) })
//
//  operator fun plus(addend: VectorConst<X, MaxLength>): VectorConst<X, MaxLength> = merge(addend) { it.first + it.second }
//  operator fun minus(subtrahend: VectorConst<X, MaxLength>): VectorConst<X, MaxLength> = merge(subtrahend) { it.first - it.second }
//  operator fun times(multiplicand: VectorConst<X, MaxLength>): VectorConst<X, MaxLength> = merge(multiplicand) { it.first * it.second }
//  infix fun dot(vx: VectorConst<X, MaxLength>): X = (this * vx).reduce { acc, it -> acc + it }
//
//  private fun merge(vFun: VectorConst<X, MaxLength>, operation: (Pair<X, X>) -> X) = VectorConst(length, zip(vFun).map(operation))
//  fun broadcast(operation: (X) -> X): VectorConst<X, MaxLength> = VectorConst(length, contents.map(operation))
//  infix operator fun plus(addend: X): VectorFun<X, MaxLength> = broadcast { it + addend }
//  infix operator fun minus(subtrahend: X): VectorFun<X, MaxLength> = broadcast { it - subtrahend }
//  infix operator fun times(multiplicand: X): VectorFun<X, MaxLength> = broadcast { it * multiplicand }
//  infix operator fun div(divisor: X): VectorFun<X, MaxLength> = broadcast { it / divisor }
//
//  override fun toString() = contents.toString()
//}
