package ai.hypergraph.kotlingrad.notebook

//import io.kotest.matchers.string.shouldContain
//import org.intellij.lang.annotations.Language
//import org.jetbrains.kotlinx.jupyter.testkit.JupyterReplTestCase
//import org.junit.jupiter.api.Test
//
///*
//./gradlew jvmTest --tests "ai.hypergraph.kotlingrad.notebook.RenderingTests"
//*/
//
//class RenderingTests: JupyterReplTestCase() {
//  @Test
//  fun `circuit is rendered to html`() {
//    @Language("kts")
//    val html = execHtml(
//      """
//            val x by SVar(DReal)
//            val y by SVar(DReal)
//            val z by SVar(DReal)
//
//            val t = (1 + x * 2 - 3 + y + z / y).d(y).d(x) + z / y * 3 - 2; t
//        """.trimIndent()
//    )
//    html shouldContain "polygon"
//  }
//}