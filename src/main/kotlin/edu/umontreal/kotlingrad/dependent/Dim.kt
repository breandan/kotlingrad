package edu.umontreal.kotlingrad.dependent

open class `0`(override val i: Int = 0): `1`(i) { companion object: `0`(), Nat<`0`> }

sealed class `1`(override val i: Int = 1): `2`(i) { companion object: `1`(), Nat<`1`> }

sealed class `2`(override val i: Int = 2): `3`(i) { companion object :`2`(), Nat<`2`> }

sealed class `3`(override val i: Int = 3): `4`(i) { companion object: `3`(), Nat<`3`> }

sealed class `4`(override val i: Int = 4): `5`(i) { companion object: `4`(), Nat<`4`> }

sealed class `5`(override val i: Int = 5): `6`(i) { companion object: `5`(), Nat<`5`> }

sealed class `6`(open val i: Int = 6) { companion object: `6`(), Nat<`6`> }

interface Nat<T: `6`> {
  val i: Int
}