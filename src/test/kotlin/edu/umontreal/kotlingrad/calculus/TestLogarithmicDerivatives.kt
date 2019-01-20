package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.numerical.BigDecimalPrecision
import edu.umontreal.kotlingrad.numerical.DoublePrecision
import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestLogarithmicDerivatives: StringSpec({
  with(BigDecimalPrecision) {
    val n = variable("n")

    "dnⁿ / dn should be nⁿ * (ln(n) + 1)" {
      assertAll(NumericalGenerator(0)) { nVal: Double ->
        val `df∕dn` = d(n.pow(n)) / d(n)
        val manualDerivative = n.pow(n) * (n.ln() + 1)
        `df∕dn`(n to nVal) shouldBeAbout manualDerivative(nVal)
      }
    }
  }

  with(DoublePrecision) {
    val n = variable("n")

    "dn³ / dn should be 3n²" {
      assertAll(NumericalGenerator(0)) { nVal: Double ->
        val `df∕dn` = d(pow(n, 3)) / d(n)
        val manualDerivative = 3 * pow(n, 2)
        `df∕dn`(n to nVal) shouldBeAbout manualDerivative(nVal)
      }
    }
  }
})