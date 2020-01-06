package edu.umontreal.kotlingrad.evaluation

import edu.umontreal.kotlingrad.calculus.ExpressionGenerator
import edu.umontreal.kotlingrad.samples.*
import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec
import javax.script.ScriptEngineManager

@Suppress("NonAsciiCharacters")
class TestSymbolic : StringSpec({
  fun ktEval(f: Fun<DoubleReal>, vararg bnds: Pair<Var<DoubleReal>, Number>) =
    ScriptEngineManager().getEngineByExtension("kts").run {
      bnds.forEach { eval("val ${it.first} = ${it.second.toDouble()}") }
      eval(f.toString())
    }

  with(DoublePrecision) {
    "test symbolic evaluation" {
      ExpressionGenerator.assertAll(100) { f: Fun<DoubleReal> ->
        try {
          f(x to 1, y to 1, z to 1) shouldBeAbout ktEval(f, x to 1, y to 1, z to 1)
        } catch (e: Exception) {
          System.err.println("Failed on expression: $f")
          throw e
        }
      }
    }
  }
})