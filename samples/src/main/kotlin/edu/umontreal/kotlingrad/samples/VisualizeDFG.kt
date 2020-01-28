package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.DoublePrecision

fun main() {
  with(DoublePrecision) {
    val t = (1 + x * 2 - 3 + y + z / y).d(y).d(x) + z / y * 3 - 2
    t.render().saveToFile("dataflow.svg").show()
  }
}