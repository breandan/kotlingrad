package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.DoublePrecision
import edu.umontreal.kotlingrad.experimental.SFun
import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.attribute.Rank.RankDir.*
import guru.nidi.graphviz.*
import guru.nidi.graphviz.engine.Format.SVG
import guru.nidi.graphviz.engine.Renderer
import java.io.File

fun main() {
  with(DoublePrecision) {
    val t = (1 + x * 2 - 3 + y + z / y).d(y).d(x) + z / y * 3 - 2
    t.render().saveToFile("dataflow.svg").viewInBrowser()
  }
}

const val DARKMODE = false
const val THICKNESS = 2

fun SFun<*>.render() =
  graph(directed = true) {
    val color = if (DARKMODE) Color.WHITE else Color.BLACK

    edge[color, Arrow.NORMAL, Style.lineWidth(THICKNESS)]

    graph[Rank.dir(TOP_TO_BOTTOM), Color.TRANSPARENT.background()]

    node[color, color.font(), Font.config("Helvetica", 20),
      Style.lineWidth(THICKNESS)]

    toGraph()
  }.toGraphviz().render(SVG)

fun Renderer.saveToFile(filename: String) =
  toFile(File("$resourcesPath/$filename"))