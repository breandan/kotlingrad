package edu.umontreal.kotlingrad.dependent

@JvmName("longVecPlus") infix operator fun <C: D100, V: Vec<Long, C>> V.plus(v: V): Vec<Long, C> = Vec(length, contents.zip(v.contents).map { it.first + it.second })
@JvmName("intVecPlus") infix operator fun <C: D100, V: Vec<Int, C>> V.plus(v: V): Vec<Int, C> = Vec(length, contents.zip(v.contents).map { it.first + it.second })
@JvmName("shortVecPlus") infix operator fun <C: D100, V: Vec<Short, C>> V.plus(v: V): Vec<Short, C> = Vec(length, contents.zip(v.contents).map { (it.first + it.second).toShort() })
@JvmName("byteVecPlus") infix operator fun <C: D100, V: Vec<Byte, C>> V.plus(v: V): Vec<Byte, C> = Vec(length, contents.zip(v.contents).map { (it.first + it.second).toByte() })
@JvmName("doubleVecPlus") infix operator fun <C: D100, V: Vec<Double, C>> V.plus(v: V): Vec<Double, C> = Vec(length, contents.zip(v.contents).map { it.first + it.second })
@JvmName("floatVecPlus") infix operator fun <C: D100, V: Vec<Float, C>> V.plus(v: V): Vec<Float, C> = Vec(length, contents.zip(v.contents).map { it.first + it.second })

@JvmName("longVecMinus") infix operator fun <C: D100, V: Vec<Long, C>> V.minus(v: V): Vec<Long, C> = Vec(length, contents.zip(v.contents).map { it.first - it.second })
@JvmName("intVecMinus") infix operator fun <C: D100, V: Vec<Int, C>> V.minus(v: V): Vec<Int, C> = Vec(length, contents.zip(v.contents).map { it.first - it.second })
@JvmName("shortVecMinus") infix operator fun <C: D100, V: Vec<Short, C>> V.minus(v: V): Vec<Short, C> = Vec(length, contents.zip(v.contents).map { (it.first - it.second).toShort() })
@JvmName("byteVecMinus") infix operator fun <C: D100, V: Vec<Byte, C>> V.minus(v: V): Vec<Byte, C> = Vec(length, contents.zip(v.contents).map { (it.first - it.second).toByte() })
@JvmName("doubleVecMinus") infix operator fun <C: D100, V: Vec<Double, C>> V.minus(v: V): Vec<Double, C> = Vec(length, contents.zip(v.contents).map { it.first - it.second })
@JvmName("floatVecMinus") infix operator fun <C: D100, V: Vec<Float, C>> V.minus(v: V): Vec<Float, C> = Vec(length, contents.zip(v.contents).map { it.first - it.second })