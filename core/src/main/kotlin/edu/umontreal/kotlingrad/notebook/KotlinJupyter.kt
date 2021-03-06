package edu.umontreal.kotlingrad.notebook

import org.jetbrains.kotlinx.jupyter.api.annotations.JupyterLibrary
import org.jetbrains.kotlinx.jupyter.api.*
import org.jetbrains.kotlinx.jupyter.api.libraries.*

@JupyterLibrary
// https://github.com/Kotlin/kotlin-jupyter/blob/master/docs/libraries.md#integration-using-kotlin-api
internal class Integration: JupyterIntegration() {
  override fun Builder.onLoaded() {
    listOf(
      "edu.umontreal.kotlingrad.api.*",
      "edu.mcgill.kaliningraph.*"
    ).forEach { import(it) }
//    render<MyClass> { HTML(it.toHTML()) }
    // https://github.com/nikitinas/dataframe/blob/master/src/main/kotlin/org/jetbrains/dataframe/jupyter/Integration.kt
  }
}