package edu.umontreal.kotlingrad.evaluation

import edu.umontreal.kotlingrad.calculus.DoubleGenerator
import edu.umontreal.kotlingrad.calculus.ExpressionGenerator
import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.shouldBeAbout
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec
import javax.script.ScriptContext
import javax.script.ScriptEngineManager
import javax.script.SimpleBindings

@Suppress("NonAsciiCharacters")
class TestSymbolic : StringSpec({
  val engine = ScriptEngineManager().getEngineByExtension("kts")

  fun ktEval(f: Fun<DReal>, vararg kgBnds: Pair<Var<DReal>, Number>) =
    engine.run {
      val bindings = kgBnds.map { it.first.name to it.second.toDouble() }.toMap()
      setBindings(SimpleBindings(bindings), ScriptContext.ENGINE_SCOPE)
      eval(f.toString())
    }

  with(DoublePrecision) {
    "test symbolic evaluation" {
      ExpressionGenerator.assertAll(10) { f: Fun<DReal> ->
        try {
          DoubleGenerator.assertAll(10) { ẋ, ẏ, ż ->
            f(x to ẋ, y to ẏ, z to ż) shouldBeAbout ktEval(f, x to ẋ, y to ẏ, z to ż)
          }
        } catch (e: Exception) {
          System.err.println("Failed on expression: $f")
          throw e
        }
      }
    }
  }
})