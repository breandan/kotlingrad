package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.numerical.DoublePrecision
import edu.umontreal.kotlingrad.utils.step
import org.knowm.xchart.*
import java.awt.Color

@Suppress("NonAsciiCharacters", "LocalVariableName", "RemoveRedundantBackticks")
fun main() {
  with(DoublePrecision) {
    val x = Var("x")
    val y = sin(sin(sin(x))) / x + sin(x) * x + cos(x) + x
    val `dy∕dx` = d(y) / d(x)
    val `d²y∕dx²` = d(`dy∕dx`) / d(x)
    val `d³y∕dx³` = d(`d²y∕dx²`) / d(x)
    val `d⁴y∕dx⁴` = d(`d³y∕dx³`) / d(x)
    val `d⁵y∕dx⁵` = d(`d⁴y∕dx⁴`) / d(x)

    println("""y=$y
               dy/dx=$`dy∕dx`
               d²y/dx²=$`d²y∕dx²`
               d³y/dx³=$`d³y∕dx³`
               d⁴y/dx⁴=$`d⁴y∕dx⁴`""".trimIndent())

    val xs = (-9.0..9.0 step 0.0087).toList().toDoubleArray()
    val ys = arrayOf(xs.map { y(it) }.toDoubleArray(),
      xs.map { `dy∕dx`(it) }.toDoubleArray(),
      xs.map { `d²y∕dx²`(it) }.toDoubleArray(),
      xs.map { `d³y∕dx³`(it) }.toDoubleArray(),
      xs.map { `d⁴y∕dx⁴`(it) }.toDoubleArray(),
      xs.map { `d⁵y∕dx⁵`(it) }.toDoubleArray())

    val labels = arrayOf("y", "dy/dx", "d²y/x²", "d³y/dx³", "d⁴y/dx⁴", "d⁵y/dx⁵")
    val chart = QuickChart.getChart("Derivatives of y=$y", "x", "y", labels, xs, ys)

    chart.styler.chartBackgroundColor = Color.WHITE
    SwingWrapper(chart).displayChart()
    BitmapEncoder.saveBitmapWithDPI(chart, "src/main/resources/plot.png", BitmapEncoder.BitmapFormat.PNG, 300)
  }
}