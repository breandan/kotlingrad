package edu.umontreal.kotlingrad.matrix

import edu.umontreal.kotlingrad.*
import edu.umontreal.kotlingrad.api.*
import edu.umontreal.kotlingrad.shapes.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll
import kotlin.math.pow

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestVectorArithmetic: StringSpec({
  val gen = DoubleGenerator

  "a ʘ b" {
    checkAll(gen, gen, gen, gen) { v1: Double, v2: Double, v3: Double, v4: Double ->
      val a = DReal.Vec(v1, v2)
      val b = DReal.Var(D2)
      val d = DReal.Vec(v3, v4)
      val c = a ʘ b
      val s = c(b to d)()
      s shouldBeAbout DReal.Vec(v1 * v3, v2 * v4)
    }
  }

  "a + b" {
    checkAll(gen, gen, gen, gen) { v1: Double, v2: Double, v3: Double, v4: Double ->
      val a = DReal.Vec(v1, v2)
      val b = DReal.Var(D2)
      val d = DReal.Vec(v3, v4)
      val c = a + b
      val s = c(b to d)()
      s shouldBeAbout DReal.Vec(v1 + v3, v2 + v4)
    }
  }

  "sum contents" {
    checkAll(gen, gen, gen, gen) { v1: Double, v2: Double, v3: Double, v4: Double ->
      val a = DReal.Vec(v1, v2, v3, v4)
      val b = DReal.Var(D4)
      val c = b.sum()
      val s = c(b to a)()
      s shouldBeAbout v1 + v2 + v3 + v4
    }
  }

  "magnitude" {
    checkAll(gen, gen, gen, gen) { v1: Double, v2: Double, v3: Double, v4: Double ->
      val a = DReal.Vec(v1, v2, v3, v4)
      val b = DReal.Var(D4)
      val c = b * 1
      val s = c(b to a)()
      s.magnitude() shouldBeAbout kotlin.math.sqrt(v1.pow(2) + v2.pow(2) + v3.pow(2) + v4.pow(2))
    }
  }
})