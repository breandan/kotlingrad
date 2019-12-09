package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.numerical.DoublePrecision
import edu.umontreal.kotlingrad.utils.step
import org.knowm.xchart.*
import org.knowm.xchart.VectorGraphicsEncoder.VectorGraphicsFormat.SVG
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
    val ys = xs.run {
      arrayOf(
        map { y(it) },
        map { `dy∕dx`(it) },
        map { `d²y∕dx²`(it) },
        map { `d³y∕dx³`(it) },
        map { `d⁴y∕dx⁴`(it) },
        map { `d⁵y∕dx⁵`(it) }
      )
    }.map { it.toDoubleArray() }.toTypedArray()

    val labels = arrayOf("y", "dy/dx", "d²y/x²", "d³y/dx³", "d⁴y/dx⁴", "d⁵y/dx⁵")
    val chart = QuickChart.getChart("Derivatives of y=$y", "x", "y", labels, xs, ys)

    chart.styler.chartBackgroundColor = Color.WHITE
//    SwingWrapper(chart).displayChart()
    VectorGraphicsEncoder.saveVectorGraphic(chart, "src/main/resources/plot.svg", SVG)
  }
}