package edu.umontreal.kotlingrad.math.samples

import edu.umontreal.kotlingrad.math.calculus.DoubleFunctor
import edu.umontreal.kotlingrad.utils.step
import krangl.dataFrameOf
import kravis.geomLine
import kravis.plot
import java.io.File

@Suppress("NonAsciiCharacters", "LocalVariableName", "RemoveRedundantBackticks")
fun main(args: Array<String>) {
  with(DoubleFunctor) {
    val x = variable()

    val y = sin(sin(x)) / x
    val `dy_dx` = d(y) / d(x)
    val `d²y_dx²` = d(dy_dx) / d(x)
    val `d³y_dx³` = d(`d²y_dx²`) / d(x)
    val `d⁴y_dx⁴` = d(`d³y_dx³`) / d(x)

    val xs = -10.0..10.0 step 0.1

    val ys = (xs.map { listOf(it, y(x to it), "y=sin(sin(x))/x") } +
        xs.map { listOf(it, dy_dx(x to it), "dy/dx") } +
        xs.map { listOf(it, `d²y_dx²`(x to it), "d²y/x²") } +
        xs.map { listOf(it, `d³y_dx³`(x to it), "d³y/dx³") } +
        xs.map { listOf(it, `d⁴y_dx⁴`(x to it), "d⁴y/dx⁴") }).flatten()

    dataFrameOf("x", "y", "Function")(ys)
        .plot(x = "x", y = "y", color = "Function")
        .geomLine(size = 1.2)
        .title("Derivatives of y=sin(sin(x))/x")
        .save(File("src/main/resources/plot.png"))
  }
}