package edu.umontreal.kotlingrad.samples

import javafx.application.Platform
import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.plot.MonolithicAwt
import jetbrains.datalore.plot.builder.presentation.Style
import jetbrains.datalore.vis.demoUtils.jfx.SceneMapperJfxPanel
import jetbrains.datalore.vis.svg.SvgSvgElement
import jetbrains.letsPlot.geom.geom_histogram
import jetbrains.letsPlot.ggplot
import jetbrains.letsPlot.ggtitle
import jetbrains.letsPlot.intern.toSpec
import java.awt.Dimension
import javax.swing.*

// Setup
val COMPONENT_FACTORY_JFX = { svg: SvgSvgElement -> SceneMapperJfxPanel(svg, listOf(Style.JFX_PLOT_STYLESHEET)) }
val EXECUTOR_JFX = { r: () -> Unit ->
  if (Platform.isFxApplicationThread()) {
    r.invoke()
  } else {
    Platform.runLater(r)
  }
}

fun main() {
  SwingUtilities.invokeLater {

    // Generate random data-points
    val rand = java.util.Random()
    val data = mapOf<String, Any>(
      "x" to List(500) { rand.nextGaussian() } + List(500) { rand.nextGaussian() + 1.0 },
      "c" to List(500) { "A" } + List(500) { "B" }
    )

    // Create plot specs using Lets-Plot Kotlin API
    val geom = geom_histogram(alpha = 0.3, size = 0.0) {
      x = "x"; fill = "c"
    }
    val p = ggplot(data) + geom + ggtitle("The normal distribution")

    // Create JFXPanel showing the plot.
    val plotSpec = p.toSpec()
    val plotSize = DoubleVector(600.0, 300.0)

    val component =
      MonolithicAwt.buildPlotFromRawSpecs(plotSpec, plotSize, COMPONENT_FACTORY_JFX, EXECUTOR_JFX) {
        for (message in it) {
          println("PLOT MESSAGE: $message")
        }
      }

    // Show plot in Swing frame.
    val frame = JFrame("The Minimal")
    frame.contentPane.add(component)
    frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    frame.size = Dimension(plotSize.x.toInt() + 100, plotSize.y.toInt() + 100)
    frame.isVisible = true
  }
}
