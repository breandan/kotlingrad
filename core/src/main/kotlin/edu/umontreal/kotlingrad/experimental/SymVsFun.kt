package edu.umontreal.kotlingrad.experimental

import java.util.function.BiFunction
import java.util.function.Function

class QA: BiFunction<Int, Int, Int> {
  override fun apply(t: Int, u: Int): Int = t + u
}

class QT: Function<Int, Int> {
  override fun apply(t: Int): Int = t + 2
}

fun main() {
  QA().andThen(QT())
}

