package edu.umontreal.kotlingrad.evaluation

import edu.umontreal.kotlingrad.*
import edu.umontreal.kotlingrad.api.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll

@Suppress("NonAsciiCharacters")
class TestArithmetic: StringSpec({
  val x by SVar(DReal)
  val y by SVar(DReal)
  val z by SVar(DReal)

  "test addition" {
    checkAll(DoubleGenerator, DoubleGenerator) { ẋ, ẏ ->
      (x + y).invoke(x to ẋ, y to ẏ) shouldBeAbout ẏ + ẋ
    }
  }

  "test subtraction" {
    checkAll(DoubleGenerator, DoubleGenerator) { ẋ, ẏ ->
      (x - y).invoke(x to ẋ, y to ẏ) shouldBeAbout ẋ - ẏ
    }
  }

  "test exponentiation" {
    checkAll(DoubleGenerator) { ẋ ->
      (x pow 3)(ẋ) shouldBeAbout (x * x * x)(ẋ)
    }
  }

  "test unary minus" {
    checkAll(DoubleGenerator, DoubleGenerator) { ẋ, ẏ ->
      (-y + x)(x to ẋ, y to ẏ) shouldBeAbout ẋ - ẏ
      (y + -x)(x to ẋ, y to ẏ) shouldBeAbout ẏ - ẋ
    }
  }

  "test unary plus" {
    checkAll(DoubleGenerator, DoubleGenerator) { ẋ, ẏ ->
      (+y + x)(x to ẋ, y to ẏ) shouldBeAbout ẋ + ẏ
      (y + +x)(x to ẋ, y to ẏ) shouldBeAbout ẋ + ẏ
    }
  }

  "test multiplication" {
    checkAll(DoubleGenerator, DoubleGenerator) { ẋ, ẏ ->
      (x * y)(x to ẋ, y to ẏ) shouldBeAbout ẋ * ẏ
    }
  }

  "test multiplication with numerical type" {
    checkAll(DoubleGenerator, DoubleGenerator) { ẋ, ẏ ->
      (x * 2)(ẋ) shouldBeAbout ẋ * 2
    }
  }

  "test division" {
    checkAll(DoubleGenerator, DoubleGenerator(0)) { ẋ, ẏ ->
      (x / y)(x to ẋ, y to ẏ) shouldBeAbout ẋ / ẏ
    }
  }

  "test inverse" {
    checkAll(DoubleGenerator, DoubleGenerator(0)) { ẋ, ẏ ->
      val f = x * 1 / y
      val g = x / y
      f(x to ẋ, y to ẏ) shouldBeAbout g(x to ẋ, y to ẏ)
    }
  }

  "test associativity" {
    checkAll(DoubleGenerator, DoubleGenerator, DoubleGenerator) { ẋ, ẏ, ż ->
      val f = x * (y * z)
      val g = (x * y) * z
      f(x to ẋ, y to ẏ, z to ż) shouldBeAbout g(x to ẋ, y to ẏ, z to ż)
    }
  }

  "test commutativity" {
    checkAll(DoubleGenerator, DoubleGenerator, DoubleGenerator) { ẋ, ẏ, ż ->
      val f = x * y * z
      val g = z * y * x
      f(x to ẋ, y to ẏ, z to ż) shouldBeAbout g(x to ẋ, y to ẏ, z to ż)
    }
  }

  "test distributivity" {
    checkAll(DoubleGenerator, DoubleGenerator, DoubleGenerator) { ẋ, ẏ, ż ->
      val f = x * (y + z)
      val g = x * y + x * z
      f(x to ẋ, y to ẏ, z to ż) shouldBeAbout g(x to ẋ, y to ẏ, z to ż)
    }
  }

//  "test compositional associativity".config(enabled = false) {
//    checkAll(DoubleGenerator, DoubleGenerator, DoubleGenerator) { ẋ, ẏ, ż ->
//      ẋ, ẏ, ż ->
//      val f = 4 * z + x
//      val g = 3 * y + z
//      val h = 2 * x + y
//      val fogoho = f(x to g, y to h)
//      val fo_goh = f(x to g(y to h))
//      val fog_oh = f(x to g)(y to h)
//
//      fogoho(x to ẋ, y to ẏ, z to ż) shouldBeAbout fo_goh(x to ẋ, y to ẏ, z to ż)
//      fo_goh(x to ẋ, y to ẏ, z to ż) shouldBeAbout fog_oh(x to ẋ, y to ẏ, z to ż)
//      fog_oh(x to ẋ, y to ẏ, z to ż) shouldBeAbout fogoho(x to ẋ, y to ẏ, z to ż)
//    }
//  }
})