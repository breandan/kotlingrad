package co.ndan.kotlingrad.math.types.dependent

// Supports lists of up to length 6
open class Array<T, Size: `6`> constructor(val contents: List<T> = arrayListOf()) {
  companion object {
    operator fun <T> invoke(): Array<T, `0`> = Array(listOf())
    operator fun <T> invoke(t: T): Array<T, `1`> = Array(listOf(t))
    operator fun <T> invoke(t0: T, t1: T): Array<T, `2`> = Array(listOf(t0, t1))
    operator fun <T> invoke(t0: T, t1: T, t2: T): Array<T, `3`> = Array(listOf(t0, t1, t2))
    operator fun <T> invoke(t0: T, t1: T, t2: T, t3: T): Array<T, `4`> = Array(listOf(t0, t1, t2, t3))
    operator fun <T> invoke(t0: T, t1: T, t2: T, t3: T, t4: T): Array<T, `5`> = Array(listOf(t0, t1, t2, t3, t4))
    operator fun <T> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T): Array<T, `6`> = Array(listOf(t0, t1, t2, t3, t4, t5))
  }

  override fun toString(): String { return contents.toString() }
}

