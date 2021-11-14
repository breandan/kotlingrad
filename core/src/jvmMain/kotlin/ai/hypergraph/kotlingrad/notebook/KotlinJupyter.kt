package ai.hypergraph.kotlingrad.notebook

import ai.hypergraph.kaliningraph.SpsMat
import ai.hypergraph.kaliningraph.circuits.Gate
import ai.hypergraph.kaliningraph.html
import ai.hypergraph.kaliningraph.matToImg
import ai.hypergraph.kaliningraph.tensor.BooleanMatrix
import ai.hypergraph.kaliningraph.typefamily.*
import ai.hypergraph.kaliningraph.types.*
import ai.hypergraph.kotlingrad.api.SFun
import ai.hypergraph.kotlingrad.api.SVar
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration

internal class Integration: JupyterIntegration() {
  override fun Builder.onLoaded() {
    listOf(
            "ai.hypergraph.kotlingrad.api.*",
            "ai.hypergraph.kaliningraph.*",
            "ai.hypergraph.kaliningraph.tensor.*",
            "ai.hypergraph.kaliningraph.circuits.*",
            "org.ejml.data.*",
            "org.ejml.kotlin.*"
    ).forEach { import(it) }

    render<SVar<*>> { HTML(it.toGate().graph.html()) }
    render<SFun<*>> { HTML(it.toGate().graph.html()) }
    render<BooleanMatrix> { HTML("<img src=\"${it.matToImg()}\"/>") }
    render<Graph<*, *, *>> { HTML(it.html()) }
    render<Gate> { HTML(it.graph.html()) }
    render<SpsMat> { HTML("<img src=\"${it.matToImg()}\"/>") }

    // https://github.com/Kotlin/kotlin-jupyter/blob/master/docs/libraries.md#integration-using-kotlin-api
    // https://github.com/nikitinas/dataframe/blob/master/src/main/kotlin/org/jetbrains/dataframe/jupyter/Integration.kt
    // https://github.com/mipt-npm/visionforge/blob/dev/demo/jupyter-playground/src/main/kotlin/hep/dataforge/playground/VisionForgePlayGroundForJupyter.kt
  }
}