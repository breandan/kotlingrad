package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.utils.step
import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.base.values.Colors
import jetbrains.datalore.plot.MonolithicAwt
import jetbrains.letsPlot.geom.geom_path
import jetbrains.letsPlot.ggplot
import jetbrains.letsPlot.ggtitle
import jetbrains.letsPlot.intern.toSpec
import jetbrains.letsPlot.theme
import kotlin.math.cos
import kotlin.math.sin

fun main() {
  val range = -5.0..5.0
  val xs = (range step 0.0087).toList()

  val data = mapOf<String, Any>(
    "x" to xs,
    "y" to xs.map { sin(it) },
    "z" to xs.map { cos(it) },
    "t" to xs.map { cos(it + 1) }
  )

  data.plot2D("y = sin(x)", "x", "hello_lets-plot.svg")
}

fun Map<String, Any>.plot2D(title: String,
                            xAxis: String,
                            filename: String,
                            thickness: Double = 1.0,
                            dimensions: DoubleVector = DoubleVector(1000.0, 500.0)) {
  // Create plot specs using Lets-Plot Kotlin API
  val geoms = entries.filter { it.key != xAxis }.zip(Colors.distributeEvenly(entries.size - 1, 1.0))
    .map { geom_path(size = thickness, color = it.second, show_legend = true) { x = xAxis; y = it.first.key } }

  val plot = geoms.fold(ggplot(this)) { acc, it -> acc + it } + ggtitle(title) + theme(legend_position = "right")

  // Create JFXPanel showing the plot.
  val plotSpec = plot.toSpec()

  val component = MonolithicAwt.buildSvgImagesFromRawSpecs(plotSpec, dimensions) {}
  component.first().patchLetsPlot().saveAs(filename).viewInBrowser()
}

fun String.patchLetsPlot() = lines().first().replace(">",
  " xmlns=\"http://www.w3.org/2000/svg\">") +
  "\n" + lines().drop(1).joinToString("\n")