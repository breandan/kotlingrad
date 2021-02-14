package edu.umontreal.kotlingrad.evaluation

import edu.umontreal.kotlingrad.*
import edu.umontreal.kotlingrad.api.*
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec
import javax.script.*
import javax.script.ScriptContext.ENGINE_SCOPE

@Suppress("NonAsciiCharacters")
class TestSymbolic : StringSpec({
  val engine = ScriptEngineManager().getEngineByExtension("kts")

  val x by SVar(DReal)
  val y by SVar(DReal)
  val z by SVar(DReal)

  fun ktf(f: SFun<DReal>, vararg kgBnds: Pair<SVar<DReal>, Number>) =
    engine.run {
      try {
        val bnds = kgBnds.associate { it.first.name to it.second.toDouble() }
        setBindings(SimpleBindings(bnds), ENGINE_SCOPE)
        eval("import kotlin.math.*; $f")
      } catch (e: Exception) {
        System.err.println("Failed to evaluate expression: $f")
        throw e
      }
    }

  "test symbolic evaluation" {
    TestExpressionGenerator(DReal).assertAll(20) { f: SFun<DReal> ->
      DoubleGenerator.assertAll(20) { ẋ, ẏ, ż ->
        f(x to ẋ, y to ẏ, z to ż) shouldBeAbout ktf(f, x to ẋ, y to ẏ, z to ż)
      }
    }
  }
})