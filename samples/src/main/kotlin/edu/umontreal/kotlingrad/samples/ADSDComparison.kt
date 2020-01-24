 package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.D
import edu.umontreal.kotlingrad.experimental.cos
import edu.umontreal.kotlingrad.experimental.grad
import edu.umontreal.kotlingrad.experimental.sin
import edu.umontreal.kotlingrad.experimental.BigDecimalPrecision
import edu.umontreal.kotlingrad.experimental.DoublePrecision
import edu.umontreal.kotlingrad.utils.step
import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.plot.MonolithicAwt
import jetbrains.letsPlot.geom.geom_path
import jetbrains.letsPlot.ggplot
import jetbrains.letsPlot.ggtitle
import jetbrains.letsPlot.intern.toSpec
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.log10

 @Suppress("NonAsciiCharacters", "LocalVariableName", "RemoveRedundantBackticks")
fun main() {
  val xs = (-1000.0..1000.0 step 7E-1).toList()
//  val xs = (-1.0..1.0 step 0.0037).toList().toDoubleArray()

  // Arbitrary precision (defaults to 30 significant figures)
  val bdvals = with(BigDecimalPrecision) {
    val y = sin(x * cos(x * sin(x * cos(x))))
    val `dy∕dx` = d(y) / d(x)

    xs.map { BigDecimal(it) }.run {
      val f = map { y(x to it).asDouble() }
      val df = map { `dy∕dx`(x to it).asDouble() }
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
    val y = sin(x * cos(x * sin(x * cos(x))))
    val `dy∕dx` = d(y) / d(x)

//    println("""
//      y=$y
//      dy/dx=$`dy∕dx`
//      """.trimIndent())

    xs.run { arrayOf(map { y(x to it).asDouble() }, map { `dy∕dx`(x to it).asDouble() }) }.map { it.toDoubleArray() }.toTypedArray()
  }

  // Numerical differentiation using centered differences
  val fdvals = with(DoublePrecision) {
    val y = sin(x * cos(x * sin(x * cos(x))))
    val h = 7E-13
    val `dy∕dx` = (y(x to x + h) - y(x to x - h)) / (2.0 * h)

    xs.run { arrayOf(map { y(x to it).asDouble() }, map { `dy∕dx`(x to it).asDouble() }) }.map { it.toDoubleArray() }.toTypedArray()
  }

  val t = { i: Double, d: Double -> log10(abs(i - d)).let { if (it < -20) -20.0 else it } }

  val errors = listOf(
//    sdvals[1].zip(bdvals[1], t), // Appears indistinguishable on lets-plot
    advals[1].zip(bdvals[1], t),
    advals[1].zip(sdvals[1], t),
    fdvals[1].zip(bdvals[1], t)
  // Filter out values which hit the floor of numerical precision (effectively zero)
  ).map { it.run { var last = -14.0; map { d -> if (d <= -15.0) last else { last = d; d } } } }

//  println("SD average error: ${errors[0].average()}")
//  println("AD average error: ${errors[1].average()}")
//  println("AD/SD average delta: ${errors[2].average()}")

  for (i in xs.indices) println(xs[i].toString() + ",\t" + errors[0][i] + ",\t" + errors[1][i] + ",\t" + errors[2][i])

  val title = "f(x) = sin(sin(sin(x)))) / x + sin(x) * x + cos(x) + x"
  val labels = arrayOf("Δ(SD, IP), Δ(AD, IP)", "Δ(AD, SD)", "Δ(FD, IP)")
  val data = (labels.zip(errors) + ("x" to xs)).toMap()
  val colors = listOf("dark_green", "gray", "black", "red", "orange", "dark_blue")
  val geoms = labels.zip(colors).map { geom_path(color = it.second) { x = "x"; y = it.first } }
  val plot = geoms.foldRight(ggplot(data)) { it, acc -> acc + it } + ggtitle(title)

  val plotSpec = plot.toSpec()
  val plotSize = DoubleVector(1000.0, 500.0)

  val result = MonolithicAwt.buildSvgImagesFromRawSpecs(plotSpec, plotSize) {}.first()
  val resultWithAttributes = result.lines().first().replace(">",
    " xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\">") +
    "\n" + result.lines().drop(1).joinToString("\n")
  resultWithAttributes.saveAs("comparison.svg").viewInBrowser()
}