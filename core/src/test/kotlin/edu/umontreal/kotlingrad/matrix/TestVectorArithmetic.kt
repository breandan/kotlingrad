package edu.umontreal.kotlingrad.matrix

import edu.umontreal.kotlingrad.DoubleGenerator
import edu.umontreal.kotlingrad.experimental.DoublePrecision
import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec
import kotlin.math.pow

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestVectorArithmetic: StringSpec({
  val gen = DoubleGenerator
  with(DoublePrecision) {
    "a ʘ b" {
      gen.assertAll { v1: Double, v2: Double, v3: Double, v4: Double ->
        val a = Vec(v1, v2)
        val b = Var2("t")
        val d = Vec(v3, v4)
        val c = a ʘ b
        val s = c(b to d)
        s shouldBeAbout Vec(v1 * v3, v2 * v4)
      }
    }

    "a + b" {
      gen.assertAll { v1: Double, v2: Double, v3: Double, v4: Double ->
        val a = Vec(v1, v2)
        val b = Var2("t")
        val d = Vec(v3, v4)
        val c = a + b
        val s = c(b to d)()
        s shouldBeAbout Vec(v1 + v3, v2 + v4)
      }
    }

    "sum contents" {
      gen.assertAll { v1: Double, v2: Double, v3: Double, v4: Double ->
        val a = Vec(v1, v2, v3, v4)
        val b = Var4("t")
        val c = b.sum()
        val s = c(b to a)()
        s shouldBeAbout v1 + v2 + v3 + v4
      }
    }

    "magnitude" {
      gen.assertAll { v1: Double, v2: Double, v3: Double, v4: Double ->
        val a = Vec(v1, v2, v3, v4)
        val b = Var4("t") * 1
        val s = b(b to a)
        s.magnitude() shouldBeAbout kotlin.math.sqrt(v1.pow(2) + v2.pow(2) + v3.pow(2) + v4.pow(2))
      }
    }
  }
})