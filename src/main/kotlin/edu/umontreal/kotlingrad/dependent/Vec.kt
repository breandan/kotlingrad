package edu.umontreal.kotlingrad.dependent

// Supports vectors of up to length 6
open class Vec<E, MaxLength: `15`> internal constructor(val length: Nat<MaxLength>, val contents: List<E> = listOf()) {
  operator fun get(i: `9`): E = contents[i.i]
  operator fun get(i: Int): E = contents[i]

  companion object {
    operator fun <T> invoke(): Vec<T, `0`> = Vec(`0`, arrayListOf())
    operator fun <T> invoke(t: T): Vec<T, `1`> = Vec(`1`, arrayListOf(t))
    operator fun <T> invoke(t0: T, t1: T): Vec<T, `2`> = Vec(`2`, arrayListOf(t0, t1))
    operator fun <T> invoke(t0: T, t1: T, t2: T): Vec<T, `3`> = Vec(`3`, arrayListOf(t0, t1, t2))
    operator fun <T> invoke(t0: T, t1: T, t2: T, t3: T): Vec<T, `4`> = Vec(`4`, arrayListOf(t0, t1, t2, t3))
    operator fun <T> invoke(t0: T, t1: T, t2: T, t3: T, t4: T): Vec<T, `5`> = Vec(`5`, arrayListOf(t0, t1, t2, t3, t4))
    operator fun <T> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T): Vec<T, `6`> = Vec(`6`, arrayListOf(t0, t1, t2, t3, t4, t5))
    operator fun <T> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T): Vec<T, `7`> = Vec(`7`, arrayListOf(t0, t1, t2, t3, t4, t5, t6))
    operator fun <T> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T): Vec<T, `8`> = Vec(`8`, arrayListOf(t0, t1, t2, t3, t4, t5, t6, t7))
    operator fun <T> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T): Vec<T, `9`> = Vec(`9`, arrayListOf(t0, t1, t2, t3, t3, t4, t5, t6, t7, t8))
  }


  override fun toString() = "$contents"
}