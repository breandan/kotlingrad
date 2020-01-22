package edu.umontreal.kotlingrad.samples

import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.plot.MonolithicAwt
import jetbrains.letsPlot.geom.geom_path
import jetbrains.letsPlot.ggplot
import jetbrains.letsPlot.ggtitle
import jetbrains.letsPlot.intern.toSpec
import java.io.File

fun main() {
  val data = mapOf<String, Any>(
    "x" to List(5) { it },
    "y" to List(5) { it * it }
  )

  // Create plot specs using Lets-Plot Kotlin API
  val geom = geom_path(alpha = 1.0, size = 2.0) { x = "x"; y = "y" }
  val plot = ggplot(data) + geom + ggtitle("y = x^2")

  // Create JFXPanel showing the plot.
  val plotSpec = plot.toSpec()
  val plotSize = DoubleVector(1000.0, 500.0)

  val component = MonolithicAwt.buildSvgImagesFromRawSpecs(plotSpec, plotSize) {}
  component.first().saveAs("letsPlot.html").viewInBrowser()
}

private fun String.saveAs(filename: String) =
  File("$resourcesPath/$filename").also { it.writeText(this) }