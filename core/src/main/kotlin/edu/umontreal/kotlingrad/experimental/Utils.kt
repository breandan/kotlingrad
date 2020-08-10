package edu.umontreal.kotlingrad.experimental

import edu.mcgill.kaliningraph.show
import guru.nidi.graphviz.*
import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.attribute.Color.*
import guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT
import guru.nidi.graphviz.engine.*
import guru.nidi.graphviz.engine.Format.SVG
import guru.nidi.graphviz.model.*
import java.io.File

val DARKMODE = false
val THICKNESS = 2

inline fun render(format: Format = SVG, crossinline op: () -> MutableNode) =
  graph(directed = true) {
    val color = if (DARKMODE) WHITE else BLACK

    edge[color, Arrow.NORMAL, Style.lineWidth(THICKNESS)]

    graph[Rank.dir(LEFT_TO_RIGHT), TRANSPARENT.background(), GraphAttr.margin(0.0), Attributes.attr("compound", "true"), Attributes.attr("nslimit", "20")]

    node[color, color.font(), Font.config("Lucida Console", 20), Style.lineWidth(THICKNESS), Attributes.attr("shape", "Mrecord")]

    op()
  }.toGraphviz().render(format)

fun SFun<*>.saveToFile(filename: String) =
  Format.valueOf(filename.split(".").last().toUpperCase())
    .let { format -> render(format) { toGraph() }.saveToFile(filename) }
fun SFun<*>.render(format: Format = SVG) = render(format) { toGraph() }
fun Renderer.saveToFile(filename: String) = File(filename).writeText(toString().replace("]", "];"))

fun SFun<*>.show() = toKGraph().graph.show()
fun Fun<*>.show(name: String = "temp") = render { toGraph() }.show(name)

fun Fun<*>.html() = render { toGraph() }.toString()
fun Renderer.show(name: String) = toFile(File.createTempFile(name, ".svg")).show()
fun File.show() = ProcessBuilder("x-www-browser", path).start()

operator fun MutableNode.minus(target: LinkTarget): Link = addLink(target).links().last()!!

var EAGER = false