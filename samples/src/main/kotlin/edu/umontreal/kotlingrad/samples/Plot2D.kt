@file:Suppress("NonAsciiCharacters", "LocalVariableName")

package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.DReal
import edu.umontreal.kotlingrad.experimental.DoublePrecision
import edu.umontreal.kotlingrad.experimental.SFun
import edu.umontreal.kotlingrad.utils.step
import org.knowm.xchart.*
import org.knowm.xchart.VectorGraphicsEncoder.VectorGraphicsFormat.SVG
import java.awt.Color
import java.io.File

fun main() {
  with(DoublePrecision) {
    val y0 = exp(-x * x / 2)

    val hermite = plot2D(-6.0..6.0, *y0.andDerivatives())

    val y1 = sin(sin(sin(x))) / x + sin(x) * x + cos(x) + x

    val sinusoid = plot2D(-6.0..6.0, *y1.andDerivatives())

    val y2 = sigmoid(x)

    val sigmoid = plot2D(-6.0..6.0, *y2.andDerivatives())

    hermite.saveAs("hermite.svg").viewInBrowser()
    sinusoid.saveAs("plot.svg").viewInBrowser()
    sigmoid.saveAs("sigmoid.svg").viewInBrowser()
  }
}

fun XYChart.saveAs(filename: String) =
  VectorGraphicsEncoder.saveVectorGraphic(this, "$resourcesPath/$filename", SVG)
    .run { File("$resourcesPath/$filename") }

private fun SFun<DReal>.andDerivatives(): Array<SFun<DReal>> {
  with(DoublePrecision) {
    val y = this@andDerivatives
    val `dy∕dx` = d(y) / d(x)
    val `d²y∕dx²` = d(`dy∕dx`) / d(x)
    val `d³y∕dx³` = d(`d²y∕dx²`) / d(x)
    val `d⁴y∕dx⁴` = d(`d³y∕dx³`) / d(x)
    val `d⁵y∕dx⁵` = d(`d⁴y∕dx⁴`) / d(x)

//  println("""y=$y
//               dy/dx=$`dy∕dx`
//               d²y/dx²=$`d²y∕dx²`
//               d³y/dx³=$`d³y∕dx³`
//               d⁴y/dx⁴=$`d⁴y∕dx⁴`""".trimIndent())

    return arrayOf(y, `dy∕dx`, `d²y∕dx²`, `d³y∕dx³`, `d⁴y∕dx⁴`, `d⁵y∕dx⁵`)
  }
}

private fun DoublePrecision.plot2D(range: ClosedFloatingPointRange<Double>,
                                   vararg funs: SFun<DReal>): XYChart {
  val xs = (range step 0.0087).toList().toDoubleArray()
  val ys = funs.map { xs.map { xv -> it(x to xv).asDouble() }.toDoubleArray() }.toTypedArray()

  val labels = arrayOf("y", "dy/dx", "d²y/x²", "d³y/dx³", "d⁴y/dx⁴", "d⁵y/dx⁵")
  return QuickChart.getChart("Derivatives of y=${funs[0]}", "x", "y", labels, xs, ys)
    .apply {
      val transparent = Color(1f, 1f, 1f, .1f)
      styler.chartBackgroundColor = transparent
      styler.plotBackgroundColor = transparent
      styler.legendBackgroundColor = transparent
    }
}