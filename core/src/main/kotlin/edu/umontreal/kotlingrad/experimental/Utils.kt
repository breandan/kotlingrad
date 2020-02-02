package edu.umontreal.kotlingrad.experimental

import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.edge
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Renderer
import guru.nidi.graphviz.graph
import guru.nidi.graphviz.model.MutableNode
import guru.nidi.graphviz.node
import guru.nidi.graphviz.toGraphviz
import java.io.File

val DARKMODE = false
val THICKNESS = 2

inline fun renderAsSVG(crossinline op: () -> MutableNode) =
  graph(directed = true) {
    val color = if (DARKMODE) Color.WHITE else Color.BLACK

    edge[color, Arrow.NORMAL, Style.lineWidth(THICKNESS)]

    graph[Rank.dir(Rank.RankDir.LEFT_TO_RIGHT), Color.TRANSPARENT.background()]

    node[color, color.font(), Font.config("Helvetica", 20),
      Style.lineWidth(THICKNESS)]

    op()
  }.toGraphviz().render(Format.SVG)

fun Renderer.saveToFile(filename: String) = toFile(File(filename))

fun Fun<*>.show(name: String = "temp") = renderAsSVG { toGraph() }.show(name)
fun Renderer.show(name: String) = toFile(File.createTempFile(name, ".svg")).show()
fun File.show() = ProcessBuilder("x-www-browser", path).start()