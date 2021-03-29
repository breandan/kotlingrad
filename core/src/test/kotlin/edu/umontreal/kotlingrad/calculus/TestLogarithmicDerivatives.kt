package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.*
import edu.umontreal.kotlingrad.api.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestLogarithmicDerivatives: StringSpec({
  val gen = DoubleGenerator(0)
  val n by SVar(DReal)

  "dnⁿ / dn should be nⁿ * (ln(n) + 1)" {
    checkAll(gen) { nVal: Double ->
      val `df∕dn` = d(n.pow(n)) / d(n)
      val manualDerivative = n.pow(n) * (n.ln() + 1)
      `df∕dn`(n to nVal) shouldBeAbout manualDerivative(nVal)
    }
  }

  "dn³ / dn should be 3n²" {
    checkAll(gen) { nVal: Double ->
      val `df∕dn` = d(pow(n, 3)) / d(n)
      val manualDerivative = 3 * pow(n, 2)
      `df∕dn`(n to nVal) shouldBeAbout manualDerivative(nVal)
    }
  }
})