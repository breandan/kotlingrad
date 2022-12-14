package ai.hypergraph.kotlingrad.samples

import ai.hypergraph.kaliningraph.visualization.show

import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.base.values.Colors
import jetbrains.datalore.plot.PlotSvgExport
import org.jetbrains.letsPlot.geom.*
import org.jetbrains.letsPlot.*
import org.jetbrains.letsPlot.intern.toSpec
import org.jetbrains.letsPlot.label.ggtitle
import kotlin.math.*

fun main() {
  val range = -5.0..5.0
  val xs = (range step 0.0087).toList()

  val data = mapOf<String, Any>(
    "x" to xs,
    "y" to xs.map { sin(it) },
    "z" to xs.map { cos(it) },
    "t" to xs.map { cos(it + 1) }
  )

  data.plot2D("y = sin(x)", "hello_lets-plot.svg")
}

fun Map<String, Any>.plot2D(
  title: String,
  filename: String = "tempfile_${System.currentTimeMillis()}.svg",
  thickness: Double = 1.0,
  dimensions: DoubleVector = DoubleVector(1000.0, 500.0)
) {
  val xAxis = entries.first().key
  // Create plot specs using Lets-Plot Kotlin API
  val geoms = entries.filter { it.key != xAxis }.zip(Colors.distributeEvenly(entries.size - 1, 1.0))
    .map { geomPath(size = thickness, color = it.second, showLegend = true) { x = xAxis; y = it.first.key } }

  val plot = geoms.fold(ggplot(this)) { acc, it -> acc + it } + ggtitle(title) + theme().legendPositionRight()

  // Create JFXPanel showing the plot.
  val plotSpec = plot.toSpec()

  val component = PlotSvgExport.buildSvgImageFromRawSpecs(plotSpec, dimensions)
  component.saveAs(filename).show()
}