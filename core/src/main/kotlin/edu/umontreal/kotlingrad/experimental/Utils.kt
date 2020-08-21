package edu.umontreal.kotlingrad.experimental

import edu.mcgill.kaliningraph.browserCmd
import edu.mcgill.kaliningraph.show
import guru.nidi.graphviz.*
import guru.nidi.graphviz.engine.*
import guru.nidi.graphviz.engine.Format.SVG
import java.io.File

fun Fun<*>.saveToFile(filename: String) =
  Format.valueOf(filename.split(".").last().toUpperCase())
    .let { format -> toGraphviz().render(format).saveToFile(filename) }
fun Renderer.saveToFile(filename: String) = File(filename).writeText(toString().replace("]", "];"))

fun Fun<*>.render(format: Format = SVG) = toGraphviz().render(format)

fun Fun<*>.toGraphviz(): Graphviz = toGate().graph.render().toGraphviz()
fun Fun<*>.show(name: String = "temp") = render().toFile(File.createTempFile(name, ".svg")).show()

fun Fun<*>.html() = render(SVG).toString()
fun File.show() = ProcessBuilder(browserCmd, path).start()

var EAGER = false