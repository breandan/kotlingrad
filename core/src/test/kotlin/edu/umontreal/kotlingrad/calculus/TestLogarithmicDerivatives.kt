package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.DoubleGenerator
import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestLogarithmicDerivatives: StringSpec({
  val gen = DoubleGenerator(0)
  val n by SVar(DReal)

  "dnⁿ / dn should be nⁿ * (ln(n) + 1)" {
    gen.assertAll { nVal: Double ->
      val `df∕dn` = d(n.pow(n)) / d(n)
      val manualDerivative = n.pow(n) * (n.ln() + 1)
      `df∕dn`(n to nVal) shouldBeAbout manualDerivative(nVal)
    }
  }

  "dn³ / dn should be 3n²" {
    gen.assertAll { nVal: Double ->
      val `df∕dn` = d(pow(n, 3)) / d(n)
      val manualDerivative = 3 * pow(n, 2)
      `df∕dn`(n to nVal) shouldBeAbout manualDerivative(nVal)
    }
  }
})