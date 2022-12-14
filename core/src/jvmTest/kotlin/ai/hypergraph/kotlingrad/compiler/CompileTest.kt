package ai.hypergraph.kotlingrad.compiler

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import javax.script.ScriptEngineManager

/*
./gradlew jvmTest --tests "ai.hypergraph.kotlingrad.compiler.CompileTest"
*/
class CompileTest {
  @Test
  fun testArityError() {
    testCompile("""val o = x + z + 0.0; val k = o(z to 4.0)(z to 3.0)""", false)
    testCompile("""val o = x + z + 0.0; val k = o(z to 4.0)(x to 3.0)""", true)
  }

  @Test
  fun testBinaryArithmetic() {
    testCompile( """val t: T<T<F<T<T<T<Ø>>>>>> = T.T.T * T.F.F.T""", false)
    testCompile( """val t: T<T<T<T<T<T<Ø>>>>>> = T.T.T * T.F.F.T""", true)
  }

  val engine = ScriptEngineManager().getEngineByExtension("kts")!!
  fun testCompile(@Language("kotlin") contents: String, shouldCompile: Boolean) {
    engine.run {
      try {
        eval(
          """
            import ai.hypergraph.kotlingrad.typelevel.binary.*
            import ai.hypergraph.kotlingrad.typelevel.arity.*
            $contents
          """.trimIndent()
        )
      } catch (e: Exception) {
        if(shouldCompile) throw e else return
      }
    }
  }
}