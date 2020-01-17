package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.DoublePrecision
import edu.umontreal.kotlingrad.experimental.SFun
import guru.nidi.graphviz.*
import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT
import guru.nidi.graphviz.engine.Format.SVG
import java.awt.FlowLayout
import java.io.File
import javax.swing.*

fun main() {
  with(DoublePrecision) {
    val t = (1 + x * 2 - 3 + y + z / y).d(y).d(x) + z / y * 3 - 2
    t.render("$resourcesPath/dataflow.svg")
  }
}

const val DARKMODE = false
const val THICKNESS = 2

fun SFun<*>.render(filename: String? = null) {
  val image = graph(directed = true) {
    val color = if (DARKMODE) Color.WHITE else Color.BLACK

    edge[color, Arrow.NORMAL, Style.lineWidth(THICKNESS)]

    graph[Rank.dir(LEFT_TO_RIGHT), Color.TRANSPARENT.background()]

    node[color, color.font(), Font.config("Helvetica", 20),
        Style.lineWidth(THICKNESS)]

    toGraph()
  }.toGraphviz().render(SVG).run {
    if (filename == null)
      toImage().let { image ->
        JFrame().apply {
          contentPane.layout = FlowLayout()
          contentPane.add(JLabel(ImageIcon(image)))
          pack()
          isVisible = true
        }
      }
    else toFile(File(filename))
  }
}