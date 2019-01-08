package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.calculus.DoubleFunctor
import edu.umontreal.kotlingrad.utils.step
import krangl.dataFrameOf
import kravis.geomLine
import kravis.plot
import java.io.File

@Suppress("NonAsciiCharacters", "LocalVariableName", "RemoveRedundantBackticks")
fun main(args: Array<String>) {
  with(DoubleFunctor) {
    val x = variable("x")

    val y = sin(sin(sin(x))) / x + sin(x) * x + cos(x) + x
    val `dy_dx` = d(y) / d(x)
    val `d²y_dx²` = d(dy_dx) / d(x)
    val `d³y_dx³` = d(`d²y_dx²`) / d(x)
    val `d⁴y_dx⁴` = d(`d³y_dx³`) / d(x)
    val `d⁵y_dx⁵` = d(`d⁴y_dx⁴`) / d(x)

    val xs = -10.0..10.0 step 0.09

    println("""y=$y

dy/dx=$dy_dx

d²y/dx²=$`d²y_dx²`

d³y/dx³=$`d³y_dx³`

d⁴y/dx⁴=$`d⁴y_dx⁴`""")

    val ys = (xs.map { listOf(it, y(it), "y") }
        + xs.map { listOf(it, dy_dx(it), "dy/dx") }
        + xs.map { listOf(it, `d²y_dx²`(it), "d²y/x²") }
        + xs.map { listOf(it, `d³y_dx³`(it), "d³y/dx³") }
        + xs.map { listOf(it, `d⁴y_dx⁴`(it), "d⁴y/dx⁴") }
        + xs.map { listOf(it, `d⁵y_dx⁵`(it), "d⁵y/dx⁵") }
        ).flatten()

    dataFrameOf("x", "y", "Function")(ys)
        .plot(x = "x", y = "y", color = "Function")
        .geomLine(size = 1.0)
        .title("Derivatives of y=$y")
        .save(File("src/main/resources/plot.png"))
  }
}