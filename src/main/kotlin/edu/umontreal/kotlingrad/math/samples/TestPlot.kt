package edu.umontreal.kotlingrad.math.samples

import edu.umontreal.kotlingrad.math.calculus.DoubleFunctor
import edu.umontreal.kotlingrad.math.numerical.DoubleReal
import krangl.DataFrame
import krangl.dataFrameOf
import kravis.geomLine
import kravis.plot
import java.io.File

fun main(args: Array<String>) {

  with(DoubleFunctor) {
    val x = variable("x")

    val y = sin(x) / x
    val `dy_dx` = d(y) / d(x)
    val `d²y_dx²` = d(dy_dx) / d(x)
    val `d³y_dx³` = d(`d²y_dx²`) / d(x)
    val `d⁴y_dx⁴` = d(`d³y_dx³`) / d(x)

    val xs = -10.0..10.0 step 0.1

    val len = xs.toList().size

    val ys0 = xs.map { arrayListOf(it, y(x to DoubleReal(it)).dbl, "y=sin(x)/x") }
    val ys1 = xs.map { arrayListOf(it, dy_dx(x to DoubleReal(it)).dbl, "dy/dx") }
    val ys2 = xs.map { arrayListOf(it, `d²y_dx²`(x to DoubleReal(it)).dbl, "d²y/x²") }
    val ys3 = xs.map { arrayListOf(it, `d³y_dx³`(x to DoubleReal(it)).dbl, "d³y/dx³") }
    val ys4 = xs.map { arrayListOf(it, `d⁴y_dx⁴`(x to DoubleReal(it)).dbl, "d⁴y/dx⁴") }


    val df: DataFrame = dataFrameOf("x", "y", "fun")((ys0 + ys1 + ys2 + ys3 + ys4).flatten())

    df.plot(x = "x", y = "y", color = "fun")
        .geomLine(size = 1.3)
        .title("Derivatives of y=sin(x)/x")
        .save(File("src/main/resources/plot.png"))
  }
}

infix fun ClosedRange<Double>.step(step: Double): Iterable<Double> {
  require(start.isFinite())
  require(endInclusive.isFinite())
  require(step > 0.0) { "Step must be positive, was: $step." }
  val sequence = generateSequence(start) { previous ->
    if (previous == Double.POSITIVE_INFINITY) return@generateSequence null
    val next = previous + step
    if (next > endInclusive) null else next
  }
  return sequence.asIterable()
}