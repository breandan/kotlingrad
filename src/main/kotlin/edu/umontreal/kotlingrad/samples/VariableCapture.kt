package edu.umontreal.kotlingrad.samples

fun main() {
  with(DoubleContext) {
    val q = X + Y + Z + Y + 0.0
    println("q = $q") // Should print above expression
    val totalApp = q(X to 1.0, Y to 2.0, Z to 3.0) // Name resolution
    println(totalApp) // Should be 8
    val partialApp = q(X to 1.0, Y to 1.0)(Z to 1.0) // Currying is possible
    println(partialApp) // Should be 4
    val partialApp2 = q(X to 1.0)(Y to 1.0, Z to 1.0) // Any arity is possible
    println(partialApp2) // Should be 4
    val partialApp3 = q(Z to 1.0)(X to 1.0, Y to 1.0) // Any order is possible
    println(partialApp3) // Should be 4

    val t = X + Z + Z + Y + 0.0
    val l = t(X to 1.0)(Z to 2.0)
    val r = t(X to 1.0)(Z to 2.0)(Y to 3.0) // Full currying

    val o = X + Z + 0.0
    //val k = o(Y to 4.0) // Does not compile

    val p = X + Y * Z + 0.0
    val totalApp2 = p(X to 1.0, Y to 2.0, Z to 3.0)
    println(totalApp2) // Should be 7
    val d = X + Z * X
    println(d(X to 3.0, Z to 4.0)) // Should be 15
    println((2.0 * d)(X to 3.0, Z to 4.0)) // Should be 30
  }
}

open class X<P: Const<P, in Number>>(override val left: BiFn<*>,
                                     override val right: BiFn<*>,
                                     override val op: Op<P>): BiFn<P>(left, right, op) {
  constructor(c: Const<P, in Number>): this(c, c, First())

  operator fun plus(that: P): X<P> = X(this, that, add)
  operator fun times(that: P): X<P> = X(this, that, mul)

  operator fun plus(that: X<P>): X<P> = X(this, that, add)
  operator fun plus(that: Y<P>): XY<P> = XY(this, that, add)
  operator fun plus(that: Z<P>): XZ<P> = XZ(this, that, add)
  operator fun plus(that: XY<P>): XY<P> = XY(this, that, add)
  operator fun plus(that: XZ<P>): XZ<P> = XZ(this, that, add)
  operator fun plus(that: YZ<P>): XYZ<P> = XYZ(this, that, add)
  operator fun plus(that: XYZ<P>): XYZ<P> = XYZ(this, that, add)
  operator fun times(that: X<P>): X<P> = X(this, that, mul)
  operator fun times(that: Y<P>): XY<P> = XY(this, that, mul)
  operator fun times(that: Z<P>): XZ<P> = XZ(this, that, mul)
  operator fun times(that: XY<P>): XY<P> = XY(this, that, mul)
  operator fun times(that: XZ<P>): XZ<P> = XZ(this, that, mul)
  operator fun times(that: YZ<P>): XYZ<P> = XYZ(this, that, mul)
  operator fun times(that: XYZ<P>): XYZ<P> = XYZ(this, that, mul)

  open operator fun invoke(X: XBnd<P>): P =
    op(left(X), right(X))

  private operator fun BiFn<*>.invoke(X: XBnd<P>): P =
    when (this) {
      is Const<*, *> -> this as P
      is X<*> -> (this as X<P>)(X)
      else -> throw IllegalStateException(toString())
    }
}

open class Y<P: Const<P, in Number>>(override val left: BiFn<*>,
                                     override val right: BiFn<*>,
                                     override val op: Op<P>): BiFn<P>(left, right, op) {
  constructor(c: Const<P, in Number>): this(c, c, First())

  operator fun plus(that: P): Y<P> = Y(this, that, add)
  operator fun times(that: P): Y<P> = Y(this, that, mul)

  operator fun plus(that: Y<P>): Y<P> = Y(this, that, add)
  operator fun plus(that: X<P>): XY<P> = XY(this, that, add)
  operator fun plus(that: Z<P>): YZ<P> = YZ(this, that, add)
  operator fun plus(that: XY<P>): XY<P> = XY(this, that, add)
  operator fun plus(that: XZ<P>): XYZ<P> = XYZ(this, that, add)
  operator fun plus(that: YZ<P>): YZ<P> = YZ(this, that, add)
  operator fun plus(that: XYZ<P>): XYZ<P> = XYZ(this, that, add)
  operator fun times(that: Y<P>): Y<P> = Y(this, that, mul)
  operator fun times(that: X<P>): XY<P> = XY(this, that, mul)
  operator fun times(that: Z<P>): YZ<P> = YZ(this, that, mul)
  operator fun times(that: XY<P>): XY<P> = XY(this, that, mul)
  operator fun times(that: XZ<P>): XYZ<P> = XYZ(this, that, mul)
  operator fun times(that: YZ<P>): YZ<P> = YZ(this, that, mul)
  operator fun times(that: XYZ<P>): XYZ<P> = XYZ(this, that, mul)

  open operator fun invoke(Y: YBnd<P>): P =
    op(left(Y), right(Y))

  private operator fun BiFn<*>.invoke(Y: YBnd<P>): P =
    when (this) {
      is Const<*, *> -> this as P
      is Y<*> -> (this as Y<P>)(Y)
      else -> throw IllegalStateException(toString())
    }
}

open class Z<P: Const<P, in Number>>(override val left: BiFn<*>,
                                     override val right: BiFn<*>,
                                     override val op: Op<P>): BiFn<P>(left, right, op) {
  constructor(c: Const<P, in Number>): this(c, c, First())

  operator fun plus(that: P): Z<P> = Z(this, that, add)
  operator fun times(that: P): Z<P> = Z(this, that, mul)

  operator fun plus(that: Z<P>): Z<P> = Z(this, that, add)
  operator fun plus(that: Y<P>): YZ<P> = YZ(this, that, add)
  operator fun plus(that: X<P>): XZ<P> = XZ(this, that, add)
  operator fun plus(that: XY<P>): XYZ<P> = XYZ(this, that, add)
  operator fun plus(that: XZ<P>): XZ<P> = XZ(this, that, add)
  operator fun plus(that: YZ<P>): YZ<P> = YZ(this, that, add)
  operator fun plus(that: XYZ<P>): XYZ<P> = XYZ(this, that, add)
  operator fun times(that: Z<P>): Z<P> = Z(this, that, mul)
  operator fun times(that: Y<P>): YZ<P> = YZ(this, that, mul)
  operator fun times(that: X<P>): XZ<P> = XZ(this, that, mul)
  operator fun times(that: XY<P>): XYZ<P> = XYZ(this, that, mul)
  operator fun times(that: XZ<P>): XZ<P> = XZ(this, that, mul)
  operator fun times(that: YZ<P>): YZ<P> = YZ(this, that, mul)
  operator fun times(that: XYZ<P>): XYZ<P> = XYZ(this, that, mul)

  open operator fun invoke(Z: ZBnd<P>): P = op(left(Z), right(Z))

  private operator fun BiFn<*>.invoke(Z: ZBnd<P>): P =
    when (this) {
      is Const<*, *> -> this as P
      is Z<*> -> (this as Z<P>)(Z)
      else -> throw IllegalStateException(toString())
    }
}

open class XY<P: Const<P, in Number>>(override val left: BiFn<*>,
                                      override val right: BiFn<*>,
                                      override val op: Op<P>): BiFn<P>(left, right, op) {
  constructor(f: BiFn<*>): this(f, f, First())

  operator fun plus(that: P): XY<P> = XY(this, that, add)
  operator fun times(that: P): XY<P> = XY(this, that, mul)

  operator fun plus(that: X<P>): XY<P> = XY(this, that, add)
  operator fun plus(that: Y<P>): XY<P> = XY(this, that, add)
  operator fun plus(that: Z<P>): XYZ<P> = XYZ(this, that, add)
  operator fun plus(that: XY<P>): XY<P> = XY(this, that, add)
  operator fun plus(that: XZ<P>): XYZ<P> = XYZ(this, that, add)
  operator fun plus(that: YZ<P>): XYZ<P> = XYZ(this, that, add)
  operator fun plus(that: XYZ<P>): XYZ<P> = XYZ(this, that, add)
  operator fun times(that: X<P>): XY<P> = XY(this, that, mul)
  operator fun times(that: Y<P>): XY<P> = XY(this, that, mul)
  operator fun times(that: Z<P>): XYZ<P> = XYZ(this, that, mul)
  operator fun times(that: XY<P>): XY<P> = XY(this, that, mul)
  operator fun times(that: XZ<P>): XYZ<P> = XYZ(this, that, mul)
  operator fun times(that: YZ<P>): XYZ<P> = XYZ(this, that, mul)
  operator fun times(that: XYZ<P>): XYZ<P> = XYZ(this, that, mul)

  private operator fun BiFn<*>.invoke(X: XBnd<P>, Y: YBnd<P>): P =
    when (this) {
      is Const<*, *> -> this as P
      is XY<*> -> (this as XY<P>)(X, Y)
      is X<*> -> (this as X<P>)(X)
      is Y<*> -> (this as Y<P>)(Y)
      else -> throw IllegalStateException(toString())
    }

  operator fun invoke(X: XBnd<P>, Y: YBnd<P>): P = op(left(X, Y), right(X, Y))

  private operator fun BiFn<*>.invoke(X: XBnd<P>): Y<P> =
    when (this) {
      is Const<*, *> -> Y(this as P)
      is XY<*> -> (this as XY<P>)(X)
      is X<*> -> Y((this as X<P>)(X))
      is Y<*> -> this as Y<P>
      else -> throw IllegalStateException(toString())
    }

  private operator fun BiFn<*>.invoke(Y: YBnd<P>): X<P> =
    when (this) {
      is Const<*, *> -> X(this as P)
      is XY<*> -> (this as XY<P>)(Y)
      is Y<*> -> X((this as Y<P>)(Y))
      is X<*> -> this as X<P>
      else -> throw IllegalStateException(toString())
    }

  operator fun invoke(X: XBnd<P>): Y<P> = Y(left(X), right(X), op)
  operator fun invoke(Y: YBnd<P>): X<P> = X(left(Y), right(Y), op)
}

open class XZ<P: Const<P, in Number>>(override val left: BiFn<*>,
                                      override val right: BiFn<*>,
                                      override val op: Op<P>): BiFn<P>(left, right, op) {
  constructor(f: BiFn<*>): this(f, f, First())

  operator fun plus(that: P): XZ<P> = XZ(this, that, add)
  operator fun times(that: P): XZ<P> = XZ(this, that, mul)

  operator fun plus(that: X<P>): XZ<P> = XZ(this, that, add)
  operator fun plus(that: Y<P>): XYZ<P> = XYZ(this, that, add)
  operator fun plus(that: Z<P>): XZ<P> = XZ(this, that, add)
  operator fun plus(that: XY<P>): XYZ<P> = XYZ(this, that, add)
  operator fun plus(that: XZ<P>): XZ<P> = XZ(this, that, add)
  operator fun plus(that: YZ<P>): XYZ<P> = XYZ(this, that, add)
  operator fun plus(that: XYZ<P>): XYZ<P> = XYZ(this, that, add)
  operator fun times(that: X<P>): XZ<P> = XZ(this, that, mul)
  operator fun times(that: Y<P>): XYZ<P> = XYZ(this, that, mul)
  operator fun times(that: Z<P>): XZ<P> = XZ(this, that, mul)
  operator fun times(that: XY<P>): XYZ<P> = XYZ(this, that, mul)
  operator fun times(that: XZ<P>): XZ<P> = XZ(this, that, mul)
  operator fun times(that: YZ<P>): XYZ<P> = XYZ(this, that, mul)
  operator fun times(that: XYZ<P>): XYZ<P> = XYZ(this, that, mul)

  private operator fun BiFn<*>.invoke(X: XBnd<P>, Z: ZBnd<P>): P =
    when (this) {
      is Const<*, *> -> this as P
      is XZ<*> -> (this as XZ<P>)(X, Z)
      is X<*> -> (this as X<P>)(X)
      is Z<*> -> (this as Z<P>)(Z)
      else -> throw IllegalStateException(toString())
    }

  operator fun invoke(X: XBnd<P>, Z: ZBnd<P>): P = op(left(X, Z), right(X, Z))

  private operator fun BiFn<*>.invoke(X: XBnd<P>): Z<P> =
    when (this) {
      is Const<*, *> -> Z(this as P)
      is XZ<*> -> (this as XZ<P>)(X)
      is X<*> -> Z((this as X<P>)(X))
      is Z<*> -> this as Z<P>
      else -> throw IllegalStateException(toString())
    }

  private operator fun BiFn<*>.invoke(Z: ZBnd<P>): X<P> =
    when (this) {
      is Const<*, *> -> X(this as P)
      is XZ<*> -> (this as XZ<P>)(Z)
      is Z<*> -> X((this as Z<P>)(Z))
      is X<*> -> this as X<P>
      else -> throw IllegalStateException(toString())
    }

  operator fun invoke(X: XBnd<P>): Z<P> = Z(left(X), right(X), op)
  operator fun invoke(Z: ZBnd<P>): X<P> = X(left(Z), right(Z), op)
}

open class YZ<P: Const<P, in Number>>(override val left: BiFn<*>,
                                      override val right: BiFn<*>,
                                      override val op: Op<P>): BiFn<P>(left, right, op) {
  constructor(f: BiFn<*>): this(f, f, First())

  operator fun plus(that: P): YZ<P> = YZ(this, that, add)
  operator fun times(that: P): YZ<P> = YZ(this, that, mul)

  operator fun plus(that: X<P>): XYZ<P> = XYZ(this, that, add)
  operator fun plus(that: Z<P>): YZ<P> = YZ(this, that, add)
  operator fun plus(that: Y<P>): YZ<P> = YZ(this, that, add)
  operator fun plus(that: XY<P>): XYZ<P> = XYZ(this, that, add)
  operator fun plus(that: XZ<P>): XYZ<P> = XYZ(this, that, add)
  operator fun plus(that: YZ<P>): YZ<P> = YZ(this, that, add)
  operator fun plus(that: XYZ<P>): XYZ<P> = XYZ(this, that, add)
  operator fun times(that: X<P>): XYZ<P> = XYZ(this, that, mul)
  operator fun times(that: Z<P>): YZ<P> = YZ(this, that, mul)
  operator fun times(that: Y<P>): YZ<P> = YZ(this, that, mul)
  operator fun times(that: XY<P>): XYZ<P> = XYZ(this, that, mul)
  operator fun times(that: XZ<P>): XYZ<P> = XYZ(this, that, mul)
  operator fun times(that: YZ<P>): YZ<P> = YZ(this, that, mul)
  operator fun times(that: XYZ<P>): XYZ<P> = XYZ(this, that, mul)

  private operator fun BiFn<*>.invoke(Y: YBnd<P>, Z: ZBnd<P>): P =
    when (this) {
      is Const<*, *> -> this as P
      is YZ<*> -> (this as YZ<P>)(Y, Z)
      is Y<*> -> (this as Y<P>)(Y)
      is Z<*> -> (this as Z<P>)(Z)
      else -> throw IllegalStateException(toString())
    }

  operator fun invoke(Y: YBnd<P>, Z: ZBnd<P>): P =
    op(left(Y, Z), right(Y, Z))

  private operator fun BiFn<*>.invoke(Y: YBnd<P>): Z<P> =
    when (this) {
      is Const<*, *> -> Z(this as P)
      is YZ<*> -> (this as YZ<P>)(Y)
      is Y<*> -> Z((this as Y<P>)(Y))
      is Z<*> -> this as Z<P>
      else -> throw IllegalStateException(toString())
    }

  private operator fun BiFn<*>.invoke(Z: ZBnd<P>): Y<P> =
    when (this) {
      is Const<*, *> -> Y(this as P)
      is YZ<*> -> (this as YZ<P>)(Z)
      is Z<*> -> Y((this as Z<P>)(Z))
      is Y<*> -> this as Y<P>
      else -> throw IllegalStateException(toString())
    }

  operator fun invoke(Y: YBnd<P>): Z<P> = Z(left(Y), right(Y), op)
  operator fun invoke(Z: ZBnd<P>): Y<P> = Y(left(Z), right(Z), op)
}

open class XYZ<P: Const<P, in Number>>(override val left: BiFn<*>,
                                       override val right: BiFn<*>,
                                       override val op: Op<P>): BiFn<P>(left, right, op) {
  operator fun plus(that: P): XYZ<P> = XYZ(this, that, add)
  operator fun times(that: P): XYZ<P> = XYZ(this, that, mul)

  operator fun plus(that: X<P>): XYZ<P> = XYZ(this, that, add)
  operator fun plus(that: Y<P>): XYZ<P> = XYZ(this, that, add)
  operator fun plus(that: Z<P>): XYZ<P> = XYZ(this, that, add)
  operator fun plus(that: XY<P>): XYZ<P> = XYZ(this, that, add)
  operator fun plus(that: XZ<P>): XYZ<P> = XYZ(this, that, add)
  operator fun plus(that: YZ<P>): XYZ<P> = XYZ(this, that, add)
  operator fun plus(that: XYZ<P>): XYZ<P> = XYZ(this, that, add)
  operator fun times(that: X<P>): XYZ<P> = XYZ(this, that, mul)
  operator fun times(that: Y<P>): XYZ<P> = XYZ(this, that, mul)
  operator fun times(that: Z<P>): XYZ<P> = XYZ(this, that, mul)
  operator fun times(that: XY<P>): XYZ<P> = XYZ(this, that, mul)
  operator fun times(that: XZ<P>): XYZ<P> = XYZ(this, that, mul)
  operator fun times(that: YZ<P>): XYZ<P> = XYZ(this, that, mul)
  operator fun times(that: XYZ<P>): XYZ<P> = XYZ(this, that, mul)

  private operator fun BiFn<*>.invoke(X: XBnd<P>, Y: YBnd<P>, Z: ZBnd<P>): P =
    when (this) {
      is Const<*, *> -> this as P
      is XYZ<*> -> (this as XYZ<P>)(X, Y, Z)
      is XY<*> -> (this as XY<P>)(X, Y)
      is XZ<*> -> (this as XZ<P>)(X, Z)
      is YZ<*> -> (this as YZ<P>)(Y, Z)
      is X<*> -> (this as X<P>)(X)
      is Y<*> -> (this as Y<P>)(Y)
      is Z<*> -> (this as Z<P>)(Z)
      else -> throw IllegalStateException(toString())
    }

  operator fun invoke(X: XBnd<P>, Y: YBnd<P>, Z: ZBnd<P>): P =
    op(left(X, Y, Z), right(X, Y, Z))

  private operator fun BiFn<*>.invoke(X: XBnd<P>, Z: ZBnd<P>): Y<P> =
    when (this) {
      is Const<*, *> -> Y(this as P)
      is XYZ<*> -> (this as XYZ<P>)(X, Z)
      is XY<*> -> (this as XY<P>)(X)
      is XZ<*> -> Y((this as XZ<P>)(X, Z))
      is YZ<*> -> (this as YZ<P>)(Z)
      is X<*> -> Y((this as X<P>)(X))
      is Y<*> -> this as Y<P>
      is Z<*> -> Y((this as Z<P>)(Z))
      else -> throw IllegalStateException(toString())
    }

  operator fun invoke(X: XBnd<P>, Z: ZBnd<P>): Y<P> =
    Y(left(X, Z), right(X, Z), op)

  private operator fun BiFn<*>.invoke(X: XBnd<P>, Y: YBnd<P>): Z<P> =
    when (this) {
      is Const<*, *> -> Z(this as P)
      is XYZ<*> -> (this as XYZ<P>)(X, Y)
      is XY<*> -> Z((this as XY<P>)(X, Y))
      is XZ<*> -> (this as XZ<P>)(X)
      is YZ<*> -> (this as YZ<P>)(Y)
      is X<*> -> Z((this as X<P>)(X))
      is Y<*> -> Z((this as Y<P>)(Y))
      is Z<*> -> this as Z<P>
      else -> throw IllegalStateException(toString())
    }

  operator fun invoke(X: XBnd<P>, Y: YBnd<P>): Z<P> =
    Z(left(X, Y), right(X, Y), op)

  private operator fun BiFn<*>.invoke(Y: YBnd<P>, Z: ZBnd<P>): X<P> =
    when (this) {
      is Const<*, *> -> X(this as P)
      is XYZ<*> -> (this as XYZ<P>)(Y, Z)
      is XY<*> -> (this as XY<P>)(Y)
      is XZ<*> -> (this as XZ<P>)(Z)
      is YZ<*> -> X((this as YZ<P>)(Y, Z))
      is X<*> -> this as X<P>
      is Y<*> -> X((this as Y<P>)(Y))
      is Z<*> -> X((this as Z<P>)(Z))
      else -> throw IllegalStateException(toString())
    }

  operator fun invoke(Y: YBnd<P>, Z: ZBnd<P>): X<P> =
    X(left(Y, Z), right(Y, Z), op)

  private operator fun BiFn<*>.invoke(X: XBnd<P>): YZ<P> =
    when (this) {
      is Const<*, *> -> YZ(this as P)
      is XYZ<*> -> (this as XYZ<P>)(X)
      is XY<*> -> YZ((this as XY<P>)(X))
      is XZ<*> -> YZ((this as XZ<P>)(X))
      is YZ<*> -> this as YZ<P>
      is X<*> -> YZ(X.const)
      is Y<*> -> YZ(this as Y<P>)
      is Z<*> -> YZ(this as Z<P>)
      else -> throw IllegalStateException(toString())
    }

  operator fun invoke(X: XBnd<P>): YZ<P> = YZ(left(X), right(X), op)

  private operator fun BiFn<*>.invoke(Y: YBnd<P>): XZ<P> =
    when (this) {
      is Const<*, *> -> XZ(this as P)
      is XYZ<*> -> (this as XYZ<P>)(Y)
      is XY<*> -> XZ((this as XY<P>)(Y))
      is XZ<*> -> this as XZ<P>
      is YZ<*> -> XZ((this as YZ<P>)(Y))
      is X<*> -> XZ(this as X<P>)
      is Y<*> -> XZ(Y.const)
      is Z<*> -> XZ(this as Z<P>)
      else -> throw IllegalStateException(toString())
    }

  operator fun invoke(Y: YBnd<P>): XZ<P> = XZ(left(Y), right(Y), op)

  private operator fun BiFn<*>.invoke(Z: ZBnd<P>): XY<P> =
    when (this) {
      is Const<*, *> -> XY(this as P)
      is XYZ<*> -> (this as XYZ<P>)(Z)
      is XY<*> -> this as XY<P>
      is XZ<*> -> XY((this as XZ<P>)(Z))
      is YZ<*> -> XY((this as YZ<P>)(Z))
      is X<*> -> XY(this as X<P>)
      is Y<*> -> XY(this as Y<P>)
      is Z<*> -> XY(Z.const)
      else -> throw IllegalStateException(toString())
    }

  operator fun invoke(Z: ZBnd<P>): XY<P> = XY(left(Z), right(Z), op)
}

abstract class BiFn<T: Const<T, Number>>(open val left: BiFn<*>? = null,
                                         open val right: BiFn<*>? = null,
                                         open val op: Op<*>? = null) {
  val add: Add<T> by lazy{ Add() }
  val mul: Mul<T> by lazy{ Mul() }

  override fun toString() = "$left $op $right"
}

abstract class Const<T: Const<T, Number>, Y: Number>(internal open val c: Y): BiFn<T>() {
  override fun toString() = c.toString()
  abstract operator fun plus(that: T): T
  abstract operator fun times(that: T): T
}

class DConst(override val c: Double): Const<DConst, Number>(c) {
  override fun plus(that: DConst) = DConst(this.c + that.c)
  override fun times(that: DConst) = DConst(this.c * that.c)
}

class IConst(override val c: Int): Const<IConst, Number>(c) {
  override fun plus(that: IConst) = IConst(this.c + that.c)
  override fun times(that: IConst) = IConst(this.c * that.c)
}

abstract class Op<T: Const<T, in Number>>(val string: String): (T, T) -> T {
  override fun toString() = string
}

class Add<T: Const<T, in Number>>: Op<T>("+") {
  override fun invoke(l: T, r: T): T = l + r
}

class Mul<T: Const<T, in Number>>: Op<T>("*") {
  override fun invoke(l: T, r: T): T = l * r
}

class First<T: Const<T, in Number>>: Op<T>("") {
  override fun invoke(l: T, r: T): T = l
}

sealed class Proto<T: Const<T, in Number>, Q: Number> {
  abstract fun wrap(default: Number): T

  abstract val X: X<T>
  abstract val Y: Y<T>
  abstract val Z: Z<T>

  operator fun X<T>.plus(c: Q): X<T> = this + wrap(c)
  operator fun Y<T>.plus(c: Q): Y<T> = this + wrap(c)
  operator fun Z<T>.plus(c: Q): Z<T> = this + wrap(c)
  operator fun XY<T>.plus(c: Q): XY<T> = this + wrap(c)
  operator fun XZ<T>.plus(c: Q): XZ<T> = this + wrap(c)
  operator fun YZ<T>.plus(c: Q): YZ<T> = this + wrap(c)
  operator fun XYZ<T>.plus(c: Q): XYZ<T> = this + wrap(c)

  operator fun X<T>.times(c: Q): X<T> = this * wrap(c)
  operator fun Y<T>.times(c: Q): Y<T> = this * wrap(c)
  operator fun Z<T>.times(c: Q): Z<T> = this * wrap(c)
  operator fun XY<T>.times(c: Q): XY<T> = this * wrap(c)
  operator fun XZ<T>.times(c: Q): XZ<T> = this * wrap(c)
  operator fun YZ<T>.times(c: Q): YZ<T> = this * wrap(c)
  operator fun XYZ<T>.times(c: Q): XYZ<T> = this * wrap(c)

  // TODO: Make these order-preserving to support non-commutative algebras
  operator fun Q.plus(c: X<T>): X<T> = c + wrap(this)
  operator fun Q.plus(c: Y<T>): Y<T> = c + wrap(this)
  operator fun Q.plus(c: Z<T>): Z<T> = c + wrap(this)
  operator fun Q.plus(c: XY<T>): XY<T> = c + wrap(this)
  operator fun Q.plus(c: XZ<T>): XZ<T> = c + wrap(this)
  operator fun Q.plus(c: YZ<T>): YZ<T> = c + wrap(this)
  operator fun Q.plus(c: XYZ<T>): XYZ<T> = c + wrap(this)

  operator fun Q.times(c: X<T>): X<T> = c * wrap(this)
  operator fun Q.times(c: Y<T>): Y<T> = c * wrap(this)
  operator fun Q.times(c: Z<T>): Z<T> = c * wrap(this)
  operator fun Q.times(c: XY<T>): XY<T> = c * wrap(this)
  operator fun Q.times(c: XZ<T>): XZ<T> = c * wrap(this)
  operator fun Q.times(c: YZ<T>): YZ<T> = c * wrap(this)
  operator fun Q.times(c: XYZ<T>): XYZ<T> = c * wrap(this)

  abstract infix fun X<T>.to(c: Q): XBnd<T>
  abstract infix fun Y<T>.to(c: Q): YBnd<T>
  abstract infix fun Z<T>.to(c: Q): ZBnd<T>
  abstract val Const<T, Number>.value: Q
}

object DoubleContext: Proto<DConst, Double>() {
  override val Const<DConst, Number>.value: Double
    get() = c.toDouble()

  override fun wrap(default: Number): DConst = DConst(default.toDouble())
  override val X: X<DConst> = object: X<DConst>(DConst(0.0), DConst(0.0), First()) {
    override fun invoke(X: XBnd<DConst>): DConst = X.const
    override fun toString() = "X"
  }
  override val Y: Y<DConst> = object: Y<DConst>(DConst(0.0), DConst(0.0), First()) {
    override fun invoke(Y: YBnd<DConst>): DConst = Y.const
    override fun toString() = "Y"
  }
  override val Z: Z<DConst> = object: Z<DConst>(DConst(0.0), DConst(0.0), First()) {
    override fun invoke(Z: ZBnd<DConst>): DConst = Z.const
    override fun toString() = "Z"
  }

  override infix fun X<DConst>.to(c: Double) = XBnd(DConst(c))
  override infix fun Y<DConst>.to(c: Double) = YBnd(DConst(c))
  override infix fun Z<DConst>.to(c: Double) = ZBnd(DConst(c))
}

object IntegerContext: Proto<IConst, Int>() {
  override val Const<IConst, Number>.value: Int
    get() = c.toInt()

  override fun wrap(default: Number): IConst = IConst(default.toInt())
  override val X: X<IConst> = object: X<IConst>(IConst(0), IConst(0), First()) {
    override fun invoke(X: XBnd<IConst>): IConst = X.const
    override fun toString() = "X"
  }
  override val Y: Y<IConst> = object: Y<IConst>(IConst(0), IConst(0), First()) {
    override fun invoke(Y: YBnd<IConst>): IConst = Y.const
    override fun toString() = "Y"
  }
  override val Z: Z<IConst> = object: Z<IConst>(IConst(0), IConst(0), First()) {
    override fun invoke(Z: ZBnd<IConst>): IConst = Z.const
    override fun toString() = "Z"
  }

  override infix fun X<IConst>.to(c: Int) = XBnd(IConst(c))
  override infix fun Y<IConst>.to(c: Int) = YBnd(IConst(c))
  override infix fun Z<IConst>.to(c: Int) = ZBnd(IConst(c))
}

class XBnd<T: Const<T, in Number>>(val const: T)
class YBnd<T: Const<T, in Number>>(val const: T)
class ZBnd<T: Const<T, in Number>>(val const: T)