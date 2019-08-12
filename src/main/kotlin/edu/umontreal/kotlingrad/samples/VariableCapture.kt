package edu.umontreal.kotlingrad.samples

fun main() {
  with(DProto) {
    val q = X + Y + Z + Y
    val shouldBe8: Double = q(X = 1.0, Y = 2.0, Z = 3.0)
    println(shouldBe8)
    val shouldBe4: Double = q(X to 1.0, Y to 1.0)(1.0)
    println(shouldBe4)
    val shouldBe4Again: Double = q(X to 1.0)(Y = 1.0, Z = 1.0)
    println(shouldBe4Again)
    val t = X + Z + Z
    val l = t(X to 1.0)
    val p = X + Y * Z
    val shouldBe7 = p(X = 1.0, Y = 2.0, Z = 3.0)
    println(shouldBe7)
    val d = X + Z + X
  }
}

open class X<P : Const<P, Q>, Q : Number>(val left: Fnc<*>,
                                          val right: Fnc<*>,
                                          val app: (P, P) -> P) : Fnc<X<P, Q>>() {
  constructor(c: Const<P, Q>) : this(c, c, ::firstX)

  override operator fun plus(that: X<P, Q>): X<P, Q> = X(this, that, ::plusX)
  operator fun plus(that: Y<P, Q>): XY<P, Q> = XY<P, Q>(this, that, ::plusX)
  operator fun plus(that: Z<P, Q>): XZ<P, Q> = XZ<P, Q>(this, that, ::plusX)
  operator fun plus(that: XY<P, Q>): XY<P, Q> = XY<P, Q>(this, that, ::plusX)
  operator fun plus(that: XZ<P, Q>): XZ<P, Q> = XZ<P, Q>(this, that, ::plusX)
  operator fun plus(that: YZ<P, Q>): XYZ<P, Q> = XYZ<P, Q>(this, that, ::plusX)
  operator fun plus(that: XYZ<P, Q>): XYZ<P, Q> = XYZ<P, Q>(this, that, ::plusX)
  override operator fun times(that: X<P, Q>): X<P, Q> = X<P, Q>(this, that, ::timesX)
  operator fun times(that: Y<P, Q>): XY<P, Q> = XY<P, Q>(this, that, ::timesX)
  operator fun times(that: Z<P, Q>): XZ<P, Q> = XZ<P, Q>(this, that, ::timesX)
  operator fun times(that: XY<P, Q>): XY<P, Q> = XY<P, Q>(this, that, ::timesX)
  operator fun times(that: XZ<P, Q>): XZ<P, Q> = XZ<P, Q>(this, that, ::timesX)
  operator fun times(that: YZ<P, Q>): XYZ<P, Q> = XYZ<P, Q>(this, that, ::timesX)
  operator fun times(that: XYZ<P, Q>): XYZ<P, Q> = XYZ<P, Q>(this, that, ::timesX)

  open operator fun invoke(X: Const<P, Q>): Const<P, Q> =
    app(left(X) as P, right(X) as P)

  operator fun Fnc<*>.invoke(X: Const<P, Q>): Const<P, Q> =
    when (this) {
      is Const<*, *> -> this as Const<P, Q>
      is X<*, *> -> (this as X<P, Q>)(X)
      else -> throw ClassNotFoundException(this.toString())
    }
}

open class Y<P : Const<P, Q>, Q : Number>(val left: Fnc<*>,
                                          val right: Fnc<*>,
                                          val app: (P, P) -> P) : Fnc<Y<P, Q>>() {
  constructor(c: Const<P, Q>) : this(c, c, ::firstX)

  override operator fun plus(that: Y<P, Q>): Y<P, Q> = Y(this, that, ::plusX)
  operator fun plus(that: X<P, Q>): XY<P, Q> = XY(this, that, ::plusX)
  operator fun plus(that: Z<P, Q>): YZ<P, Q> = YZ(this, that, ::plusX)
  operator fun plus(that: XY<P, Q>): XY<P, Q> = XY(this, that, ::plusX)
  operator fun plus(that: XZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  operator fun plus(that: YZ<P, Q>): YZ<P, Q> = YZ(this, that, ::plusX)
  operator fun plus(that: XYZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  override operator fun times(that: Y<P, Q>): Y<P, Q> = Y(this, that, ::timesX)
  operator fun times(that: X<P, Q>): XY<P, Q> = XY(this, that, ::timesX)
  operator fun times(that: Z<P, Q>): YZ<P, Q> = YZ(this, that, ::timesX)
  operator fun times(that: XY<P, Q>): XY<P, Q> = XY(this, that, ::timesX)
  operator fun times(that: XZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)
  operator fun times(that: YZ<P, Q>): YZ<P, Q> = YZ(this, that, ::timesX)
  operator fun times(that: XYZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)

  open operator fun invoke(Y: Const<P, Q>): Const<P, Q> =
    app(left(Y) as P, right(Y) as P)

  operator fun Fnc<*>.invoke(Y: Const<P, Q>): Const<P, Q> =
    when (this) {
      is Const<*, *> -> this as Const<P, Q>
      is Y<*, *> -> (this as Y<P, Q>)(Y)
      else -> throw ClassNotFoundException(this.toString())
    }
}

open class Z<P : Const<P, Q>, Q : Number>(val left: Fnc<*>,
                                          val right: Fnc<*>,
                                          val app: (P, P) -> P) : Fnc<Z<P, Q>>() {
  constructor(c: Const<P, Q>) : this(c, c, ::firstX)

  override operator fun plus(that: Z<P, Q>): Z<P, Q> = Z(this, that, ::plusX)
  operator fun plus(that: Y<P, Q>): YZ<P, Q> = YZ(this, that, ::plusX)
  operator fun plus(that: X<P, Q>): XZ<P, Q> = XZ(this, that, ::plusX)
  operator fun plus(that: XY<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  operator fun plus(that: XZ<P, Q>): XZ<P, Q> = XZ(this, that, ::plusX)
  operator fun plus(that: YZ<P, Q>): YZ<P, Q> = YZ(this, that, ::plusX)
  operator fun plus(that: XYZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  override operator fun times(that: Z<P, Q>): Z<P, Q> = Z(this, that, ::timesX)
  operator fun times(that: Y<P, Q>): YZ<P, Q> = YZ(this, that, ::timesX)
  operator fun times(that: X<P, Q>): XZ<P, Q> = XZ(this, that, ::timesX)
  operator fun times(that: XY<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)
  operator fun times(that: XZ<P, Q>): XZ<P, Q> = XZ(this, that, ::timesX)
  operator fun times(that: YZ<P, Q>): YZ<P, Q> = YZ(this, that, ::timesX)
  operator fun times(that: XYZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)

  open operator fun invoke(Z: Const<P, Q>): Const<P, Q> =
    app(left(Z) as P, right(Z) as P)

  operator fun Fnc<*>.invoke(Z: Const<P, Q>): Const<P, Q> =
    when (this) {
      is Const<*, *> -> this as Const<P, Q>
      is Z<*, *> -> (this as Z<P, Q>)(Z)
      else -> throw ClassNotFoundException(this.toString())
    }
}

open class XY<P : Const<P, Q>, Q : Number>(val left: Fnc<*>,
                                           val right: Fnc<*>,
                                           val app: (P, P) -> P) : Fnc<XY<P, Q>>() {
  constructor(f: Fnc<*>) : this(f, f, ::firstX)
  constructor(c: Const<P, Q>) : this(c, c, ::firstX)

  operator fun plus(that: X<P, Q>): XY<P, Q> = XY(this, that, ::plusX)
  operator fun plus(that: Y<P, Q>): XY<P, Q> = XY(this, that, ::plusX)
  operator fun plus(that: Z<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  override operator fun plus(that: XY<P, Q>): XY<P, Q> = XY(this, that, ::plusX)
  operator fun plus(that: XZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  operator fun plus(that: YZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  operator fun plus(that: XYZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  operator fun times(that: X<P, Q>): XY<P, Q> = XY(this, that, ::timesX)
  operator fun times(that: Y<P, Q>): XY<P, Q> = XY(this, that, ::timesX)
  operator fun times(that: Z<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)
  override operator fun times(that: XY<P, Q>): XY<P, Q> = XY(this, that, ::timesX)
  operator fun times(that: XZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)
  operator fun times(that: YZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)
  operator fun times(that: XYZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)

  operator fun Fnc<*>.invoke(X: Const<P, Q>, Y: Const<P, Q>): Const<P, Q> =
    when (this) {
      is Const<*, *> -> this as Const<P, Q>
      is XY<*, *> -> (this as XY<P, Q>).invoke(X, Y)
      is X<*, *> -> (this as X<P, Q>)(X)
      is Y<*, *> -> (this as Y<P, Q>)(Y)
      else -> throw ClassNotFoundException(this.toString())
    }

  operator fun invoke(X: Const<P, Q>, Y: Const<P, Q>): Const<P, Q> =
    app(left(X, Y) as P, right(X, Y) as P)

  operator fun invoke(X: X<P, Q>, XP: Const<P, Q>, Y: Y<P, Q>, YP: Const<P, Q>) =
    app(left(XP, YP) as P, right(XP, YP) as P)

  operator fun Fnc<*>.invoke(X: X<P, Q>, XP: Const<P, Q>): Y<P, Q> =
    when (this) {
      is XY<*, *> -> (this as XY<P, Q>)(X, XP)
      is X<*, *> -> Y((this as X<P, Q>)(XP))
      is Y<*, *> -> this as Y<P, Q>
      else -> throw ClassNotFoundException(this.toString())
    }

  operator fun Fnc<*>.invoke(Y: Y<P, Q>, YP: Const<P, Q>): X<P, Q> =
    when (this) {
      is XY<*, *> -> (this as XY<P, Q>)(Y, YP)
      is Y<*, *> -> X((this as Y<P, Q>)(YP))
      is X<*, *> -> this as X<P, Q>
      else -> throw ClassNotFoundException(this.toString())
    }

  operator fun invoke(X: X<P, Q>, XP: Const<P, Q>): Y<P, Q> = Y(left(X, XP), right(X, XP), app)
  operator fun invoke(Y: Y<P, Q>, YP: Const<P, Q>): X<P, Q> = X(left(Y, YP), right(Y, YP), app)
}

open class XZ<P : Const<P, Q>, Q : Number>(val left: Fnc<*>,
                                           val right: Fnc<*>,
                                           val app: (P, P) -> P) : Fnc<XZ<P, Q>>() {
  constructor(f: Fnc<*>) : this(f, f, ::firstX)
  constructor(c: Const<P, Q>) : this(c, c, ::firstX)

  operator fun plus(that: X<P, Q>): XZ<P, Q> = XZ(this, that, ::plusX)
  operator fun plus(that: Y<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  operator fun plus(that: Z<P, Q>): XZ<P, Q> = XZ(this, that, ::plusX)
  operator fun plus(that: XY<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  override operator fun plus(that: XZ<P, Q>): XZ<P, Q> = XZ(this, that, ::plusX)
  operator fun plus(that: YZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  operator fun plus(that: XYZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  operator fun times(that: X<P, Q>): XZ<P, Q> = XZ(this, that, ::timesX)
  operator fun times(that: Y<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)
  operator fun times(that: Z<P, Q>): XZ<P, Q> = XZ(this, that, ::timesX)
  operator fun times(that: XY<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)
  override operator fun times(that: XZ<P, Q>): XZ<P, Q> = XZ(this, that, ::timesX)
  operator fun times(that: YZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)
  operator fun times(that: XYZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)

  operator fun Fnc<*>.invoke(X: Const<P, Q>, Z: Const<P, Q>): Const<P, Q> =
    when (this) {
      is Const<*, *> -> this as Const<P, Q>
      is XZ<*, *> -> (this as XZ<P, Q>)(X, Z)
      is X<*, *> -> (this as X<P, Q>)(X)
      is Z<*, *> -> (this as Z<P, Q>)(Z)
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(X: Const<P, Q>, Z: Const<P, Q>): Const<P, Q> =
    app(left(X, Z) as P, right(X, Z) as P)

  operator fun invoke(X: X<P, Q>, XP: Const<P, Q>, Z: Z<P, Q>, ZP: Const<P, Q>) =
    app(left(XP, ZP) as P, right(XP, ZP) as P)

  operator fun Fnc<*>.invoke(X: X<P, Q>, XP: Const<P, Q>): Z<P, Q> =
    when (this) {
      is XZ<*, *> -> (this as XZ<P, Q>)(X, XP)
      is X<*, *> -> Z((this as X<P, Q>)(XP))
      is Z<*, *> -> this as Z<P, Q>
      else -> throw ClassNotFoundException(toString())
    }

  operator fun Fnc<*>.invoke(Z: Z<P, Q>, ZP: Const<P, Q>): X<P, Q> =
    when (this) {
      is XZ<*, *> -> (this as XZ<P, Q>)(Z, ZP)
      is Z<*, *> -> X((this as Z<P, Q>)(ZP))
      is X<*, *> -> this as X<P, Q>
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(X: X<P, Q>, XP: Const<P, Q>): Z<P, Q> = Z(left(X, XP), right(X, XP), app)
  operator fun invoke(Z: Z<P, Q>, ZP: Const<P, Q>): X<P, Q> = X(left(Z, ZP), right(Z, ZP), app)
}

open class YZ<P : Const<P, Q>, Q : Number>(val left: Fnc<*>,
                                           val right: Fnc<*>,
                                           val app: (P, P) -> P) : Fnc<YZ<P, Q>>() {
  constructor(f: Fnc<*>) : this(f, f, ::firstX)
  constructor(c: Const<P, Q>) : this(c, c, ::firstX)

  operator fun plus(that: X<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  operator fun plus(that: Z<P, Q>): YZ<P, Q> = YZ(this, that, ::plusX)
  operator fun plus(that: Y<P, Q>): YZ<P, Q> = YZ(this, that, ::plusX)
  operator fun plus(that: XY<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  operator fun plus(that: XZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  override operator fun plus(that: YZ<P, Q>): YZ<P, Q> = YZ(this, that, ::plusX)
  operator fun plus(that: XYZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  operator fun times(that: X<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)
  operator fun times(that: Z<P, Q>): YZ<P, Q> = YZ(this, that, ::timesX)
  operator fun times(that: Y<P, Q>): YZ<P, Q> = YZ(this, that, ::timesX)
  operator fun times(that: XY<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)
  operator fun times(that: XZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)
  override operator fun times(that: YZ<P, Q>): YZ<P, Q> = YZ(this, that, ::timesX)
  operator fun times(that: XYZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)

  operator fun Fnc<*>.invoke(Y: Const<P, Q>, Z: Const<P, Q>): Const<P, Q> =
    when (this) {
      is Const<*, *> -> this as Const<P, Q>
      is YZ<*, *> -> (this as YZ<P, Q>)(Y, Z)
      is Y<*, *> -> (this as Y<P, Q>)(Y)
      is Z<*, *> -> (this as Z<P, Q>)(Z)
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(Y: Const<P, Q>, Z: Const<P, Q>): Const<P, Q> =
    app(left(Y, Z) as P, right(Y, Z) as P)

  operator fun invoke(Y: Y<P, Q>, YP: Const<P, Q>, Z: Z<P, Q>, ZP: Const<P, Q>) =
    app(left(YP, ZP) as P, right(YP, ZP) as P)

  operator fun Fnc<*>.invoke(Y: Y<P, Q>, YP: Const<P, Q>): Z<P, Q> =
    when (this) {
      is YZ<*, *> -> (this as YZ<P, Q>)(Y, YP)
      is Y<*, *> -> Z((this as Y<P, Q>)(YP))
      is Z<*, *> -> this as Z<P, Q>
      else -> throw ClassNotFoundException(toString())
    }

  operator fun Fnc<*>.invoke(Z: Z<P, Q>, ZP: Const<P, Q>): Y<P, Q> =
    when (this) {
      is YZ<*, *> -> (this as YZ<P, Q>)(Z, ZP)
      is Z<*, *> -> Y((this as Z<P, Q>)(ZP))
      is Y<*, *> -> this as Y<P, Q>
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(Y: Y<P, Q>, YP: Const<P, Q>): Z<P, Q> =
    Z(left(Y, YP), right(Y, YP), app)

  operator fun invoke(Z: Z<P, Q>, ZP: Const<P, Q>): Y<P, Q> =
    Y(left(Z, ZP) as P, right(Z, ZP) as P, app)

}

open class XYZ<P : Const<P, Q>, Q : Number>(val left: Fnc<*>,
                                            val right: Fnc<*>,
                                            val app: (P, P) -> P) : Fnc<XYZ<P, Q>>() {
  operator fun plus(that: X<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  operator fun plus(that: Y<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  operator fun plus(that: Z<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  operator fun plus(that: XY<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  operator fun plus(that: XZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  operator fun plus(that: YZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  override operator fun plus(that: XYZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::plusX)
  operator fun times(that: X<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)
  operator fun times(that: Y<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)
  operator fun times(that: Z<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)
  operator fun times(that: XY<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)
  operator fun times(that: XZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)
  operator fun times(that: YZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)
  override operator fun times(that: XYZ<P, Q>): XYZ<P, Q> = XYZ(this, that, ::timesX)

  operator fun Fnc<*>.invoke(X: Const<P, Q>, Y: Const<P, Q>, Z: Const<P, Q>): Const<P, Q> =
    when (this) {
      is Const<*, *> -> this as Const<P, Q>
      is XYZ<*, *> -> (this as XYZ<P, Q>)(X, Y, Z)
      is XY<*, *> -> (this as XY<P, Q>)(X, Y)
      is XZ<*, *> -> (this as XZ<P, Q>)(X, Z)
      is YZ<*, *> -> (this as YZ<P, Q>)(Y, Z)
      is X<*, *> -> (this as X<P, Q>)(X)
      is Y<*, *> -> (this as Y<P, Q>)(Y)
      is Z<*, *> -> (this as Z<P, Q>)(Z)
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(X: Const<P, Q>, Y: Const<P, Q>, Z: Const<P, Q>): Const<P, Q> =
    app(left(X, Y, Z) as P, right(X, Y, Z) as P)

  operator fun Fnc<*>.invoke(X: X<P, Q>, XP: Const<P, Q>, Z: Z<P, Q>, ZP: Const<P, Q>): Y<P, Q> =
    when (this) {
      is XYZ<*, *> -> (this as XYZ<P, Q>)(X, XP, Z, ZP)
      is XY<*, *> -> (this as XY<P, Q>)(X, XP)
      is XZ<*, *> -> Y((this as XZ<P, Q>)(X, XP, Z, ZP))
      is YZ<*, *> -> (this as YZ<P, Q>)(Z, ZP)
      is X<*, *> -> Y((this as Y<P, Q>)(XP))
      is Y<*, *> -> this as Y<P, Q>
      is Z<*, *> -> Y((this as Z<P, Q>)(ZP))
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(X: X<P, Q>, XP: Const<P, Q>, Z: Z<P, Q>, ZP: Const<P, Q>): Y<P, Q> =
    Y(left(X, XP, Z, ZP), right(X, XP, Z, ZP), app)

  operator fun Fnc<*>.invoke(X: X<P, Q>, XP: Const<P, Q>, Y: Y<P, Q>, YP: Const<P, Q>): Z<P, Q> =
    when (this) {
      is XYZ<*, *> -> (this as XYZ<P, Q>)(X, XP, Y, YP)
      is XY<*, *> -> Z((this as XY<P, Q>)(X, XP, Y, YP))
      is XZ<*, *> -> (this as XZ<P, Q>)(X, XP)
      is YZ<*, *> -> (this as YZ<P, Q>)(Y, YP)
      is X<*, *> -> Z((this as X<P, Q>)(XP))
      is Y<*, *> -> Z((this as Y<P, Q>)(YP))
      is Z<*, *> -> this as Z<P, Q>
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(X: X<P, Q>, XP: Const<P, Q>, Y: Y<P, Q>, YP: Const<P, Q>): Z<P, Q> =
    Z(left(X, XP, Y, YP), right(X, XP, Y, YP), app)

  operator fun Fnc<*>.invoke(Y: Y<P, Q>, YP: Const<P, Q>, Z: Z<P, Q>, ZP: Const<P, Q>): X<P, Q> =
    when (this) {
      is XYZ<*, *> -> (this as XYZ<P, Q>)(Y, YP, Z, ZP)
      is XY<*, *> -> (this as XY<P, Q>)(Y, YP)
      is XZ<*, *> -> (this as XZ<P, Q>)(Z, ZP)
      is YZ<*, *> -> X((this as YZ<P, Q>)(Y, YP, Z, ZP))
      is X<*, *> -> this as X<P, Q>
      is Y<*, *> -> X((this as Y<P, Q>)(YP))
      is Z<*, *> -> X((this as Z<P, Q>)(ZP))
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(Y: Y<P, Q>, YP: Const<P, Q>, Z: Z<P, Q>, ZP: Const<P, Q>): X<P, Q> =
    X(left(Y, YP, Z, ZP), right(Y, YP, Z, ZP), app)

  operator fun Fnc<*>.invoke(X: X<P, Q>, XP: Const<P, Q>): YZ<P, Q> =
    when (this) {
      is XYZ<*, *> -> (this as XYZ<P, Q>)(X, XP)
      is XY<*, *> -> YZ((this as XY<P, Q>)(X, XP))
      is XZ<*, *> -> YZ((this as XZ<P, Q>)(X, XP))
      is YZ<*, *> -> this as YZ<P, Q>
      is X<*, *> -> YZ(X(XP))
      is Y<*, *> -> YZ(this as Y<P, Q>)
      is Z<*, *> -> YZ(this as Z<P, Q>)
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(X: X<P, Q>, XP: Const<P, Q>): YZ<P, Q> = YZ(left(X, XP), right(X, XP), app)
  operator fun Fnc<*>.invoke(Y: Y<P, Q>, YP: Const<P, Q>): XZ<P, Q> =
    when (this) {
      is XYZ<*, *> -> (this as XYZ<P, Q>)(Y, YP)
      is XY<*, *> -> XZ((this as XY<P, Q>)(Y, YP))
      is XZ<*, *> -> this as XZ<P, Q>
      is YZ<*, *> -> XZ((this as YZ<P, Q>)(Y, YP))
      is X<*, *> -> XZ(this as X<P, Q>)
      is Y<*, *> -> XZ(Y(YP))
      is Z<*, *> -> XZ(this as Z<P, Q>)
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(Y: Y<P, Q>, YP: Const<P, Q>): XZ<P, Q> = XZ(left(Y, YP), right(Y, YP), app)
  operator fun Fnc<*>.invoke(Z: Z<P, Q>, ZP: Const<P, Q>): XY<P, Q> =
    when (this) {
      is XYZ<*, *> -> (this as XYZ<P, Q>)(Z, ZP)
      is XY<*, *> -> this as XY<P, Q>
      is XZ<*, *> -> XY((this as XZ<P, Q>)(Z, ZP))
      is YZ<*, *> -> XY((this as YZ<P, Q>)(Z, ZP))
      is X<*, *> -> XY(this as X<P, Q>)
      is Y<*, *> -> XY(this as Y<P, Q>)
      is Z<*, *> -> XY(Z(ZP))
      else -> throw ClassNotFoundException(toString())
    }

  operator fun invoke(Z: Z<P, Q>, ZP: Const<P, Q>): XY<P, Q> = XY(left(Z, ZP), right(Z, ZP), app)
}

abstract class Fnc<T : Fnc<T>> {
  abstract infix operator fun plus(that: T): T
  abstract infix operator fun times(that: T): T
}

abstract class Const<T : Fnc<T>, Y : Number>(open val c: Y) : Fnc<T>() {
//  override fun <T: Fnc<T>> plus(fn: T): T = TODO()
}

class DConst(override val c: Double) : Const<DConst, Double>(c) {
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

sealed class Proto<T : Const<T, Q>, Q : Number> {
  abstract fun wrap(default: Number): T

  operator fun Number.times(multiplicand: Fnc<T>) = multiplicand * wrap(this)
//  operator fun Fnc<T>.times(multiplicand: Number) = wrap(multiplicand) * this

  operator fun Number.plus(addend: Fnc<T>) = addend + wrap(this)
//  operator fun Fnc<T>.plus(addend: Number) = wrap(addend) + this

  abstract val X: X<T, Q>
  abstract val Y: Y<T, Q>
  abstract val Z: Z<T, Q>
}

typealias DCD = Const<DConst, Double>

object DProto : Proto<DConst, Double>() {
  override fun wrap(default: Number): DConst = DConst(default.toDouble())
  override val X: X<DConst, Double> = object :
    X<DConst, Double>(Vrb, Vrb, ::firstX) {
    override fun invoke(X: DCD): DCD = X
  }
  override val Y: Y<DConst, Double> = object :
    Y<DConst, Double>(Vrb, Vrb, ::firstX) {
    override fun invoke(Y: DCD): DCD = Y
  }
  override val Z: Z<DConst, Double> = object :
    Z<DConst, Double>(Vrb, Vrb, ::firstX) {
    override fun invoke(Z: DCD): DCD = Z
  }
}