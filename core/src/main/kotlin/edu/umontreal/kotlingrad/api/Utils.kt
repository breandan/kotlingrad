package edu.umontreal.kotlingrad.api

import edu.mcgill.kaliningraph.*
import guru.nidi.graphviz.engine.*
import guru.nidi.graphviz.engine.Format.SVG
import java.io.File

fun Fun<*>.saveToFile(filename: String) =
  filename.split(".").last().toUpperCase().let { ext ->
    when (ext) {
      "gv" -> Format.DOT
      else -> Format.valueOf(ext)
    }
  }.let { format -> render(format).toFile(File(filename)) }

fun Fun<*>.render(format: Format = SVG) = toGraph().toGraphviz().render(format)

fun Fun<*>.show(name: String = "temp") = toGraph().show(name)

var EAGER = false