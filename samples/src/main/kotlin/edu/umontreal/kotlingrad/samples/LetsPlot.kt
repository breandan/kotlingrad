package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.utils.step
import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.plot.MonolithicAwt
import jetbrains.letsPlot.geom.geom_path
import jetbrains.letsPlot.ggplot
import jetbrains.letsPlot.ggtitle
import jetbrains.letsPlot.intern.toSpec
import kotlin.math.*

fun main() {
  val range = -5.0..5.0
  val xs = (range step 0.0087).toList()

  val data = mapOf<String, Any>(
    "x" to xs,
    "y" to xs.map { sin(it) }
  )

  data.plot("y = sin(x)", "hello_lets-plot.svg")
}

fun Map<String, Any>.plot(title: String, filename: String) {
  // Create plot specs using Lets-Plot Kotlin API
  val geom1 = geom_path(size = 2.0, color = "dark_green") { x = "x"; y = "y" }
//  val geom2 = geom_path(size = 2.0, color = "dark_blue") { x = "x"; y = "z" }

  val plot = ggplot(this) + geom1 + ggtitle(title)

  // Create JFXPanel showing the plot.
  val plotSpec = plot.toSpec()
  val plotSize = DoubleVector(1000.0, 500.0)

  val component = MonolithicAwt.buildSvgImagesFromRawSpecs(plotSpec, plotSize) {}
  component.first().patchLetsPlot().saveAs(filename).viewInBrowser()
}