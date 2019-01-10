package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.calculus.DoubleFunctor

@Suppress("NonAsciiCharacters", "LocalVariableName")
fun main(args: Array<String>) {
  with(DoubleFunctor) {
    val x = variable("x")
    val y = variable("y")

    val z = x * (-sin(x * y) + y) * 4  // Infix notation
    val `∂z_∂x` = d(z) / d(x)          // Leibniz notation
    val `∂z_∂y` = d(z) / d(y)          // Partial derivatives
    val `∂²z_∂x²` = d(`∂z_∂x`) / d(x)  // Higher order derivatives
    val `∂²z_∂x∂y` = d(`∂z_∂x`) / d(y) // Higher order partials
    val `∇z` = z.grad()                // Gradient operator

    val values = mapOf(x to 0, y to 1)
    val indVar = z.variables.joinToString(", ")

    val p = "$values"
    print("z($indVar) \t\t\t= $z\n" +
        "z($p) \t\t\t= ${z(values)}\n" +
        "∂z($p)/∂x \t\t= $`∂z_∂x` \n\t\t\t\t= " + `∂z_∂x`(values) + "\n" +
        "∂z($p)/∂y \t\t= $`∂z_∂y` \n\t\t\t\t= " + `∂z_∂y`(values) + "\n" +
        "∂²z($p)/∂x² \t\t= $`∂z_∂y` \n\t\t\t\t= " + `∂²z_∂x²`(values) + "\n" +
        "∂²z($p)/∂x∂y \t\t= $`∂²z_∂x∂y` \n\t\t\t\t= " + `∂²z_∂x∂y`(values) + "\n" +
        "∇z($p) \t\t\t= $`∇z` \n\t\t\t\t= [${`∇z`[x]!!(values)}, ${`∇z`[y]!!(values)}]ᵀ")
  }
}