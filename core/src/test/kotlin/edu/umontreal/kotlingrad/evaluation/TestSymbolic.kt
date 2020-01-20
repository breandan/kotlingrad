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

  fun ktEval(f: SFun<DReal>, vararg kgBnds: Pair<Var<DReal>, Number>) =
    engine.run {
      try {
        val bnds = kgBnds.map { it.first.name to it.second.toDouble() }.toMap()
        setBindings(SimpleBindings(bnds), ScriptContext.ENGINE_SCOPE)
        eval("import kotlin.math.*; $f")
      } catch (e: Exception) {
        System.err.println("Failed to evaluate expression: $f")
        throw e
      }
    }

  with(DoublePrecision) {
    "test symbolic evaluation" {
      ExpressionGenerator(DoublePrecision).assertAll(20) { f: SFun<DReal> ->
        DoubleGenerator.assertAll(20) { ẋ, ẏ, ż ->
          f(x to ẋ, y to ẏ, z to ż) shouldBeAbout ktEval(f, x to ẋ, y to ẏ, z to ż)
        }
      }
    }
  }
})