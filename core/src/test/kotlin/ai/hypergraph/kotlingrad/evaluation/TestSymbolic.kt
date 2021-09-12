package ai.hypergraph.kotlingrad.evaluation

import ai.hypergraph.kotlingrad.*
import ai.hypergraph.kotlingrad.api.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.*
import javax.script.*
import javax.script.ScriptContext.ENGINE_SCOPE

@Suppress("NonAsciiCharacters")
class TestSymbolic : StringSpec({
  val engine = ScriptEngineManager().getEngineByExtension("kts")!!

  val x by SVar(DReal)
  val y by SVar(DReal)
  val z by SVar(DReal)

  fun ktf(f: SFun<DReal>, vararg kgBnds: Pair<SVar<DReal>, Number>) =
    engine.run {
      try {
        val bnds = kgBnds.associate { it.first.name to it.second.toDouble() }
        setBindings(SimpleBindings(bnds), ENGINE_SCOPE)
        val expr = "import kotlin.math.*; $f"
        eval(expr)
      } catch (e: Exception) {
        System.err.println("Failed to evaluate expression: $f")
        throw e
      }
    }

  "test symbolic evaluation".config(enabled = false) {
    checkAll(10, TestExpressionGenerator(DReal)) { f ->
      checkAll(3, DoubleGenerator, DoubleGenerator, DoubleGenerator) { ẋ, ẏ, ż ->
        f(x to ẋ, y to ẏ, z to ż) shouldBeAbout ktf(f, x to ẋ, y to ẏ, z to ż)
      }
    }
  }
})

class TestExpressionGenerator<X : RealNumber<X, *>>(proto: X) : Arb<SFun<X>>() {
  val expGen = ExpressionGenerator<X>()

  override fun sample(rs: RandomSource): Sample<SFun<X>> =
    Sample(expGen.randomBiTree())

  override fun edgecase(rs: RandomSource) = null
}