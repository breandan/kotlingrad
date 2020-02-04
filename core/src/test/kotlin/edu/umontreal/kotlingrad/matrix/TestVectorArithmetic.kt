package edu.umontreal.kotlingrad.matrix

import edu.umontreal.kotlingrad.DoubleGenerator
import edu.umontreal.kotlingrad.experimental.DoublePrecision
import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestVectorArithmetic: StringSpec({
  val gen = DoubleGenerator(positive = false, expRange = -3..3)
  with(DoublePrecision) {
    "a ʘ b" {
      gen.assertAll { v1: Double, v2: Double, v3: Double, v4: Double ->
        val a = Vec(v1, v2)
        val b = Var2("t")
        val d = Vec(v3, v4)
        val c = a ʘ b
        val s = c(b to d)
        s shouldBeAbout Vec(v1 + v3, v2 + v4)
      }
    }
  }
})