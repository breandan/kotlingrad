package co.ndan.kotlingrad.math.experimental

interface Expression<T> {
  operator fun invoke(arguments: Map<String, T>): T
}