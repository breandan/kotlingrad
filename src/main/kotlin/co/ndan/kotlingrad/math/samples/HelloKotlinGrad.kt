package co.ndan.kotlingrad.math.samples

import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import co.ndan.kotlingrad.math.calculus.DoubleFunctor
import co.ndan.kotlingrad.math.functions.Function
import co.ndan.kotlingrad.math.numerical.Double

@Suppress("NonAsciiCharacters", "LocalVariableName")
fun main(args: Array<String>) {
  with(DoubleFunctor) {
    val x = variable("x", 0)
    val y = variable("y", 1)

    val d: Function<Double> = x + y
    val z = x * (-sin(x * y) + y)      // Operator overloads
    val `∂z_∂x` = d(z) / d(x)          // Leibniz notation
    val `∂z_∂y` = d(z) / d(y)          // Multiple variables
    val `∂²z_∂x²` = d(`∂z_∂x`) / d(x)  // Higher order and
    val `∂²z_∂x∂y` = d(`∂z_∂x`) / d(y) // partial derivatives

    val p = "${x()}, ${y()}"
    print("z(x, y) \t\t\t= $z\n" +
        "∂z($p)/∂x \t= $`∂z_∂x` \n\t\t\t\t\t= " + `∂z_∂x`() + "\n" +
        "∂z($p)/∂y \t= $`∂z_∂y` \n\t\t\t\t\t= " + `∂z_∂y`() + "\n" +
        "∂²z($p)/∂x² \t= $`∂z_∂y` \n\t\t\t\t\t= " + `∂²z_∂x²`() + "\n" +
        "∂²z($p)/∂x∂y \t= $`∂²z_∂x∂y` \n\t\t\t\t\t= " + `∂²z_∂x∂y`())
  }
}