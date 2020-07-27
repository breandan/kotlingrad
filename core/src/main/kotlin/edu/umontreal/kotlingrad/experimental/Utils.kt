package edu.umontreal.kotlingrad.experimental

import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.engine.Engine
import guru.nidi.graphviz.engine.Engine.DOT
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Format.SVG
import guru.nidi.graphviz.engine.Renderer
import guru.nidi.graphviz.graph
import guru.nidi.graphviz.model.Link
import guru.nidi.graphviz.model.LinkTarget
import guru.nidi.graphviz.model.MutableNode
import guru.nidi.graphviz.toGraphviz
import java.io.File

val DARKMODE = false
val THICKNESS = 2

inline fun render(format: Format = SVG, crossinline op: () -> MutableNode) =
  graph(directed = true) {
    val color = if (DARKMODE) Color.WHITE else Color.BLACK

    edge[color, Arrow.NORMAL, Style.lineWidth(THICKNESS)]

    graph[Rank.dir(Rank.RankDir.LEFT_TO_RIGHT), Color.TRANSPARENT.background(), GraphAttr.margin(0.0), Attributes.attr("compound", "true"), Attributes.attr("nslimit", "20")]

    node[color, color.font(), Font.config("Lucida Console", 20), Style.lineWidth(THICKNESS), Attributes.attr("shape", "Mrecord")]

    op()
  }.toGraphviz().render(format)

fun extToFormat(string: String): Format = when(string) {
  "dot" -> Format.DOT
  "png" -> Format.PNG
  "ps" -> Format.PS
  else -> SVG
}
fun SFun<*>.saveToFile(filename: String) =
  render(extToFormat(filename.split(".").last())) { toGraph() }.saveToFile(filename)
fun SFun<*>.render(format: Format = SVG) = render(format) { toGraph() }
fun Renderer.saveToFile(filename: String) = File(filename).writeText(toString().replace("]", "];"))
fun Fun<*>.show(name: String = "temp") = render { toGraph() }.show(name)
fun Fun<*>.html() = render { toGraph() }.toString()
fun Renderer.show(name: String) = toFile(File.createTempFile(name, ".svg")).show()
fun File.show() = ProcessBuilder("x-www-browser", path).start()

operator fun MutableNode.minus(target: LinkTarget): Link = addLink(target).links().last()!!

var EAGER = false