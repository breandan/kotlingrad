package edu.umontreal.kotlingrad.samples

fun main() {
  val q = X + Y + Z + Y
  val d: Double = q(X = 1.0, Y = 2.0, Z = 3.0)
  val z: Double = q(X to 1.0, Y to 1.0)(1.0)
  val m: Double = q(X to 1.0).invoke(Y = 1.0, Z = 1.0)
  val t = X + Z
  val l: Z = t(X to 1.0)
}

interface V
interface C: V { companion object: C }
interface X: V {
  operator fun plus(X: X): X = X
  operator fun plus(Y: Y): XY = XY
  operator fun plus(Z: Z): XZ = XZ
  operator fun plus(XY: XY): XY = XY
  operator fun plus(XZ: XZ): XZ = XZ
  operator fun plus(YZ: YZ): XYZ = XYZ
  operator fun plus(XYZ: XYZ): XYZ = XYZ
  operator fun invoke(X: Double) = 0.0
  companion object: X
}
interface Y: V {
  operator fun plus(Y: Y): Y = Y
  operator fun plus(Y: X): XY = XY
  operator fun plus(Z: Z): YZ = YZ
  operator fun plus(XY: XY): XY = XY
  operator fun plus(XZ: XZ): XYZ = XYZ
  operator fun plus(YZ: YZ): YZ = YZ
  operator fun plus(XYZ: XYZ): XYZ = XYZ
  operator fun invoke(Y: Double) = 0.0
  companion object: Y
}
interface Z: V {
  operator fun plus(Z: Z): Z = Z
  operator fun plus(Y: Y): YZ = YZ
  operator fun plus(X: X): XZ = XZ
  operator fun plus(XY: XY): XYZ = XYZ
  operator fun plus(XZ: XZ): XZ = XZ
  operator fun plus(YZ: YZ): YZ = YZ
  operator fun plus(XYZ: XYZ): XYZ = XYZ
  operator fun invoke(Z: Double) = 0.0
  companion object: Z
}
interface XY {
  operator fun plus(X: X): XY = XY
  operator fun plus(Y: Y): XY = XY
  operator fun plus(Z: Z): XYZ = XYZ
  operator fun plus(XY: XY): XY = XY
  operator fun plus(XZ: XZ): XYZ = XYZ
  operator fun plus(YZ: YZ): XYZ = XYZ
  operator fun plus(XYZ: XYZ): XYZ = XYZ
  operator fun invoke(X: Pair<X, Double>, Y: Pair<Y, Double>) = 0.0
  operator fun invoke(X: Double, Y: Double) = 0.0
  operator fun invoke(X: Pair<X, Double>): Y = Y
  operator fun invoke(Y: Pair<Y, Double>): X = X
  companion object: XY
}
interface XZ {
  operator fun plus(X: X): XZ = XZ
  operator fun plus(Y: Y): XYZ = XYZ
  operator fun plus(Z: Z): XZ = XZ
  operator fun plus(XY: XY): XYZ = XYZ
  operator fun plus(XZ: XZ): XZ = XZ
  operator fun plus(YZ: YZ): XYZ = XYZ
  operator fun plus(XYZ: XYZ): XYZ = XYZ
  operator fun invoke(X: Pair<X, Double>, Z: Pair<Z, Double>) = 0.0
  operator fun invoke(X: Double, Z: Double) = 0.0
  operator fun invoke(X: Pair<X, Double>): Z = Z
  operator fun invoke(Z: Pair<Z, Double>): X = X
  companion object: XZ
}
interface YZ {
  operator fun plus(X: X): XYZ = XYZ
  operator fun plus(Z: Z): YZ = YZ
  operator fun plus(Y: Y): YZ = YZ
  operator fun plus(XY: XY): XYZ = XYZ
  operator fun plus(XZ: XZ): XYZ = XYZ
  operator fun plus(YZ: YZ): YZ = YZ
  operator fun plus(XYZ: XYZ): XYZ = XYZ
  operator fun invoke(Y: Pair<Y, Double>, Z: Pair<Z, Double>) = 0.0
  operator fun invoke(Y: Double, Z: Double) = 0.0
  operator fun invoke(Y: Pair<Y, Double>): Z = Z
  operator fun invoke(Z: Pair<Z, Double>): Y = Y
  companion object: YZ
}
interface XYZ {
  operator fun plus(X: X): XYZ = XYZ
  operator fun plus(Y: Y): XYZ = XYZ
  operator fun plus(Z: Z): XYZ = XYZ
  operator fun plus(XY: XY): XYZ = XYZ
  operator fun plus(XZ: XZ): XYZ = XYZ
  operator fun plus(YZ: YZ): XYZ = XYZ
  operator fun plus(XYZ: XYZ): XYZ = XYZ
  operator fun invoke(X: Double, Y: Double, Z: Double) = 0.0
  operator fun invoke(X: Pair<X, Double>, Z: Pair<Z, Double>): Y = Y
  operator fun invoke(X: Pair<X, Double>, Y: Pair<Y, Double>): Z = Z
  operator fun invoke(Y: Pair<Y, Double>, Z: Pair<Z, Double>): X = X
  operator fun invoke(X: Pair<X, Double>): YZ = YZ
  operator fun invoke(Y: Pair<Y, Double>): XZ = XZ
  operator fun invoke(Z: Pair<Z, Double>): XY = XY
  companion object: XYZ
}

//@JvmName("01") operator fun X.plus(X: X) = X
//@JvmName("02") operator fun X.plus(Y: Y) = XY
//@JvmName("03") operator fun X.plus(Z: Z) = XZ
//@JvmName("04") operator fun Y.plus(X: X) = XY
//@JvmName("05") operator fun Y.plus(Y: Y) = Y
//@JvmName("06") operator fun Y.plus(Z: Z) = YZ
//@JvmName("07") operator fun Z.plus(X: X) = XZ
//@JvmName("08") operator fun Z.plus(Y: Y) = YZ
//@JvmName("09") operator fun Z.plus(Z: Z) = Z
//@JvmName("10") operator fun XY.plus(X: X) = XY
//@JvmName("11") operator fun XY.plus(Y: Y) = XY
//@JvmName("12") operator fun XY.plus(Z: Z) = XYZ
//@JvmName("13") operator fun YZ.plus(X: X) = XYZ
//@JvmName("14") operator fun YZ.plus(Y: Y) = YZ
//@JvmName("15") operator fun YZ.plus(Z: Z) = YZ
//@JvmName("16") operator fun XZ.plus(X: X) = XZ
//@JvmName("17") operator fun XZ.plus(Y: Y) = XYZ
//@JvmName("18") operator fun XZ.plus(Z: Z) = XZ
//@JvmName("19") operator fun XYZ.plus(X: X) = XYZ
//@JvmName("20") operator fun XYZ.plus(Y: Y) = XYZ
//@JvmName("20") operator fun XYZ.plus(Z: Z) = XYZ
//@JvmName("22") operator fun XYZ.invoke(X: Double, Y: Double, Z: Double): Double = 0.0
//@JvmName("23") operator fun XYZ.invoke(X: X, Z: Z) = Y
//@JvmName("24") operator fun XYZ.invoke(X: X, Z: Y) = Z
//@JvmName("25") operator fun XYZ.invoke(Y: Y, Z: Z) = X
//@JvmName("26") operator fun XYZ.invoke(X: X) = YZ
//@JvmName("27") operator fun XYZ.invoke(Y: Y) = XZ
//@JvmName("28") operator fun XYZ.invoke(Z: Z) = XY
