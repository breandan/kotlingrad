//package edu.umontreal.kotlingrad.samples
//
//open class Vr
//object x: Vr() {
//  operator fun plus(x: x): (x) -> Double = { x -> TODO() }
//  operator fun plus(y: y): (x, y) -> Double = { x, y -> TODO() }
//  operator fun plus(z: z): (x, z) -> Double = { x, z -> TODO() }
//}
//object y: Vr() {
//  operator fun plus(y: y): (y) -> Double = { y -> TODO() }
//  operator fun plus(x: x): (x, y) -> Double = { x, y -> TODO() }
//  operator fun plus(z: z): (y, z) -> Double = { y, z -> TODO() }
//}
//object z: Vr() {
//  operator fun plus(z: z): (z) -> Double = { z -> TODO() }
//  operator fun plus(x: x): (x, z) -> Double = { x, z -> TODO() }
//  operator fun plus(y: y): (y, z) -> Double = { x, y -> TODO() }
//}
//
////@JvmName("x+x") operator fun <T1: Vr> T1.plus(t1: T1): (T1) -> Double = { t1: T1 -> TODO() }
//
//@JvmName("x+(y)") operator fun <T1: Vr, T2: Vr> T1.plus(f2: (T2) -> Double): (T1, T2) -> Double = { t1: T1, t2: T2 -> TODO() }
//@JvmName("x+(yz)") operator fun <T1: Vr, T2: Vr, T3: Vr> T1.plus(f2: (T2, T3) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//@JvmName("x+(xz)") operator fun <T1: Vr, T2: Vr> T1.plus(f2: (T1, T2) -> Double): (T1, T2) -> Double = { t1: T1, t2: T2 -> TODO() }
//@JvmName("x+(xyz)") operator fun <T1: Vr, T2: Vr, T3: Vr> T1.plus(f2: (T1, T2, T3) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//
//@JvmName("x+(x)") operator fun <T1: Vr> T1.plus(f1: (T1) -> Double): (T1) -> Double = { t1: T1 -> TODO() }
//
//@JvmName("(x)+x") operator fun <T1: Vr> ((T1) -> Double).plus(t1: T1): (T1) -> Double = { t1: T1 -> TODO() }
//@JvmName("(x)+(x)") inline operator fun <reified T1: Vr> ((T1) -> Double).plus(f1: (T1) -> Double): (T1) -> Double = { t1: T1 -> TODO() }
//
//@JvmName("(x)+y") operator fun <T1: Vr, T2: Vr> ((T1) -> Double).plus(t2: T2): (T1, T2) -> Double = { t1, t2 -> TODO() }
//@JvmName("(x)+(y)") inline operator fun <reified T1: Vr, reified T2: Vr> ((T1) -> Double).plus(f2: (T2) -> Double): (T1, T2) -> Double = { t1, t2 -> TODO() }
//
//@JvmName("(xy)+x") operator fun <T1: Vr, T2: Vr> ((T1, T2) -> Double).plus(t1: T1): (T1, T2) -> Double = { t1: T1, t2: T2 -> TODO() }
//@JvmName("(xy)+(x)") operator fun <T1: Vr, T2: Vr> ((T1, T2) -> Double).plus(f1: (T1) -> Double): (T1, T2) -> Double = { t1: T1, t2: T2 -> TODO() }
//
//@JvmName("(xy)+y") operator fun <T1: Vr, T2: Vr> ((T1, T2) -> Double).plus(t1: T2): (T1, T2) -> Double = { t1: T1, t2: T2 -> TODO() }
//@JvmName("(xy)+(y)") operator fun <T1: Vr, T2: Vr> ((T1, T2) -> Double).plus(f2: (T2) -> Double): (T1, T2) -> Double = { t1: T1, t2: T2 -> TODO() }
//
//
//@JvmName("(xy)+z") operator fun <T1: Vr, T2: Vr, T3> ((T1, T2) -> Double).plus(t3: T3): (T1, T2, T3) -> Double = { t1, t2, t3 -> TODO() }
//@JvmName("(xy)+(z)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T1, T2) -> Double).plus(f3: (T3) -> Double): (T1, T2, T3) -> Double = { t1, t2, t3 -> TODO() }
//
//
//@JvmName("(xyz)+x") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T1, T2, T3) -> Double).plus(t1: T1): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//@JvmName("(xyz)+y") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T1, T2, T3) -> Double).plus(t2: T2): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//@JvmName("(xyz)+z") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T1, T2, T3) -> Double).plus(t2: T3): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//
//@JvmName("(xyz)+(x)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T1, T2, T3) -> Double).plus(f1: (T1) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//@JvmName("(xyz)+(y)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T1, T2, T3) -> Double).plus(f2: (T2) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//@JvmName("(xyz)+(z)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T1, T2, T3) -> Double).plus(f3: (T3) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//
//@JvmName("(x)+(xy)") operator fun <T1: Vr, T2: Vr> ((T1) -> Double).plus(f3: (T1, T2) -> Double): (T1, T2) -> Double = { t1: T1, t2: T2 -> TODO() }
//@JvmName("(y)+(xz)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T2) -> Double).plus(f3: (T1, T3) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//@JvmName("(z)+(yz)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T3) -> Double).plus(f3: (T2, T3) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//
//@JvmName("(x)+(xyz)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T1) -> Double).plus(f3: (T1, T2, T3) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3 -> TODO() }
//@JvmName("(y)+(xyz)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T2) -> Double).plus(f3: (T1, T2, T3) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//@JvmName("(z)+(xyz)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T3) -> Double).plus(f3: (T1, T2, T3) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//
//@JvmName("(xy)+(xy)") operator fun <T1: Vr, T2: Vr> ((T1, T2) -> Double).plus(f3: (T1, T2) -> Double): (T1, T2) -> Double = { t1: T1, t2: T2 -> TODO() }
//@JvmName("(xy)+(xz)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T1, T2) -> Double).plus(f3: (T1, T3) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//@JvmName("(xy)+(yz)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T1, T2) -> Double).plus(f3: (T2, T3) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//@JvmName("(yz)+(xz)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T2, T3) -> Double).plus(f3: (T1, T3) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//
//@JvmName("(xy)+(xyz)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T1, T2) -> Double).plus(f3: (T1, T2, T3) -> Double): (T1, T2) -> Double = { t1: T1, t2: T2 -> TODO() }
//@JvmName("(xz)+(xyz)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T1, T3) -> Double).plus(f3: (T1, T2, T3) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//@JvmName("(yz)+(xyz)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T2, T3) -> Double).plus(f3: (T1, T2, T3) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//
//@JvmName("(xyz)+(xy)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T1, T2, T3) -> Double).plus(f3: (T1, T2) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//@JvmName("(xyz)+(yz)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T1, T2, T3) -> Double).plus(f3: (T2, T3) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//@JvmName("(xyz)+(xz)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T1, T2, T3) -> Double).plus(f3: (T1, T3) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//
//@JvmName("(xyz)+(xyz)") operator fun <T1: Vr, T2: Vr, T3: Vr> ((T1, T2, T3) -> Double).plus(f3: (T1, T2, T3) -> Double): (T1, T2, T3) -> Double = { t1: T1, t2: T2, t3: T3 -> TODO() }
//
//
//fun main() {
//// TODO: Figure out the problem here: maybe a Kotlin type checking bug?
////  val m = ((x + y) + z) + (x + z)
//  val s = y + y
//  val t = x + x
////  val d = s + t
//  val q = x + y + z + (x + y + z)
//  val z = x + (x + y + z)
//}