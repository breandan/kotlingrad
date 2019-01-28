package edu.umontreal.kotlingrad.dependent

@JvmName("longVecPlus") infix operator fun <C: `15`, V: Vec<Long, C>> V.plus(v: V): Vec<Long, C> = add(v)
@JvmName("intVecPlus") infix operator fun <C: `15`, V: Vec<Int, C>> V.plus(v: V): Vec<Int, C> = add(v)
@JvmName("shortVecPlus") infix operator fun <C: `15`, V: Vec<Short, C>> V.plus(v: V): Vec<Short, C> = add(v)
@JvmName("byteVecPlus") infix operator fun <C: `15`, V: Vec<Byte, C>> V.plus(v: V): Vec<Byte, C> = add(v)
@JvmName("doubleVecPlus") infix operator fun <C: `15`, V: Vec<Double, C>> V.plus(v: V): Vec<Double, C> = add(v)
@JvmName("floatVecPlus") infix operator fun <C: `15`, V: Vec<Float, C>> V.plus(v: V): Vec<Float, C> = add(v)

//infix operator fun <T: Number, C: `4`, V: Vec<Number, C>> V.plus(v: V): Vec<Number, C> = add(v)

//@JvmName("v0cT") infix fun <T> Vec<T, `0`>.cat(t: T): Vec<T, `1`> = Vec(`1`, contents + t)
//
//@JvmName("v1cT") infix fun <T> Vec<T, `1`>.cat(t: T): Vec<T, `2`> = Vec(`2`, contents + t)
//
//@JvmName("v2cT") infix fun <T> Vec<T, `2`>.cat(t: T): Vec<T, `3`> = Vec(`3`, contents + t)
//
//@JvmName("v3cT") infix fun <T> Vec<T, `3`>.cat(t: T): Vec<T, `4`> = Vec(`4`, contents + t)
//
//@JvmName("vTc0") infix fun <T> T.cat(t: Vec<T, `0`>): Vec<T, `1`> = Vec(`1`, arrayListOf(this))
//
//@JvmName("vTc1") infix fun <T> T.cat(t: Vec<T, `1`>): Vec<T, `2`> = Vec(`2`, arrayListOf(this) + t.contents)
//
//@JvmName("vTc2") infix fun <T> T.cat(t: Vec<T, `2`>): Vec<T, `3`> = Vec(`3`, arrayListOf(this) + t.contents)
//
//@JvmName("vTc3") infix fun <T> T.cat(t: Vec<T, `3`>): Vec<T, `4`> = Vec(`4`, arrayListOf(this) + t.contents)
//
//@JvmName("v0c0") infix fun <T> Vec<T, `0`>.cat(l: Vec<T, `0`>): Vec<T, `0`> = l
//
//@JvmName("v0c1") infix fun <T> Vec<T, `0`>.cat(l: Vec<T, `1`>): Vec<T, `1`> = l
//
//@JvmName("v0c2") infix fun <T> Vec<T, `0`>.cat(l: Vec<T, `2`>): Vec<T, `2`> = l
//
//@JvmName("v0c3") infix fun <T> Vec<T, `0`>.cat(l: Vec<T, `3`>): Vec<T, `3`> = l
//
//@JvmName("v1c0") infix fun <T> Vec<T, `1`>.cat(l: Vec<T, `0`>): Vec<T, `1`> = this
//
//@JvmName("v2c0") infix fun <T> Vec<T, `2`>.cat(l: Vec<T, `0`>): Vec<T, `2`> = this
//
//@JvmName("v3c0") infix fun <T> Vec<T, `3`>.cat(l: Vec<T, `0`>): Vec<T, `3`> = this
//
//@JvmName("v1c1") infix fun <T> Vec<T, `1`>.cat(l: Vec<T, `1`>): Vec<T, `2`> = Vec(`2`, contents + l.contents)
//
//@JvmName("v1c2") infix fun <T> Vec<T, `1`>.cat(l: Vec<T, `2`>): Vec<T, `3`> = Vec(`3`, contents + l.contents)
//
//@JvmName("v1c3") infix fun <T> Vec<T, `1`>.cat(l: Vec<T, `3`>): Vec<T, `4`> = Vec(`4`, contents + l.contents)
//
//@JvmName("v2c1") infix fun <T> Vec<T, `2`>.cat(l: Vec<T, `1`>): Vec<T, `3`> = Vec(`3`, contents + l.contents)
//
//@JvmName("v3c1") infix fun <T> Vec<T, `3`>.cat(l: Vec<T, `1`>): Vec<T, `4`> = Vec(`4`, contents + l.contents)
//
//@JvmName("v2c2") infix fun <T> Vec<T, `2`>.cat(l: Vec<T, `2`>): Vec<T, `4`> = Vec(`4`, contents + l.contents)