package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

@Suppress("NonAsciiCharacters", "LocalVariableName")
class TestLogarithmicDerivatives: StringSpec({
  with(BigDecimalFunctor) {
    val n = variable("n")

    "dnⁿ / dn should be nⁿ * (ln(n) + 1)" {
      assertAll(NumericalGenerator(0)) { nVal: Double ->
        val df_dn = d(n.pow(n)) / d(n)
        println(df_dn)
        val manualDerivative = n.pow(n) * (n.ln() + 1)
        df_dn(n to nVal).toDouble() shouldBeAbout manualDerivative(nVal).toDouble()
      }
    }
  }

  with(DoubleFunctor) {
    val n = variable("n")

    "dn³ / dn should be 3n²" {
      assertAll(NumericalGenerator(0)) { nVal: Double ->
        val df_dn = d(pow(n, 3)) / d(n)
        val manualDerivative = 3 * pow(n, 2)
        df_dn(n to nVal).toDouble() shouldBeAbout manualDerivative(nVal).toDouble()
      }
    }
  }
})