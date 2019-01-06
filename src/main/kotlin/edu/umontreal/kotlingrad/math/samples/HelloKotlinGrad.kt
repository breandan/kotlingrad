package edu.umontreal.kotlingrad.math.samples

import edu.umontreal.kotlingrad.math.calculus.DoubleFunctor
import edu.umontreal.kotlingrad.math.numerical.DoubleReal

@Suppress("NonAsciiCharacters", "LocalVariableName")
fun main(args: Array<String>) {
  with(DoubleFunctor) {
    val x = variable("x")
    val y = variable("y")

    val z = x * (-sin(x * y) + y)      // Polish notation
    val `∂z_∂x` = d(z) / d(x)          // Leibniz notation
    val `∂z_∂y` = d(z) / d(y)          // Partial derivatives
    val `∂²z_∂x²` = d(`∂z_∂x`) / d(x)  // Higher order derivatives
    val `∂²z_∂x∂y` = d(`∂z_∂x`) / d(y) // Higher order partials
    val `∇z` = z.grad()                // Gradient operator

    val values = mapOf(x to DoubleReal(0), y to DoubleReal(1))
    val indVar = z.independentVariables().joinToString(", ")
    val p = "${x(x to DoubleReal(0))}, ${y(y to DoubleReal(1))}"
    print("z($indVar) \t\t\t\t= $z\n" +
        "∂z($p)/∂x \t\t= $`∂z_∂x` \n\t\t\t\t\t\t= " + `∂z_∂x`(values) + "\n" +
        "∂z($p)/∂y \t\t= $`∂z_∂y` \n\t\t\t\t\t\t= " + `∂z_∂y`(values) + "\n" +
        "∂²z($p)/∂x² \t\t= $`∂z_∂y` \n\t\t\t\t\t\t= " + `∂²z_∂x²`(values) + "\n" +
        "∂²z($p)/∂x∂y \t\t= $`∂²z_∂x∂y` \n\t\t\t\t\t\t= " + `∂²z_∂x∂y`(values) + "\n" +
        "∇z($p) \t\t\t= $`∇z` \n\t\t\t\t\t\t= [${`∇z`[x]!!(values)}, ${`∇z`[y]!!(values)}]ᵀ")
  }
}