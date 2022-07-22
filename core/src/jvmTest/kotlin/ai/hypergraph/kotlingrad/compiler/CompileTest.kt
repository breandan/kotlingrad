package ai.hypergraph.kotlingrad.compiler

import com.tschuchort.compiletesting.*
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.*
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/*
./gradlew jvmTest --tests "ai.hypergraph.kotlingrad.compiler.CompileTest"
*/
class CompileTest {
  @Test
  fun testArityError() {
    testCompile("""val o = x + z + 0.0; val k = o(z to 4.0)(z to 3.0)""", COMPILATION_ERROR)
    testCompile("""val o = x + z + 0.0; val k = o(z to 4.0)(x to 3.0)""", OK)
  }

  @Test
  fun testBinaryArithmetic() {
    testCompile( """val t: T<T<F<T<T<T<Ø>>>>>> = T.T.T * T.F.F.T""", COMPILATION_ERROR)
    testCompile( """val t: T<T<T<T<T<T<Ø>>>>>> = T.T.T * T.F.F.T""", OK)
  }

  fun testCompile(@Language("kotlin") contents: String, exitCode: ExitCode) {
    val kotlinSource = SourceFile.kotlin(
      "KClass.kt", """
        import ai.hypergraph.kotlingrad.typelevel.binary.*
        import ai.hypergraph.kotlingrad.typelevel.arity.*
        
        $contents
    """.trimIndent())

    val result = KotlinCompilation().apply {
      sources = listOf(kotlinSource)
      inheritClassPath = true
      messageOutputStream = System.out // see diagnostics in real time
    }.compile()

    assertEquals(exitCode, result.exitCode)
  }
}