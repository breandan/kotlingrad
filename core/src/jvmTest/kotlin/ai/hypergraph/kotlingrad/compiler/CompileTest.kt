package ai.hypergraph.kotlingrad.compiler

import com.tschuchort.compiletesting.*
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/*
./gradlew jvmTest --tests "ai.hypergraph.kotlingrad.compiler.CompileTest"
*/
class CompileTest {
  @Test
  fun testClassVisibleToTestEnv() {
    val kotlinSource = SourceFile.kotlin(
      "KClass.kt", """
        import ai.hypergraph.kaliningraph.graphs.*
        
        class KClass {
            fun foo() = LabeledGraph { d - e }
        }
    """
    )

    val javaSource = SourceFile.java(
      "JClass.java", """
        import ai.hypergraph.kaliningraph.graphs.*;

        public class JClass {
            public void bar() {
                // compiled Kotlin classes are visible to Java sources
                KClass kClass = new KClass(); 
                LabeledGraph lg = kClass.foo();
            }
	    }
    """)

    val result = KotlinCompilation().apply {
      sources = listOf(kotlinSource, javaSource)

      inheritClassPath = true
      messageOutputStream = System.out // see diagnostics in real time
    }.compile()

    assertEquals(OK, result.exitCode)
  }

  @Test
  fun testArityError() {
    val kotlinSource = SourceFile.kotlin(
      "KClass.kt", """
        import ai.hypergraph.kotlingrad.typelevel.arity.*
        
        fun testArity() {
          val o = x + z + 0.0
          val k = o(z to 4.0)(z to 3.0) // Does not compile 
        }
    """
    )

    val result = KotlinCompilation().apply {
      sources = listOf(kotlinSource)

      inheritClassPath = true
      messageOutputStream = System.out // see diagnostics in real time
    }.compile()

    assertEquals(COMPILATION_ERROR, result.exitCode)
  }

  @Test
  fun testBinaryArithmeticError() {
    val kotlinSource = SourceFile.kotlin(
      "KClass.kt", """
        import ai.hypergraph.kotlingrad.typelevel.binary.*
        
        fun testBinaryArithmetic() {
          //         ┌──[Error here]
          val t: T<T<F<T<T<T<Ø>>>>>> = T.T.T * T.F.F.T
        }
    """
    )

    val result = KotlinCompilation().apply {
      sources = listOf(kotlinSource)

      inheritClassPath = true
      messageOutputStream = System.out // see diagnostics in real time
    }.compile()

    assertEquals(COMPILATION_ERROR, result.exitCode)
  }
}