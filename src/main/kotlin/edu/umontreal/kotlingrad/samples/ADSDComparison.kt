package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.numerical.BigDecimalPrecision
import edu.umontreal.kotlingrad.numerical.DoublePrecision
import edu.umontreal.kotlingrad.utils.step
import org.knowm.xchart.*
import org.knowm.xchart.VectorGraphicsEncoder.VectorGraphicsFormat.SVG
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.log10

@Suppress("NonAsciiCharacters", "LocalVariableName", "RemoveRedundantBackticks")
fun main() {
  val xs = (-1000.0..1000.0 step 7E-1).toList().toDoubleArray()
//  val xs = (-1.0..1.0 step 0.0037).toList().toDoubleArray()

  // Arbitrary precision (defaults to 30 significant figures)
  val bdvals = with(BigDecimalPrecision) {
    val x = Var("x")
    val y = sin(x * cos(x * sin(x * cos(x))))
    val `dy∕dx` = d(y) / d(x)

    xs.map { BigDecimal(it) }.run {
      val f = map { y(it).toDouble() }
      val df = map { `dy∕dx`(it).toDouble() }
      arrayOf(f, df)
    }.map { it.toDoubleArray() }.toTypedArray()
  }

  // Automatic differentiation
  val advals = xs.run {
    fun x1(d: Double = 0.0, x: D = D(d)): D = grad { sin(x * cos(x * sin(x * cos(x)))) }
    fun d1(d: Double = 0.0, x: D = D(d)): D {
      grad { sin(x * cos(x * sin(x * cos(x)))) }
      return x
    }

    arrayOf(map { x1(it).x }, map { d1(it).d }).map { it.toDoubleArray() }.toTypedArray()
  }

  // Symbolic differentiation
  val sdvals = with(DoublePrecision) {
    val x = Var("x")
    val y = sin(x * cos(x * sin(x * cos(x))))
    val `dy∕dx` = d(y) / d(x)

//    println("""
//      y=$y
//      dy/dx=$`dy∕dx`
//      """.trimIndent())

    xs.run { arrayOf(map { y.invoke(it) }, map { `dy∕dx`(it) }) }.map { it.toDoubleArray() }.toTypedArray()
  }

  // Numerical differentiation using centered differences
  val fdvals = with(DoublePrecision) {
    val x = Var("x")
    val y = sin(x * cos(x * sin(x * cos(x))))
    val h = 7E-13
    val `dy∕dx` = (y(x + h) - y(x - h)) / (2.0 * h)

    xs.run { arrayOf(map { y.invoke(it) }, map { `dy∕dx`(it) }) }.map { it.toDoubleArray() }.toTypedArray()
  }

  val t = { i: Double, d: Double -> log10(abs(i - d)).let { if (it < -20) -20.0 else it } }

  val errors = arrayOf(
//    sdvals[1].zip(bdvals[1], t), // Appears indistinguishable on Xchart
    advals[1].zip(bdvals[1], t),
    advals[1].zip(sdvals[1], t),
    fdvals[1].zip(bdvals[1], t)
  // Filter out values which hit the floor of numerical precision (effectively zero)
  ).map { it.run { var last = -14.0; map { d -> if (d <= -15.0) last else { last = d; d } }.toDoubleArray() } }.toTypedArray()

//  println("SD average error: ${errors[0].average()}")
//  println("AD average error: ${errors[1].average()}")
//  println("AD/SD average delta: ${errors[2].average()}")

  for (i in xs.indices) println(xs[i].toString() + ",\t" + errors[0][i] + ",\t" + errors[1][i] + ",\t" + errors[2][i])

  val eqn = "sin(sin(sin(x)))) / x + sin(x) * x + cos(x) + x"
  val labels = arrayOf("Δ(SD, IP), Δ(AD, IP)", "Δ(AD, SD)", "Δ(FD, IP)")
  val chart = QuickChart.getChart("f(x) = $eqn", "x", "log₁₀(Δ)", labels, xs, errors)

  chart.styler.apply {
    val transparent = Color(1f, 1f, 1f, .0f)
    chartBackgroundColor = transparent
    plotBackgroundColor = transparent
    legendBackgroundColor = transparent
  }

//  SwingWrapper(chart).displayChart()
  VectorGraphicsEncoder.saveVectorGraphic(chart, "src/main/resources/comparison.svg", SVG)
}