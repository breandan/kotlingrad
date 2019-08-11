package edu.umontreal.kotlingrad.samples

fun main() {
  val q = X + Y + Z + Y
  val shouldBe8: Double = q(X = 1.0, Y = 2.0, Z = 3.0)
  println(shouldBe8)
  val shouldBe4: Double = q(X to 1.0, Y to 1.0)(1.0)
  println(shouldBe4)
  val shouldBe4Again: Double = q(X to 1.0)(Y = 1.0, Z = 1.0)
  println(shouldBe4Again)
  val t = X + Z + Z
  val l: Z = t(X to 1.0)
  val p = X + Y * Z
  val shouldBe7 = p.invoke(X = 1.0, Y = 2.0, Z = 3.0)
  println(shouldBe7)
  val d = X + Z
}

//interface V
//interface C: V { companion object: C }
open class X(val left: Fnc<*>,
             val right: Fnc<*>,
             val app: (Double, Double) -> Double) : Fnc<X>() {
  constructor(c: Double): this(DConst(c), DConst(c), first)
  override operator fun plus(that: X): X = X(this, that, plus)
  operator fun plus(that: Y): XY = XY(this, that, plus)
  operator fun plus(that: Z): XZ = XZ(this, that, plus)
  operator fun plus(that: XY): XY = XY(this, that, plus)
  operator fun plus(that: XZ): XZ = XZ(this, that, plus)
  operator fun plus(that: YZ): XYZ = XYZ(this, that, plus)
  operator fun plus(that: XYZ): XYZ = XYZ(this, that, plus)
  override operator fun times(that: X): X = X(this, that, times)
  operator fun times(that: Y): XY = XY(this, that, times)
  operator fun times(that: Z): XZ = XZ(this, that, times)
  operator fun times(that: XY): XY = XY(this, that, times)
  operator fun times(that: XZ): XZ = XZ(this, that, times)
  operator fun times(that: YZ): XYZ = XYZ(this, that, times)
  operator fun times(that: XYZ): XYZ = XYZ(this, that, times)

  open operator fun invoke(X: Double): Double = app(left(X), right(X))
  operator fun Fnc<*>.invoke(X: Double): Double = when(this) {
    is DConst -> c
    is X -> this(X)
    else -> throw ClassNotFoundException(this.toString())
  }

  companion object : X(Vrb, Vrb, first) {
    override fun invoke(X: Double): Double = X
  }
}

open class Y(val left: Fnc<*>,
             val right: Fnc<*>,
             val app: (Double, Double) -> Double) : Fnc<Y>() {
  constructor(c: Double): this(DConst(c), DConst(c), first)
  override operator fun plus(that: Y): Y = Y(this, that, plus)
  operator fun plus(that: X): XY = XY(this, that, plus)
  operator fun plus(that: Z): YZ = YZ(this, that, plus)
  operator fun plus(that: XY): XY = XY(this, that, plus)
  operator fun plus(that: XZ): XYZ = XYZ(this, that, plus)
  operator fun plus(that: YZ): YZ = YZ(this, that, plus)
  operator fun plus(that: XYZ): XYZ = XYZ(this, that, plus)
  override operator fun times(that: Y): Y = Y(this, that, times)
  operator fun times(that: X): XY = XY(this, that, times)
  operator fun times(that: Z): YZ = YZ(this, that, times)
  operator fun times(that: XY): XY = XY(this, that, times)
  operator fun times(that: XZ): XYZ = XYZ(this, that, times)
  operator fun times(that: YZ): YZ = YZ(this, that, times)
  operator fun times(that: XYZ): XYZ = XYZ(this, that, times)

  open operator fun invoke(Y: Double): Double = app(left(Y), right(Y))
  operator fun Fnc<*>.invoke(Y: Double): Double = when(this) {
    is DConst -> c
    is Y -> this(Y)
    else -> throw ClassNotFoundException(this.toString())
  }

  companion object : Y(Vrb, Vrb, first) {
    override fun invoke(Y: Double): Double = Y
  }
}

open class Z(val left: Fnc<*>,
             val right: Fnc<*>,
             val app: (Double, Double) -> Double) : Fnc<Z>() {
  constructor(c: Double): this(DConst(c), DConst(c), first)
  override operator fun plus(that: Z): Z = Z(this, that, plus)
  operator fun plus(that: Y): YZ = YZ(this, that, plus)
  operator fun plus(that: X): XZ = XZ(this, that, plus)
  operator fun plus(that: XY): XYZ = XYZ(this, that, plus)
  operator fun plus(that: XZ): XZ = XZ(this, that, plus)
  operator fun plus(that: YZ): YZ = YZ(this, that, plus)
  operator fun plus(that: XYZ): XYZ = XYZ(this, that, plus)
  override operator fun times(that: Z): Z = Z(this, that, times)
  operator fun times(that: Y): YZ = YZ(this, that, times)
  operator fun times(that: X): XZ = XZ(this, that, times)
  operator fun times(that: XY): XYZ = XYZ(this, that, times)
  operator fun times(that: XZ): XZ = XZ(this, that, times)
  operator fun times(that: YZ): YZ = YZ(this, that, times)
  operator fun times(that: XYZ): XYZ = XYZ(this, that, times)

  open operator fun invoke(Z: Double): Double = app(left(Z), right(Z))
  operator fun Fnc<*>.invoke(Z: Double): Double = when(this) {
    is DConst -> c
    is Z -> this(Z)
    else -> throw ClassNotFoundException(this.toString())
  }

  companion object : Z(Vrb, Vrb, first) {
    override fun invoke(Z: Double): Double = Z
  }
}

open class XY(val left: Fnc<*>,
              val right: Fnc<*>,
              val app: (Double, Double) -> Double) : Fnc<XY>() {
  constructor(f: Fnc<*>): this(f, f, first)
  constructor(c: Double): this(DConst(c), DConst(c), first)
  operator fun plus(that: X): XY = XY(this, that, plus)
  operator fun plus(that: Y): XY = XY(this, that, plus)
  operator fun plus(that: Z): XYZ = XYZ(this, that, plus)
  override operator fun plus(that: XY): XY = XY(this, that, plus)
  operator fun plus(that: XZ): XYZ = XYZ(this, that, plus)
  operator fun plus(that: YZ): XYZ = XYZ(this, that, plus)
  operator fun plus(that: XYZ): XYZ = XYZ(this, that, plus)
  operator fun times(that: X): XY = XY(this, that, times)
  operator fun times(that: Y): XY = XY(this, that, times)
  operator fun times(that: Z): XYZ = XYZ(this, that, times)
  override operator fun times(that: XY): XY = XY(this, that, times)
  operator fun times(that: XZ): XYZ = XYZ(this, that, times)
  operator fun times(that: YZ): XYZ = XYZ(this, that, times)
  operator fun times(that: XYZ): XYZ = XYZ(this, that, times)

  operator fun Fnc<*>.invoke(X: Double, Y: Double): Double = when(this) {
    is DConst -> c
    is XY -> this(X, Y)
    is X -> this(X)
    is Y -> this(Y)
    else -> throw ClassNotFoundException(this.toString())
  }
  operator fun invoke(X: Double, Y: Double) = app(left(X, Y), right(X, Y))
  operator fun invoke(X: Pair<X, Double>, Y: Pair<Y, Double>) = app(left(X.second, Y.second), right(X.second, Y.second))
  operator fun Fnc<*>.invoke(X: Pair<X, Double>): Y = when(this) {
    is XY -> this(X)
    is X -> Y(this(X.second))
    is Y -> this
    else -> throw ClassNotFoundException(this.toString())
  }
  operator fun Fnc<*>.invoke(Y: Pair<Y, Double>): X = when(this) {
    is XY -> this(Y)
    is Y -> X(this(Y.second))
    is X -> this
    else -> throw ClassNotFoundException(this.toString())
  }
  operator fun invoke(X: Pair<X, Double>): Y = Y(left(X), right(X), app)
  operator fun invoke(Y: Pair<Y, Double>): X = X(left(Y), right(Y), app)
}

open class XZ(val left: Fnc<*>,
              val right: Fnc<*>,
              val app: (Double, Double) -> Double) : Fnc<XZ>() {
  constructor(f: Fnc<*>): this(f, f, first)
  constructor(c: Double): this(DConst(c), DConst(c), first)
  operator fun plus(that: X): XZ = XZ(this, that, plus)
  operator fun plus(that: Y): XYZ = XYZ(this, that, plus)
  operator fun plus(that: Z): XZ = XZ(this, that, plus)
  operator fun plus(that: XY): XYZ = XYZ(this, that, plus)
  override operator fun plus(that: XZ): XZ = XZ(this, that, plus)
  operator fun plus(that: YZ): XYZ = XYZ(this, that, plus)
  operator fun plus(that: XYZ): XYZ = XYZ(this, that, plus)
  operator fun times(that: X): XZ = XZ(this, that, times)
  operator fun times(that: Y): XYZ = XYZ(this, that, times)
  operator fun times(that: Z): XZ = XZ(this, that, times)
  operator fun times(that: XY): XYZ = XYZ(this, that, times)
  override operator fun times(that: XZ): XZ = XZ(this, that, times)
  operator fun times(that: YZ): XYZ = XYZ(this, that, times)
  operator fun times(that: XYZ): XYZ = XYZ(this, that, times)
  operator fun Fnc<*>.invoke(X: Double, Z: Double): Double = when(this) {
    is DConst -> c
    is XZ -> this(X, Z)
    is X -> this(X)
    is Z -> this(Z)
    else -> throw ClassNotFoundException(this.toString())
  }
  operator fun invoke(X: Double, Z: Double) = app(left(X, Z), right(X, Z))
  operator fun invoke(X: Pair<X, Double>, Z: Pair<Z, Double>) = app(left(X.second, Z.second), right(X.second, Z.second))
  operator fun Fnc<*>.invoke(X: Pair<X, Double>): Z = when(this) {
    is XZ -> this(X)
    is X -> Z(this(X.second))
    is Z -> this
    else -> throw ClassNotFoundException(this.toString())
  }
  operator fun Fnc<*>.invoke(Z: Pair<Z, Double>): X = when(this) {
    is XZ -> this(Z)
    is Z -> X(this(Z.second))
    is X -> this
    else -> throw ClassNotFoundException(this.toString())
  }
  operator fun invoke(X: Pair<X, Double>): Z = Z(left(X), right(X), app)
  operator fun invoke(Z: Pair<Z, Double>): X = X(left(Z), right(Z), app)
}

open class YZ(val left: Fnc<*>,
              val right: Fnc<*>,
              val app: (Double, Double) -> Double) : Fnc<YZ>() {
  constructor(f: Fnc<*>): this(f, f, first)
  constructor(c: Double): this(DConst(c), DConst(c), first)
  operator fun plus(that: X): XYZ = XYZ(this, that, plus)
  operator fun plus(that: Z): YZ = YZ(this, that, plus)
  operator fun plus(that: Y): YZ = YZ(this, that, plus)
  operator fun plus(that: XY): XYZ = XYZ(this, that, plus)
  operator fun plus(that: XZ): XYZ = XYZ(this, that, plus)
  override fun plus(that: YZ): YZ = YZ(this, that, plus)
  operator fun plus(that: XYZ): XYZ = XYZ(this, that, plus)
  operator fun times(that: X): XYZ = XYZ(this, that, times)
  operator fun times(that: Z): YZ = YZ(this, that, times)
  operator fun times(that: Y): YZ = YZ(this, that, times)
  operator fun times(that: XY): XYZ = XYZ(this, that, times)
  operator fun times(that: XZ): XYZ = XYZ(this, that, times)
  override fun times(that: YZ): YZ = YZ(this, that, times)
  operator fun times(that: XYZ): XYZ = XYZ(this, that, times)
  operator fun Fnc<*>.invoke(Y: Double, Z: Double): Double = when(this) {
    is DConst -> c
    is YZ -> this(Y, Z)
    is Y -> this(Y)
    is Z -> this(Z)
    else -> throw ClassNotFoundException(this.toString())
  }
  operator fun invoke(Y: Double, Z: Double) = app(left(Y, Z), right(Y, Z))
  operator fun invoke(Y: Pair<Y, Double>, Z: Pair<Z, Double>) = app(left(Y.second, Z.second), right(Y.second, Z.second))
  operator fun Fnc<*>.invoke(Y: Pair<Y, Double>): Z = when(this) {
    is YZ -> this(Y)
    is Y -> Z(this(Y.second))
    is Z -> this
    else -> throw ClassNotFoundException(this.toString())
  }
  operator fun Fnc<*>.invoke(Z: Pair<Z, Double>): Y = when(this) {
    is YZ -> this(Z)
    is Z -> Y(this(Z.second))
    is Y -> this
    else -> throw ClassNotFoundException(this.toString())
  }
  operator fun invoke(Y: Pair<Y, Double>): Z = Z(left(Y), right(Y), app)
  operator fun invoke(Z: Pair<Z, Double>): Y = Y(left(Z), right(Z), app)

}

open class XYZ(val left: Fnc<*>,
               val right: Fnc<*>,
               val app: (Double, Double) -> Double) : Fnc<XYZ>() {
  operator fun plus(that: X): XYZ = XYZ(this, that, plus)
  operator fun plus(that: Y): XYZ = XYZ(this, that, plus)
  operator fun plus(that: Z): XYZ = XYZ(this, that, plus)
  operator fun plus(that: XY): XYZ = XYZ(this, that, plus)
  operator fun plus(that: XZ): XYZ = XYZ(this, that, plus)
  operator fun plus(that: YZ): XYZ = XYZ(this, that, plus)
  override fun plus(that: XYZ): XYZ = XYZ(this, that, plus)
  operator fun times(that: X): XYZ = XYZ(this, that, times)
  operator fun times(that: Y): XYZ = XYZ(this, that, times)
  operator fun times(that: Z): XYZ = XYZ(this, that, times)
  operator fun times(that: XY): XYZ = XYZ(this, that, times)
  operator fun times(that: XZ): XYZ = XYZ(this, that, times)
  operator fun times(that: YZ): XYZ = XYZ(this, that, times)
  override fun times(that: XYZ): XYZ = XYZ(this, that, times)
  operator fun Fnc<*>.invoke(X: Double, Y: Double, Z: Double): Double = when(this) {
    is DConst -> c
    is XYZ -> this(X, Y, Z)
    is XY -> this(X, Y)
    is XZ -> this(X, Z)
    is YZ -> this(Y, Z)
    is X -> this(X)
    is Y -> this(Y)
    is Z -> this(Z)
    else -> throw ClassNotFoundException(this.toString())
  }
  operator fun invoke(X: Double, Y: Double, Z: Double): Double = app(left(X, Y, Z), right(X, Y, Z))
  operator fun Fnc<*>.invoke(X: Pair<X, Double>, Z: Pair<Z, Double>): Y = when(this) {
    is XYZ -> this(X, Z)
    is XY -> this(X)
    is XZ -> Y(this(X, Z))
    is YZ -> this(Z)
    is X -> Y(this(X.second))
    is Y -> this
    is Z -> Y(this(Z.second))
    else -> throw ClassNotFoundException(this.toString())
  }
  operator fun invoke(X: Pair<X, Double>, Z: Pair<Z, Double>): Y = Y(left(X, Z), right(X, Z), app)
  operator fun Fnc<*>.invoke(X: Pair<X, Double>, Y: Pair<Y, Double>): Z = when(this) {
    is XYZ -> this(X, Y)
    is XY -> Z(this(X, Y))
    is XZ -> this(X)
    is YZ -> this(Y)
    is X -> Z(this(X.second))
    is Y -> Z(this(Y.second))
    is Z -> this
    else -> throw ClassNotFoundException(this.toString())
  }
  operator fun invoke(X: Pair<X, Double>, Y: Pair<Y, Double>): Z = Z(left(X, Y), right(X, Y), app)
  operator fun Fnc<*>.invoke(Y: Pair<Y, Double>, Z: Pair<Z, Double>): X = when(this) {
    is XYZ -> this(Y, Z)
    is XY -> this(Y)
    is XZ -> this(Z)
    is YZ -> X(this(Y, Z))
    is X -> this
    is Y -> X(this(Y.second))
    is Z -> X(this(Z.second))
    else -> throw ClassNotFoundException(this.toString())
  }
  operator fun invoke(Y: Pair<Y, Double>, Z: Pair<Z, Double>): X = X(left(Y, Z), right(Y, Z), app)
  operator fun Fnc<*>.invoke(X: Pair<X, Double>): YZ = when(this) {
    is XYZ -> this(X)
    is XY -> YZ(this(X))
    is XZ -> YZ(this(X))
    is YZ -> this
    is X -> YZ(X(X.second))
    is Y -> YZ(this)
    is Z -> YZ(this)
    else -> throw ClassNotFoundException(this.toString())
  }
  operator fun invoke(X: Pair<X, Double>): YZ = YZ(left(X), right(X), app)
  operator fun Fnc<*>.invoke(Y: Pair<Y, Double>): XZ = when(this) {
    is XYZ -> this(Y)
    is XY -> XZ(this(Y))
    is XZ -> this
    is YZ -> XZ(this(Y))
    is X -> XZ(this)
    is Y -> XZ(Y(Y.second))
    is Z -> XZ(this)
    else -> throw ClassNotFoundException(this.toString())
  }
  operator fun invoke(Y: Pair<Y, Double>): XZ = XZ(left(Y), right(Y), app)
  operator fun Fnc<*>.invoke(Z: Pair<Z, Double>): XY = when(this) {
    is XYZ -> this(Z)
    is XY -> this
    is XZ -> XY(this(Z))
    is YZ -> XY(this(Z))
    is X -> XY(this)
    is Y -> XY(this)
    is Z -> XY(Z(Z.second))
    else -> throw ClassNotFoundException(this.toString())
  }
  operator fun invoke(Z: Pair<Z, Double>): XY = XY(left(Z), right(Z), app)
}

abstract class Fnc<T : Fnc<T>> {
  abstract infix operator fun plus(that: T): T
  abstract infix operator fun times(that: T): T
}

abstract class Const<T: Fnc<T>, Y: Number>(open val c: Y): Fnc<T>()

class DConst(override val c: Double): Const<DConst, Double>(c) {
  override fun plus(that: DConst) = DConst(this.c + that.c)
  override fun times(that: DConst) = DConst(this.c * that.c)
}

class IConst(override val c: Int): Const<IConst, Int>(c) {
  override fun plus(that: IConst) = IConst(this.c + that.c)
  override fun times(that: IConst) = IConst(this.c * that.c)
}

object Vrb: Fnc<Vrb>() {
  override operator fun plus(that: Vrb): Vrb = Vrb
  override operator fun times(that: Vrb): Vrb = Vrb
}

val plus = { l: Double, r: Double -> l + r }
val times = { l: Double, r: Double -> l * r }
val first = { l: Double, _: Double -> l }
