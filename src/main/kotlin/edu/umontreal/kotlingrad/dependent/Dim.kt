package edu.umontreal.kotlingrad.dependent

open class `0`(override val i: Int = 0): `1`(i) { companion object: `0`(), Nat<`0`> }

sealed class `1`(override val i: Int = 1): `2`(i) { companion object: `1`(), Nat<`1`> }

sealed class `2`(override val i: Int = 2): `3`(i) { companion object :`2`(), Nat<`2`> }

sealed class `3`(override val i: Int = 3): `4`(i) { companion object: `3`(), Nat<`3`> }

sealed class `4`(override val i: Int = 4): `5`(i) { companion object: `4`(), Nat<`4`> }

sealed class `5`(override val i: Int = 5): `6`(i) { companion object: `5`(), Nat<`5`> }

sealed class `6`(override val i: Int = 6): `7`(i) { companion object: `6`(), Nat<`6`> }

sealed class `7`(override val i: Int = 7): `8`(i) { companion object: `7`(), Nat<`7`> }

sealed class `8`(override val i: Int = 8): `9`(i) { companion object: `8`(), Nat<`8`> }
sealed class `9`(override val i: Int = 9): `10`(i) { companion object: `9`(), Nat<`9`> }
sealed class `10`(override val i: Int = 10): `11`(i) { companion object: `10`(), Nat<`10`> }
sealed class `11`(override val i: Int = 11): `12`(i) { companion object: `11`(), Nat<`11`> }
sealed class `12`(override val i: Int = 12): `13`(i) { companion object: `12`(), Nat<`12`> }
sealed class `13`(override val i: Int = 13): `14`(i) { companion object: `13`(), Nat<`13`> }
sealed class `14`(override val i: Int = 14): `15`(i) { companion object: `14`(), Nat<`14`> }

sealed class `15`(open val i: Int = 15) { companion object: `15`(), Nat<`15`> }

interface Nat<T: `15`> {
  val i: Int
}