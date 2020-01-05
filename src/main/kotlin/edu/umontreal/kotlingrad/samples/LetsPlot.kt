package edu.umontreal.kotlingrad.samples

import javafx.application.Platform
import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.plot.MonolithicAwt
import jetbrains.datalore.plot.builder.presentation.Style
import jetbrains.datalore.vis.demoUtils.jfx.SceneMapperJfxPanel
import jetbrains.datalore.vis.svg.SvgSvgElement
import jetbrains.letsPlot.geom.geom_path
import jetbrains.letsPlot.ggplot
import jetbrains.letsPlot.ggtitle
import jetbrains.letsPlot.intern.toSpec
import java.awt.Dimension
import javax.swing.*

// Setup
val COMPONENT_FACTORY_JFX = { svg: SvgSvgElement -> SceneMapperJfxPanel(svg, listOf(Style.JFX_PLOT_STYLESHEET)) }

val EXECUTOR_JFX = { r: () -> Unit ->
  if (Platform.isFxApplicationThread()) r() else Platform.runLater(r)
}

fun main() =
  SwingUtilities.invokeLater {
    val data = mapOf<String, Any>(
      "x" to List(10) { it },
      "y" to List(10) { it * it }
    )

    // Create plot specs using Lets-Plot Kotlin API
    val geom = geom_path(alpha = 1.0, size = 2.0) { x = "x"; y= "y" }
    val plot = ggplot(data) + geom + ggtitle("y = x^2")

    // Create JFXPanel showing the plot.
    val plotSpec = plot.toSpec()
    val plotSize = DoubleVector(1000.0, 500.0)

    val component = MonolithicAwt.buildPlotFromRawSpecs(plotSpec, plotSize, COMPONENT_FACTORY_JFX, EXECUTOR_JFX) {}

    // Show plot in Swing frame.
    JFrame("Minimal Function").apply {
      contentPane.add(component)
      defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
      size = Dimension(plotSize.x.toInt() + 100, plotSize.y.toInt() + 100)
      isVisible = true
    }
  }