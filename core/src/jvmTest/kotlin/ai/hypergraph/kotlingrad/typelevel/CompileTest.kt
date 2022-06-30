package ai.hypergraph.kotlingrad.typelevel

import com.tschuchort.compiletesting.*
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/*
./gradlew jvmTest --tests "ai.hypergraph.kotlingrad.typelevel.CompileTest"
*/
class CompileTest {
  class TestEnvClass {}

  @Test
  fun testClassVisibleToTestEnv() {
    val kotlinSource = SourceFile.kotlin(
      "KClass.kt", """
        class KClass {
            fun foo() {
                // Classes from the test environment are visible to the compiled sources
                val testEnvClass = TestEnvClass() 
            }
        }
    """
    )

    val javaSource = SourceFile.java(
      "JClass.java", """
        public class JClass {
            public void bar() {
                // compiled Kotlin classes are visible to Java sources
                KClass kClass = new KClass(); 
            }
	    }
    """)

    val result = KotlinCompilation().apply {
      sources = listOf(kotlinSource, javaSource)

      inheritClassPath = true
      messageOutputStream = System.out // see diagnostics in real time
    }.compile()

    assertEquals(result.exitCode, ExitCode.COMPILATION_ERROR)
  }
}