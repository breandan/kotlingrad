package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.numerical.BigDecimalPrecision
import edu.umontreal.kotlingrad.numerical.DoublePrecision
import edu.umontreal.kotlingrad.utils.step
import org.knowm.xchart.*
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.log10

@Suppress("NonAsciiCharacters", "LocalVariableName", "RemoveRedundantBackticks")
fun main() {
  val xs = (-1000.0..1000.0 step 7E-1).toList().toDoubleArray()
//  val xs = (-1.0..1.0 step 0.0037).toList().toDoubleArray()
  val bdvals = with(BigDecimalPrecision) {
    val x = Var("x")
    val y = sin(x* cos(x* sin(x * cos(x))))
    val `dy∕dx` = d(y) / d(x)

    xs.map { BigDecimal(it) }.run {
      val f = map { y(it).toDouble() }
      val df = map { `dy∕dx`(it).toDouble() }
      arrayOf(f, df)
    }.map { it.toDoubleArray() }.toTypedArray()
  }

  val advals = xs.run {
    fun x1(d: Double = 0.0, x: D = D(d)): D = grad { sin(x* cos(x* sin(x * cos(x)))) }
    fun d1(d: Double = 0.0, x: D = D(d)): D {
      grad { sin(x* cos(x* sin(x * cos(x)))) }
      return x
    }

    arrayOf(map { x1(it).x }, map { d1(it).d }).map { it.toDoubleArray() }.toTypedArray()
  }

  val sdvals = with(DoublePrecision) {
    val x = Var("x")
    val y = sin(x* cos(x* sin(x * cos(x))))
    val `dy∕dx` = d(y) / d(x)

    println("""
      y=$y
      dy/dx=$`dy∕dx`
      """.trimIndent())

    xs.run { arrayOf(map { y(it) }, map { `dy∕dx`(it) }) }.map { it.toDoubleArray() }.toTypedArray()
  }

  val t = { i: Double, d: Double -> log10(abs(i - d)).let { if (it < -20) -20.0 else it } }

  val errors = arrayOf(
    sdvals[1].zip(bdvals[1], t),
    advals[1].zip(bdvals[1], t),
    advals[1].zip(sdvals[1], t)
  ).map { it.run { var last = -14.0; map {d -> if(d <= -15.0) last else {last = d; d}}.toDoubleArray() } }.toTypedArray()

  println("SD average error: ${errors[0].average()}")
  println("AD average error: ${errors[1].average()}")
  println("AD/SD average delta: ${errors[2].average()}")

  for (i in 0..(xs.size - 1))
    println(xs[i].toString() + "," + errors[0][i] + "," + errors[1][i] + "," + errors[2][i])

  val eqn = "sin(sin(sin(x)))) / x + sin(x) * x + cos(x) + x"
  val labels = arrayOf("Δ(SD, IP)", "Δ(AD, IP)", "Δ(AD, SD)")
  val chart = QuickChart.getChart("Log errors between AD and SD on the function f(x) = $eqn", "x", "log(Δ)", labels, xs, errors)

  chart.styler.chartBackgroundColor = Color.WHITE
  SwingWrapper(chart).displayChart()
  BitmapEncoder.saveBitmapWithDPI(chart, "src/main/resources/comparison.png", BitmapEncoder.BitmapFormat.PNG, 300)
}