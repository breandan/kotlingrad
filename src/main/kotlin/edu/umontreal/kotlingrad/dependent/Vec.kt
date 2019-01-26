package edu.umontreal.kotlingrad.dependent

// Supports vectors of up to length 6
open class Vec<S, MaxLength: `4`> internal constructor(val contents: List<S> = arrayListOf()) {
  companion object {
    operator fun <T> invoke(): Vec<T, `0`> = Vec(arrayListOf())
    operator fun <T> invoke(t: T): Vec<T, `1`> = Vec(arrayListOf(t))
    operator fun <T> invoke(t0: T, t1: T): Vec<T, `2`> = Vec(arrayListOf(t0, t1))
    operator fun <T> invoke(t0: T, t1: T, t2: T): Vec<T, `3`> = Vec(arrayListOf(t0, t1, t2))
    operator fun <T> invoke(t0: T, t1: T, t2: T, t3: T): Vec<T, `4`> = Vec(arrayListOf(t0, t1, t2, t3))
  }

  @JvmName("addLong") infix fun <S: MaxLength, V: Vec<Long, S>> V.add(v: V) = Vec<Long, S>(contents.zip(v.contents).map { it.first + it.second })
  @JvmName("addInt") infix fun <S: MaxLength, V: Vec<Int, S>> V.add(v: V) = Vec<Int, S>(contents.zip(v.contents).map { it.first + it.second })
  @JvmName("addShort") infix fun <S: MaxLength, V: Vec<Short, S>> V.add(v: V) = Vec<Short, S>(contents.zip(v.contents).map { (it.first + it.second).toShort() })
  @JvmName("addByte") infix fun <S: MaxLength, V: Vec<Byte, S>> V.add(v: V) = Vec<Byte, S>(contents.zip(v.contents).map { (it.first + it.second).toByte() })
  @JvmName("addDouble") infix fun <S: MaxLength, V: Vec<Double, S>> V.add(v: V) = Vec<Double, S>(contents.zip(v.contents).map { it.first + it.second })
  @JvmName("addFloat") infix fun <S: MaxLength, V: Vec<Float, S>> V.add(v: V) = Vec<Float, S>(contents.zip(v.contents).map { it.first + it.second })

  override fun toString() = "$contents"
}

infix operator fun <T> Vec<T, `1`>.get(index: `0`): T = contents[0]

infix operator fun <T> Vec<T, `2`>.get(index: `1`): T =
  when(index) {
    is `0` -> contents[0]
    else -> contents[1]
  }

infix operator fun <T> Vec<T, `3`>.get(index: `2`): T =
  when(index) {
    is `0` -> contents[0]
    is `1` -> contents[1]
    else -> contents[2]
  }

infix operator fun <T> Vec<T, `4`>.get(index: `3`): T =
  when(index) {
    is `0` -> contents[0]
    is `1` -> contents[1]
    is `2` -> contents[2]
    else -> contents[3]
  }

@JvmName("plusLong") infix operator fun <C: `4`, V: Vec<Long, C>> V.plus(v: V): Vec<Long, C> = add(v)
@JvmName("plusInt") infix operator fun <C: `4`, V: Vec<Int, C>> V.plus(v: V): Vec<Int, C> = add(v)
@JvmName("plusShort") infix operator fun <C: `4`, V: Vec<Short, C>> V.plus(v: V): Vec<Short, C> = add(v)
@JvmName("plusByte") infix operator fun <C: `4`, V: Vec<Byte, C>> V.plus(v: V): Vec<Byte, C> = add(v)
@JvmName("plusDouble") infix operator fun <C: `4`, V: Vec<Double, C>> V.plus(v: V): Vec<Double, C> = add(v)
@JvmName("plusFloat") infix operator fun <C: `4`, V: Vec<Float, C>> V.plus(v: V): Vec<Float, C> = add(v)

//infix operator fun <T: Number, C: `4`, V: Vec<Number, C>> V.plus(v: V): Vec<Number, C> = add(v)

@JvmName("v0cT") infix fun <T> Vec<T, `0`>.cat(t: T): Vec<T, `1`> = Vec(contents + t)

@JvmName("v1cT") infix fun <T> Vec<T, `1`>.cat(t: T): Vec<T, `2`> = Vec(contents + t)

@JvmName("v2cT") infix fun <T> Vec<T, `2`>.cat(t: T): Vec<T, `3`> = Vec(contents + t)

@JvmName("v3cT") infix fun <T> Vec<T, `3`>.cat(t: T): Vec<T, `4`> = Vec(contents + t)

@JvmName("vTc0") infix fun <T> T.cat(t: Vec<T, `0`>): Vec<T, `1`> = Vec(arrayListOf(this))

@JvmName("vTc1") infix fun <T> T.cat(t: Vec<T, `1`>): Vec<T, `2`> = Vec(arrayListOf(this) + t.contents)

@JvmName("vTc2") infix fun <T> T.cat(t: Vec<T, `2`>): Vec<T, `3`> = Vec(arrayListOf(this) + t.contents)

@JvmName("vTc3") infix fun <T> T.cat(t: Vec<T, `3`>): Vec<T, `4`> = Vec(arrayListOf(this) + t.contents)

@JvmName("v0c0") infix fun <T> Vec<T, `0`>.cat(l: Vec<T, `0`>): Vec<T, `0`> = l

@JvmName("v0c1") infix fun <T> Vec<T, `0`>.cat(l: Vec<T, `1`>): Vec<T, `1`> = l

@JvmName("v0c2") infix fun <T> Vec<T, `0`>.cat(l: Vec<T, `2`>): Vec<T, `2`> = l

@JvmName("v0c3") infix fun <T> Vec<T, `0`>.cat(l: Vec<T, `3`>): Vec<T, `3`> = l

@JvmName("v1c0") infix fun <T> Vec<T, `1`>.cat(l: Vec<T, `0`>): Vec<T, `1`> = this

@JvmName("v2c0") infix fun <T> Vec<T, `2`>.cat(l: Vec<T, `0`>): Vec<T, `2`> = this

@JvmName("v3c0") infix fun <T> Vec<T, `3`>.cat(l: Vec<T, `0`>): Vec<T, `3`> = this

@JvmName("v1c1") infix fun <T> Vec<T, `1`>.cat(l: Vec<T, `1`>): Vec<T, `2`> = Vec(contents + l.contents)

@JvmName("v1c2") infix fun <T> Vec<T, `1`>.cat(l: Vec<T, `2`>): Vec<T, `3`> = Vec(contents + l.contents)

@JvmName("v1c3") infix fun <T> Vec<T, `1`>.cat(l: Vec<T, `3`>): Vec<T, `4`> = Vec(contents + l.contents)

@JvmName("v2c1") infix fun <T> Vec<T, `2`>.cat(l: Vec<T, `1`>): Vec<T, `3`> = Vec(contents + l.contents)

@JvmName("v3c1") infix fun <T> Vec<T, `3`>.cat(l: Vec<T, `1`>): Vec<T, `4`> = Vec(contents + l.contents)

@JvmName("v2c2") infix fun <T> Vec<T, `2`>.cat(l: Vec<T, `2`>): Vec<T, `4`> = Vec(contents + l.contents)
