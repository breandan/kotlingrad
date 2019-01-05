package co.ndan.kotlingrad.math.types.dependent

// Supports vectors of up to length 6
open class Vector<T, MaxLength: `6`> constructor(val contents: List<T> = arrayListOf()) {
  companion object {
    operator fun <T> invoke(): Vector<T, `0`> = Vector(arrayListOf())
    operator fun <T> invoke(t: T): Vector<T, `1`> = Vector(arrayListOf(t))
    operator fun <T> invoke(t0: T, t1: T): Vector<T, `2`> = Vector(arrayListOf(t0, t1))
    operator fun <T> invoke(t0: T, t1: T, t2: T): Vector<T, `3`> = Vector(arrayListOf(t0, t1, t2))
    operator fun <T> invoke(t0: T, t1: T, t2: T, t3: T): Vector<T, `4`> = Vector(arrayListOf(t0, t1, t2, t3))
    operator fun <T> invoke(t0: T, t1: T, t2: T, t3: T, t4: T): Vector<T, `5`> = Vector(arrayListOf(t0, t1, t2, t3, t4))
    operator fun <T> invoke(t0: T, t1: T, t2: T, t3: T, t4: T, t5: T): Vector<T, `6`> = Vector(arrayListOf(t0, t1, t2, t3, t4, t5))
  }

  override fun toString() = "$contents"
}