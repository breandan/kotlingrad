package edu.umontreal.kotlingrad.dependent

// Vec should not be a List or the concatenation operator + will conflict with vector addition
open class Vec<E, MaxLength : `100`>(val length: Nat<MaxLength>, val contents: List<E> = arrayListOf()) {
  init {
    require(length.i == contents.size) { "Declared $length, but found ${contents.size}" }
  }

  operator fun get(i: Int): E = contents[i]

  fun magnitude() : E = TODO()

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
    operator fun <T> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T, t6: T, t7: T, t8: T): Vec<T, `9`> = Vec(`9`, arrayListOf(t0, t1, t2, t3, t4, t5, t6, t7, t8))
  }

  override fun toString() = "$contents"
}