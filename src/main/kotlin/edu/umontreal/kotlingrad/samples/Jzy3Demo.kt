package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.numerical.DoublePrecision
import org.jzy3d.analysis.AbstractAnalysis
import org.jzy3d.analysis.AnalysisLauncher
import org.jzy3d.chart.factories.AWTChartComponentFactory
import org.jzy3d.colors.Color
import org.jzy3d.colors.ColorMapper
import org.jzy3d.colors.colormaps.ColorMapRainbow
import org.jzy3d.maths.Range
import org.jzy3d.plot3d.builder.Builder
import org.jzy3d.plot3d.builder.Mapper
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid
import org.jzy3d.plot3d.rendering.canvas.Quality.Advanced

class SurfaceDemo: AbstractAnalysis() {
  override fun init() {
    // Define a function to plot
    val mapper = object: Mapper() {
      override fun f(xc: Double, yc: Double) =
        with(DoublePrecision) {
          val x = variable()
          val y = variable()

          val f = sin(10 * (pow(x, 2) + pow(y, 2))) / 10
          val z = d(f)/d(x)
          val m = d(z)/d(y)

          m(xc, yc)
        }
    }

    // Define range and precision for the function to plot
    val range = Range(-1f, 1f)
    val steps = 500

    // Create the object to represent the function over the given range.
    val surface = Builder.buildOrthonormal(OrthonormalGrid(range, steps, range, steps), mapper)
    with(surface) {
      colorMapper = ColorMapper(ColorMapRainbow(),
        bounds.zmin.toDouble(),
        bounds.zmax.toDouble(),
        Color(1f, 1f, 1f, .5f))

      faceDisplayed = true
      wireframeDisplayed = false
    }

    // Create a chart
    chart = AWTChartComponentFactory.chart(Advanced, getCanvasType())
    chart.scene.graph.add(surface)
  }
}

fun main(args: Array<String>) = AnalysisLauncher.open(SurfaceDemo())