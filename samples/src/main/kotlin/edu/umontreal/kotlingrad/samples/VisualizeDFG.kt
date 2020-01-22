package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.DoublePrecision
import edu.umontreal.kotlingrad.experimental.SFun
import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT
import guru.nidi.graphviz.edge
import guru.nidi.graphviz.engine.Format.SVG
import guru.nidi.graphviz.graph
import guru.nidi.graphviz.node
import guru.nidi.graphviz.toGraphviz
import java.io.File

fun main() {
  with(DoublePrecision) {
    val t = (1 + x * 2 - 3 + y + z / y).d(y).d(x) + z / y * 3 - 2
    t.render("$resourcesPath/dataflow.svg").viewInBrowser()
  }
}

const val DARKMODE = false
const val THICKNESS = 2

fun SFun<*>.render(filename: String) =
  graph(directed = true) {
    val color = if (DARKMODE) Color.WHITE else Color.BLACK

    edge[color, Arrow.NORMAL, Style.lineWidth(THICKNESS)]

    graph[Rank.dir(LEFT_TO_RIGHT), Color.TRANSPARENT.background()]

    node[color, color.font(), Font.config("Helvetica", 20),
      Style.lineWidth(THICKNESS)]

    toGraph()
  }.toGraphviz().render(SVG).run {
    toFile(File(filename))
  }