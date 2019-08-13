package edu.umontreal.kotlingrad.samples

fun main() {
  with(DProto) {
    val q = X + Y + Z + Y
    val totalApp = q(X to 1.0, Y to 2.0, Z to 3.0)
    println(totalApp) // Should be 8
    val partialApp = q(X to 1.0, Y to 1.0)(Z to 1.0)
    println(partialApp) // Should be 4
    val partialApp2 = q(X to 1.0)(Y to 1.0, Z to 1.0)
    println(partialApp2) // Should be 4
    val partialApp3 = q(Z to 1.0)(X to 1.0, Y to 1.0)
    println(partialApp3) // Should be 4

    val t = X + Z + Z + Y
    val l = t(X to 1.0)(Z to 2.0)
    val r = t(X to 1.0)(Z to 2.0)(Y to 3.0)

    val o = X + Z
    //val k = o(Y to 4.0) // Does not compile

    val p = X + Y * Z
    val totalApp2 = p(X to 1.0, Y to 2.0, Z to 3.0)
    println(totalApp2) // Should be 7
    val d = X + Z * X
    println(d(X to 3.0, Z to 4.0)) // Should be 15
  }
}

open class X<P : Const<P, in Number>>(val left: Fnc<*>,
                                      val right: Fnc<*>,
                                      val app: (P, P) -> P) : Fnc<X<P>>() {
  constructor(c: Const<P, in Number>) : this(c, c, ::firstX)

  override operator fun plus(that: X<P>): X<P> = X(this, that, ::plusX)
  operator fun plus(that: Y<P>): XY<P> = XY(this, that, ::plusX)
  operator fun plus(that: Z<P>): XZ<P> = XZ(this, that, ::plusX)
  operator fun plus(that: XY<P>): XY<P> = XY(this, that, ::plusX)
  operator fun plus(that: XZ<P>): XZ<P> = XZ(this, that, ::plusX)
  operator fun plus(that: YZ<P>): XYZ<P> = XYZ(this, that, ::plusX)
  operator fun plus(that: XYZ<P>): XYZ<P> = XYZ(this, that, ::plusX)
  override operator fun times(that: X<P>): X<P> = X(this, that, ::timesX)
  operator fun times(that: Y<P>): XY<P> = XY(this, that, ::timesX)
  operator fun times(that: Z<P>): XZ<P> = XZ(this, that, ::timesX)
  operator fun times(that: XY<P>): XY<P> = XY(this, that, ::timesX)
  operator fun times(that: XZ<P>): XZ<P> = XZ(this, that, ::timesX)
  operator fun times(that: YZ<P>): XYZ<P> = XYZ(this, that, ::timesX)
  operator fun times(that: XYZ<P>): XYZ<P> = XYZ(this, that, ::timesX)

  open operator fun invoke(X: XBnd<P>): Const<P, in Number> =
    app(left(X) as P, right(X) as P)

  operator fun Fnc<*>.invoke(X: XBnd<P>): Const<P, in Number> =
    when (this) {
      is Const<*, *> -> this as Const<P, in Number>
      is X<*> -> (this as X<P>)(X)
      else -> throw ClassNotFoundException(this.toString())
    }
}

open class Y<P : Const<P, in Number>>(val left: Fnc<*>,
                                      val right: Fnc<*>,
                                      val app: (P, P) -> P) : Fnc<Y<P>>() {
  constructor(c: Const<P, in Number>) : this(c, c, ::firstX)

  override operator fun plus(that: Y<P>): Y<P> = Y(this, that, ::plusX)
  operator fun plus(that: X<P>): XY<P> = XY(this, that, ::plusX)
  operator fun plus(that: Z<P>): YZ<P> = YZ(this, that, ::plusX)
  operator fun plus(that: XY<P>): XY<P> = XY(this, that, ::plusX)
  operator fun plus(that: XZ<P>): XYZ<P> = XYZ(this, that, ::plusX)
  operator fun plus(that: YZ<P>): YZ<P> = YZ(this, that, ::plusX)
  operator fun plus(that: XYZ<P>): XYZ<P> = XYZ(this, that, ::plusX)
  override operator fun times(that: Y<P>): Y<P> = Y(this, that, ::timesX)
  operator fun times(that: X<P>): XY<P> = XY(this, that, ::timesX)
  operator fun times(that: Z<P>): YZ<P> = YZ(this, that, ::timesX)
  operator fun times(that: XY<P>): XY<P> = XY(this, that, ::timesX)
  operator fun times(that: XZ<P>): XYZ<P> = XYZ(this, that, ::timesX)
  operator fun times(that: YZ<P>): YZ<P> = YZ(this, that, ::timesX)
  operator fun times(that: XYZ<P>): XYZ<P> = XYZ(this, that, ::timesX)

  open operator fun invoke(Y: YBnd<P>): Const<P, in Number> =
    app(left(Y) as P, right(Y) as P)

  operator fun Fnc<*>.invoke(Y: YBnd<P>): Const<P, in Number> =
    when (this) {
      is Const<*, *> -> this as Const<P, in Number>
      is Y<*> -> (this as Y<P>)(Y)
      else -> throw ClassNotFoundException(this.toString())
    }
}

open class Z<P : Const<P, in Number>>(val left: Fnc<*>,
                                      val right: Fnc<*>,
                                      val app: (P, P) -> P) : Fnc<Z<P>>() {
  constructor(c: Const<P, in Number>) : this(c, c, ::firstX)

  override operator fun plus(that: Z<P>): Z<P> = Z(this, that, ::plusX)
  operator fun plus(that: Y<P>): YZ<P> = YZ(this, that, ::plusX)
  operator fun plus(that: X<P>): XZ<P> = XZ(this, that, ::plusX)
  operator fun plus(that: XY<P>): XYZ<P> = XYZ(this, that, ::plusX)
  operator fun plus(that: XZ<P>): XZ<P> = XZ(this, that, ::plusX)
  operator fun plus(that: YZ<P>): YZ<P> = YZ(this, that, ::plusX)
  operator fun plus(that: XYZ<P>): XYZ<P> = XYZ(this, that, ::plusX)
  override operator fun times(that: Z<P>): Z<P> = Z(this, that, ::timesX)
  operator fun times(that: Y<P>): YZ<P> = YZ(this, that, ::timesX)
  operator fun times(that: X<P>): XZ<P> = XZ(this, that, ::timesX)
  operator fun times(that: XY<P>): XYZ<P> = XYZ(this, that, ::timesX)
  operator fun times(that: XZ<P>): XZ<P> = XZ(this, that, ::timesX)
  operator fun times(that: YZ<P>): YZ<P> = YZ(this, that, ::timesX)
  operator fun times(that: XYZ<P>): XYZ<P> = XYZ(this, that, ::timesX)

  open operator fun invoke(Z: ZBnd<P>): Const<P, in Number> =
    app(left(Z) as P, right(Z) as P)

  operator fun Fnc<*>.invoke(Z: ZBnd<P>): Const<P, in Number> =
    when (this) {
      is Const<*, *> -> this as Const<P, in Number>
      is Z<*> -> (this as Z<P>)(Z)
      else -> throw ClassNotFoundException(this.toString())
    }
}

open class XY<P : Const<P, in Number>>(val left: Fnc<*>,
                                       val right: Fnc<*>,
                                       val app: (P, P) -> P) : Fnc<XY<P>>() {
  constructor(f: Fnc<*>) : this(f, f, ::firstX)
  constructor(c: Const<P, in Number>) : this(c, c, ::firstX)

  operator fun plus(that: X<P>): XY<P> = XY(this, that, ::plusX)
  operator fun plus(that: Y<P>): XY<P> = XY(this, that, ::plusX)
  operator fun plus(that: Z<P>): XYZ<P> = XYZ(this, that, ::plusX)
  override operator fun plus(that: XY<P>): XY<P> = XY(this, that, ::plusX)
  operator fun plus(that: XZ<P>): XYZ<P> = XYZ(this, that, ::plusX)
  operator fun plus(that: YZ<P>): XYZ<P> = XYZ(this, that, ::plusX)
  operator fun plus(that: XYZ<P>): XYZ<P> = XYZ(this, that, ::plusX)
  operator fun times(that: X<P>): XY<P> = XY(this, that, ::timesX)
  operator fun times(that: Y<P>): XY<P> = XY(this, that, ::timesX)
  operator fun times(that: Z<P>): XYZ<P> = XYZ(this, that, ::timesX)
  override operator fun times(that: XY<P>): XY<P> = XY(this, that, ::timesX)
  operator fun times(that: XZ<P>): XYZ<P> = XYZ(this, that, ::timesX)
  operator fun times(that: YZ<P>): XYZ<P> = XYZ(this, that, ::timesX)
  operator fun times(that: XYZ<P>): XYZ<P> = XYZ(this, that, ::timesX)

  operator fun Fnc<*>.invoke(X: XBnd<P>, Y: YBnd<P>): Const<P, in Number> =
    when (this) {
      is Const<*, *> -> this as Const<P, in Number>
      is XY<*> -> (this as XY<P>).invoke(X, Y)
      is X<*> -> (this as X<P>)(X)
      is Y<*> -> (this as Y<P>)(Y)
      else -> throw ClassNotFoundException(this.toString())
    }

  operator fun invoke(X: XBnd<P>, Y: YBnd<P>): Const<P, in Number> =
    app(left(X, Y) as P, right(X, Y) as P)

  operator fun Fnc<*>.invoke(X: XBnd<P>): Y<P> =
    when (this) {
      is XY<*> -> (this as XY<P>)(X)
      is X<*> -> Y((this as X<P>)(X))
      is Y<*> -> this as Y<P>
      else -> throw ClassNotFoundException(this.toString())
    }

  operator fun Fnc<*>.invoke(Y: YBnd<P>): X<P> =
    when (this) {
      is XY<*> -> (this as XY<P>)(Y)
      is Y<*> -> X((this as Y<P>)(Y))
      is X<*> -> this as X<P>
      else -> throw ClassNotFoundException(this.toString())
    }

  operator fun invoke(X: XBnd<P>): Y<P> = Y(left(X), right(X), app)
  operator fun invoke(Y: YBnd<P>): X<P> = X(left(Y), right(Y), app)
}

open class XZ<P : Const<P, in Number>>(val left: Fnc<*>,
                                       val right: Fnc<*>,
                                       val app: (P, P) -> P) : Fnc<XZ<P>>() {
  constructor(f: Fnc<*>) : this(f, f, ::firstX)
  constructor(c: Const<P, in Number>) : this(c, c, ::firstX)

  operator fun plus(that: X<P>): XZ<P> = XZ(this, that, ::plusX)
  operator fun plus(that: Y<P>): XYZ<P> = XYZ(this, that, ::plusX)
  operator fun plus(that: Z<P>): XZ<P> = XZ(this, that, ::plusX)
  operator fun plus(that: XY<P>): XYZ<P> = XYZ(this, that, ::plusX)
  override operator fun plus(that: XZ<P>): XZ<P> = XZ(this, that, ::plusX)
  operator fun plus(that: YZ<P>): XYZ<P> = XYZ(this, that, ::plusX)
  operator fun plus(that: XYZ<P>): XYZ<P> = XYZ(this, that, ::plusX)
  operator fun times(that: X<P>): XZ<P> = XZ(this, that, ::timesX)
  operator fun times(that: Y<P>): XYZ<P> = XYZ(this, that, ::timesX)
  operator fun times(that: Z<P>): XZ<P> = XZ(this, that, ::timesX)
  operator fun times(that: XY<P>): XYZ<P> = XYZ(this, that, ::timesX)
  override operator fun times(that: XZ<P>): XZ<P> = XZ(this, that, ::timesX)
  operator fun times(that: YZ<P>): XYZ<P> = XYZ(this, that, ::timesX)
  operator fun times(that: XYZ<P>): XYZ<P> = XYZ(this, that, ::timesX)

  operator fun Fnc<*>.invoke(X: XBnd<P>, Z: ZBnd<P>): Const<P, in Number> =
    when (this) {
      is Const<*, *> -> this as Const<P, in Number>
      is XZ<*> -> (this as XZ<P>)(X, Z)
      is X<*> -> (this as X<P>)(X)
      is Z<*> -> (this as Z<P>)(Z)
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(X: XBnd<P>, Z: ZBnd<P>): Const<P, in Number> =
    app(left(X, Z) as P, right(X, Z) as P)


  operator fun Fnc<*>.invoke(X: XBnd<P>): Z<P> =
    when (this) {
      is XZ<*> -> (this as XZ<P>)(X)
      is X<*> -> Z((this as X<P>)(X))
      is Z<*> -> this as Z<P>
      else -> throw ClassNotFoundException(toString())
    }

  operator fun Fnc<*>.invoke(Z: ZBnd<P>): X<P> =
    when (this) {
      is XZ<*> -> (this as XZ<P>)(Z)
      is Z<*> -> X((this as Z<P>)(Z))
      is X<*> -> this as X<P>
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(X: XBnd<P>): Z<P> = Z(left(X), right(X), app)
  operator fun invoke(Z: ZBnd<P>): X<P> = X(left(Z), right(Z), app)
}

open class YZ<P : Const<P, in Number>>(val left: Fnc<*>,
                                       val right: Fnc<*>,
                                       val app: (P, P) -> P) : Fnc<YZ<P>>() {
  constructor(f: Fnc<*>) : this(f, f, ::firstX)
  constructor(c: Const<P, in Number>) : this(c, c, ::firstX)

  operator fun plus(that: X<P>): XYZ<P> = XYZ(this, that, ::plusX)
  operator fun plus(that: Z<P>): YZ<P> = YZ(this, that, ::plusX)
  operator fun plus(that: Y<P>): YZ<P> = YZ(this, that, ::plusX)
  operator fun plus(that: XY<P>): XYZ<P> = XYZ(this, that, ::plusX)
  operator fun plus(that: XZ<P>): XYZ<P> = XYZ(this, that, ::plusX)
  override operator fun plus(that: YZ<P>): YZ<P> = YZ(this, that, ::plusX)
  operator fun plus(that: XYZ<P>): XYZ<P> = XYZ(this, that, ::plusX)
  operator fun times(that: X<P>): XYZ<P> = XYZ(this, that, ::timesX)
  operator fun times(that: Z<P>): YZ<P> = YZ(this, that, ::timesX)
  operator fun times(that: Y<P>): YZ<P> = YZ(this, that, ::timesX)
  operator fun times(that: XY<P>): XYZ<P> = XYZ(this, that, ::timesX)
  operator fun times(that: XZ<P>): XYZ<P> = XYZ(this, that, ::timesX)
  override operator fun times(that: YZ<P>): YZ<P> = YZ(this, that, ::timesX)
  operator fun times(that: XYZ<P>): XYZ<P> = XYZ(this, that, ::timesX)

  operator fun Fnc<*>.invoke(Y: YBnd<P>, Z: ZBnd<P>): Const<P, in Number> =
    when (this) {
      is Const<*, *> -> this as Const<P, in Number>
      is YZ<*> -> (this as YZ<P>)(Y, Z)
      is Y<*> -> (this as Y<P>)(Y)
      is Z<*> -> (this as Z<P>)(Z)
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(Y: YBnd<P>, Z: ZBnd<P>): Const<P, in Number> =
    app(left(Y, Z) as P, right(Y, Z) as P)

  operator fun Fnc<*>.invoke(Y: YBnd<P>): Z<P> =
    when (this) {
      is YZ<*> -> (this as YZ<P>)(Y)
      is Y<*> -> Z((this as Y<P>)(Y))
      is Z<*> -> this as Z<P>
      else -> throw ClassNotFoundException(toString())
    }

  operator fun Fnc<*>.invoke(Z: ZBnd<P>): Y<P> =
    when (this) {
      is YZ<*> -> (this as YZ<P>)(Z)
      is Z<*> -> Y((this as Z<P>)(Z))
      is Y<*> -> this as Y<P>
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(Y: YBnd<P>): Z<P> =
    Z(left(Y), right(Y), app)

  operator fun invoke(Z: ZBnd<P>): Y<P> =
    Y(left(Z) as P, right(Z) as P, app)

}

open class XYZ<P : Const<P, in Number>>(val left: Fnc<*>,
                                        val right: Fnc<*>,
                                        val app: (P, P) -> P) : Fnc<XYZ<P>>() {
  operator fun plus(that: X<P>): XYZ<P> = XYZ(this, that, ::plusX)
  operator fun plus(that: Y<P>): XYZ<P> = XYZ(this, that, ::plusX)
  operator fun plus(that: Z<P>): XYZ<P> = XYZ(this, that, ::plusX)
  operator fun plus(that: XY<P>): XYZ<P> = XYZ(this, that, ::plusX)
  operator fun plus(that: XZ<P>): XYZ<P> = XYZ(this, that, ::plusX)
  operator fun plus(that: YZ<P>): XYZ<P> = XYZ(this, that, ::plusX)
  override operator fun plus(that: XYZ<P>): XYZ<P> = XYZ(this, that, ::plusX)
  operator fun times(that: X<P>): XYZ<P> = XYZ(this, that, ::timesX)
  operator fun times(that: Y<P>): XYZ<P> = XYZ(this, that, ::timesX)
  operator fun times(that: Z<P>): XYZ<P> = XYZ(this, that, ::timesX)
  operator fun times(that: XY<P>): XYZ<P> = XYZ(this, that, ::timesX)
  operator fun times(that: XZ<P>): XYZ<P> = XYZ(this, that, ::timesX)
  operator fun times(that: YZ<P>): XYZ<P> = XYZ(this, that, ::timesX)
  override operator fun times(that: XYZ<P>): XYZ<P> = XYZ(this, that, ::timesX)

  operator fun Fnc<*>.invoke(X: XBnd<P>, Y: YBnd<P>, Z: ZBnd<P>): Const<P, in Number> =
    when (this) {
      is Const<*, *> -> this as Const<P, in Number>
      is XYZ<*> -> (this as XYZ<P>)(X, Y, Z)
      is XY<*> -> (this as XY<P>)(X, Y)
      is XZ<*> -> (this as XZ<P>)(X, Z)
      is YZ<*> -> (this as YZ<P>)(Y, Z)
      is X<*> -> (this as X<P>)(X)
      is Y<*> -> (this as Y<P>)(Y)
      is Z<*> -> (this as Z<P>)(Z)
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(X: XBnd<P>, Y: YBnd<P>, Z: ZBnd<P>): Const<P, in Number> =
    app(left(X, Y, Z) as P, right(X, Y, Z) as P)

  operator fun Fnc<*>.invoke(X: XBnd<P>, Z: ZBnd<P>): Y<P> =
    when (this) {
      is XYZ<*> -> (this as XYZ<P>)(X, Z)
      is XY<*> -> (this as XY<P>)(X)
      is XZ<*> -> Y((this as XZ<P>)(X, Z))
      is YZ<*> -> (this as YZ<P>)(Z)
      is X<*> -> Y((this as X<P>)(X))
      is Y<*> -> this as Y<P>
      is Z<*> -> Y((this as Z<P>)(Z))
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(X: XBnd<P>, Z: ZBnd<P>): Y<P> =
    Y(left(X, Z), right(X, Z), app)

  operator fun Fnc<*>.invoke(X: XBnd<P>, Y: YBnd<P>): Z<P> =
    when (this) {
      is XYZ<*> -> (this as XYZ<P>)(X, Y)
      is XY<*> -> Z((this as XY<P>)(X, Y))
      is XZ<*> -> (this as XZ<P>)(X)
      is YZ<*> -> (this as YZ<P>)(Y)
      is X<*> -> Z((this as X<P>)(X))
      is Y<*> -> Z((this as Y<P>)(Y))
      is Z<*> -> this as Z<P>
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(X: XBnd<P>, Y: YBnd<P>): Z<P> =
    Z(left(X, Y), right(X, Y), app)

  operator fun Fnc<*>.invoke(Y: YBnd<P>, Z: ZBnd<P>): X<P> =
    when (this) {
      is XYZ<*> -> (this as XYZ<P>)(Y, Z)
      is XY<*> -> (this as XY<P>)(Y)
      is XZ<*> -> (this as XZ<P>)(Z)
      is YZ<*> -> X((this as YZ<P>)(Y, Z))
      is X<*> -> this as X<P>
      is Y<*> -> X((this as Y<P>)(Y))
      is Z<*> -> X((this as Z<P>)(Z))
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(Y: YBnd<P>, Z: ZBnd<P>): X<P> =
    X(left(Y, Z), right(Y, Z), app)

  operator fun Fnc<*>.invoke(X: XBnd<P>): YZ<P> =
    when (this) {
      is XYZ<*> -> (this as XYZ<P>)(X)
      is XY<*> -> YZ((this as XY<P>)(X))
      is XZ<*> -> YZ((this as XZ<P>)(X))
      is YZ<*> -> this as YZ<P>
      is X<*> -> YZ(X(X.const))
      is Y<*> -> YZ(this as Y<P>)
      is Z<*> -> YZ(this as Z<P>)
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(X: XBnd<P>): YZ<P> = YZ(left(X), right(X), app)
  operator fun Fnc<*>.invoke(Y: YBnd<P>): XZ<P> =
    when (this) {
      is XYZ<*> -> (this as XYZ<P>)(Y)
      is XY<*> -> XZ((this as XY<P>)(Y))
      is XZ<*> -> this as XZ<P>
      is YZ<*> -> XZ((this as YZ<P>)(Y))
      is X<*> -> XZ(this as X<P>)
      is Y<*> -> XZ(Y(Y.const))
      is Z<*> -> XZ(this as Z<P>)
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(Y: YBnd<P>): XZ<P> = XZ(left(Y), right(Y), app)
  operator fun Fnc<*>.invoke(Z: ZBnd<P>): XY<P> =
    when (this) {
      is XYZ<*> -> (this as XYZ<P>)(Z)
      is XY<*> -> this as XY<P>
      is XZ<*> -> XY((this as XZ<P>)(Z))
      is YZ<*> -> XY((this as YZ<P>)(Z))
      is X<*> -> XY(this as X<P>)
      is Y<*> -> XY(this as Y<P>)
      is Z<*> -> XY(Z(Z.const))
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(Z: ZBnd<P>): XY<P> = XY(left(Z), right(Z), app)
}

abstract class Fnc<T : Fnc<T>> {
  abstract infix operator fun plus(that: T): T
  abstract infix operator fun times(that: T): T
}

abstract class Const<T : Fnc<T>, Y : Number>(open val c: Y) : Fnc<T>() {
  override fun toString() = c.toString()
}

class DConst(override val c: Double) : Const<DConst, Number>(c) {
  override fun plus(that: DConst) = DConst(this.c + that.c)
  override fun times(that: DConst) = DConst(this.c * that.c)
}

class IConst(override val c: Int) : Const<IConst, Int>(c) {
  override fun plus(that: IConst) = IConst(this.c + that.c)
  override fun times(that: IConst) = IConst(this.c * that.c)
}

object Vrb : Fnc<Vrb>() {
  override operator fun plus(that: Vrb): Vrb = Vrb
  override operator fun times(that: Vrb): Vrb = Vrb
}

fun <T : Const<T, Y>, Y : Number> plusX(l: T, r: T) = l + r
val plus = { l: Double, r: Double -> l + r }
fun <T : Const<T, Y>, Y : Number> timesX(l: T, r: T) = l * r
val times = { l: Double, r: Double -> l * r }
fun <T : Const<T, Y>, Y : Number> firstX(l: T, r: T): T = l
val first = { l: Double, _: Double -> l }

sealed class Proto<T : Const<T, in Number>> {
  abstract fun wrap(default: Number): T

  operator fun Number.times(multiplicand: Fnc<T>) = multiplicand * wrap(this)
//  operator fun Fnc<T>.times(multiplicand: Number) = wrap(multiplicand) * this

  operator fun Number.plus(addend: Fnc<T>) = addend + wrap(this)
//  operator fun Fnc<T>.plus(addend: Number) = wrap(addend) + this

  abstract val X: X<T>
  abstract val Y: Y<T>
  abstract val Z: Z<T>
}

typealias DCN = Const<DConst, in Number>

object DProto : Proto<DConst>() {
  override fun wrap(default: Number): DConst = DConst(default.toDouble())
  override val X: X<DConst> = object : X<DConst>(Vrb, Vrb, ::firstX) {
    override fun invoke(X: XBnd<DConst>): Const<DConst, in Number> = X.const
  }
  override val Y: Y<DConst> = object : Y<DConst>(Vrb, Vrb, ::firstX) {
    override fun invoke(Y: YBnd<DConst>): Const<DConst, in Number> = Y.const
  }
  override val Z: Z<DConst> = object : Z<DConst>(Vrb, Vrb, ::firstX) {
    override fun invoke(Z: ZBnd<DConst>): Const<DConst, in Number> = Z.const
  }
  
  infix fun X<DConst>.to(d: Double) = XBnd(DConst(d))
  infix fun Y<DConst>.to(d: Double) = YBnd(DConst(d))
  infix fun Z<DConst>.to(d: Double) = ZBnd(DConst(d))
}

class XBnd<T: Const<T, Number>>(val const: T)
class YBnd<T: Const<T, Number>>(val const: T)
class ZBnd<T: Const<T, Number>>(val const: T)