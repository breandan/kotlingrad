package edu.umontreal.kotlingrad.dependent

// Supports vectors of up to length 6
open class Vec<T, MaxLength: `4`> internal constructor(val contents: List<T> = arrayListOf()) {
  companion object {
    operator fun <T> invoke(): Vec<T, `0`> = Vec(arrayListOf())
    operator fun <T> invoke(t: T): Vec<T, `1`> = Vec(arrayListOf(t))
    operator fun <T> invoke(t0: T, t1: T): Vec<T, `2`> = Vec(arrayListOf(t0, t1))
    operator fun <T> invoke(t0: T, t1: T, t2: T): Vec<T, `3`> = Vec(arrayListOf(t0, t1, t2))
    operator fun <T> invoke(t0: T, t1: T, t2: T, t3: T): Vec<T, `4`> = Vec(arrayListOf(t0, t1, t2, t3))
  }

  override fun toString() = "$contents"
}

@JvmName("g1") infix operator fun <T> Vec<T, `1`>.get(i: `0`): T = contents[0]

@JvmName("g2") infix operator fun <T> Vec<T, `2`>.get(i: `0`): T = contents[0]

@JvmName("g2") infix operator fun <T> Vec<T, `2`>.get(i: `1`): T = contents[1]

@JvmName("g3") infix operator fun <T> Vec<T, `3`>.get(i: `0`): T = contents[0]

@JvmName("g3") infix operator fun <T> Vec<T, `3`>.get(i: `1`): T = contents[1]

@JvmName("g3") infix operator fun <T> Vec<T, `3`>.get(i: `2`): T = contents[2]

@JvmName("g4") infix operator fun <T> Vec<T, `4`>.get(i: `0`): T = contents[0]

@JvmName("g4") infix operator fun <T> Vec<T, `4`>.get(i: `1`): T = contents[1]

@JvmName("g4") infix operator fun <T> Vec<T, `4`>.get(i: `2`): T = contents[2]

@JvmName("g4") infix operator fun <T> Vec<T, `4`>.get(i: `3`): T = contents[3]

@JvmName("1p1") infix operator fun <C: `1`> Vec<Double, C>.plus(v: Vec<Double, C>): Vec<Double, C> = Vec(contents.zip(v.contents).map { it.first + it.second })

@JvmName("2p2") infix operator fun <C: `2`> Vec<Double, C>.plus(v: Vec<Double, C>): Vec<Double, C> = Vec(contents.zip(v.contents).map { it.first + it.second })

@JvmName("3p3") infix operator fun <C: `3`> Vec<Double, C>.plus(v: Vec<Double, C>): Vec<Double, C> = Vec(contents.zip(v.contents).map { it.first + it.second })

@JvmName("4p4") infix operator fun <C: `4`> Vec<Double, C>.plus(v: Vec<Double, C>): Vec<Double, C> = Vec(contents.zip(v.contents).map { it.first + it.second })

@JvmName("0cT") infix fun <T> Vec<T, `0`>.cat(t: T): Vec<T, `1`> = Vec(contents + t)

@JvmName("1cT") infix fun <T> Vec<T, `1`>.cat(t: T): Vec<T, `2`> = Vec(contents + t)

@JvmName("2cT") infix fun <T> Vec<T, `2`>.cat(t: T): Vec<T, `3`> = Vec(contents + t)

@JvmName("3cT") infix fun <T> Vec<T, `3`>.cat(t: T): Vec<T, `4`> = Vec(contents + t)

@JvmName("Tc0") infix fun <T> T.cat(t: Vec<T, `0`>): Vec<T, `1`> = Vec(arrayListOf(this))

@JvmName("Tc1") infix fun <T> T.cat(t: Vec<T, `1`>): Vec<T, `2`> = Vec(arrayListOf(this) + t.contents)

@JvmName("Tc2") infix fun <T> T.cat(t: Vec<T, `2`>): Vec<T, `3`> = Vec(arrayListOf(this) + t.contents)

@JvmName("Tc3") infix fun <T> T.cat(t: Vec<T, `3`>): Vec<T, `4`> = Vec(arrayListOf(this) + t.contents)

@JvmName("0c0") infix fun <T> Vec<T, `0`>.cat(l: Vec<T, `0`>): Vec<T, `0`> = l

@JvmName("0c1") infix fun <T> Vec<T, `0`>.cat(l: Vec<T, `1`>): Vec<T, `1`> = l

@JvmName("0c2") infix fun <T> Vec<T, `0`>.cat(l: Vec<T, `2`>): Vec<T, `2`> = l

@JvmName("0c3") infix fun <T> Vec<T, `0`>.cat(l: Vec<T, `3`>): Vec<T, `3`> = l

@JvmName("1c0") infix fun <T> Vec<T, `1`>.cat(l: Vec<T, `0`>): Vec<T, `1`> = this

@JvmName("2c0") infix fun <T> Vec<T, `2`>.cat(l: Vec<T, `0`>): Vec<T, `2`> = this

@JvmName("3c0") infix fun <T> Vec<T, `3`>.cat(l: Vec<T, `0`>): Vec<T, `3`> = this

@JvmName("1c1") infix fun <T> Vec<T, `1`>.cat(l: Vec<T, `1`>): Vec<T, `2`> = Vec(contents + l.contents)

@JvmName("1c2") infix fun <T> Vec<T, `1`>.cat(l: Vec<T, `2`>): Vec<T, `3`> = Vec(contents + l.contents)

@JvmName("1c3") infix fun <T> Vec<T, `1`>.cat(l: Vec<T, `3`>): Vec<T, `4`> = Vec(contents + l.contents)

@JvmName("2c1") infix fun <T> Vec<T, `2`>.cat(l: Vec<T, `1`>): Vec<T, `3`> = Vec(contents + l.contents)

@JvmName("3c1") infix fun <T> Vec<T, `3`>.cat(l: Vec<T, `1`>): Vec<T, `4`> = Vec(contents + l.contents)

@JvmName("2c2") infix fun <T> Vec<T, `2`>.cat(l: Vec<T, `2`>): Vec<T, `4`> = Vec(contents + l.contents)
