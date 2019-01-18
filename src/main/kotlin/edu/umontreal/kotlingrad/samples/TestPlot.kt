package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.numerical.DoublePrecision
import edu.umontreal.kotlingrad.utils.step
import krangl.dataFrameOf
import kravis.geomLine
import kravis.plot
import java.io.File

@Suppress("NonAsciiCharacters", "LocalVariableName", "RemoveRedundantBackticks")
fun main(args: Array<String>) {
  with(DoublePrecision) {
    val x = variable("x")

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

               d⁴y/dx⁴=$`d⁴y∕dx⁴`"""
        .trimIndent())

    val xs = -10.0..10.0 step 0.09
    val ys = (xs.map { listOf(it, y(it), "y") }
            + xs.map { listOf(it, `dy∕dx`(it), "dy/dx") }
            + xs.map { listOf(it, `d²y∕dx²`(it), "d²y/x²") }
            + xs.map { listOf(it, `d³y∕dx³`(it), "d³y/dx³") }
            + xs.map { listOf(it, `d⁴y∕dx⁴`(it), "d⁴y/dx⁴") }
            + xs.map { listOf(it, `d⁵y∕dx⁵`(it), "d⁵y/dx⁵") }).flatten()

    // TODO: Migrate from Kravis to http://www.jzy3d.org
    dataFrameOf("x", "y", "Function")(ys)
      .plot(x = "x", y = "y", color = "Function")
      .geomLine(size = 1.0)
      .title("Derivatives of y=$y")
      .save(File("src/main/resources/plot.png"))
  }
}