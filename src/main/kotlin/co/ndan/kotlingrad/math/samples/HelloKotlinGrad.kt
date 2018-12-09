package co.ndan.kotlingrad.math.samples

import co.ndan.kotlingrad.math.algebra.DoublePrototype
import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import co.ndan.kotlingrad.math.calculus.DoubleFunctor.sin
import co.ndan.kotlingrad.math.types.Double
import co.ndan.kotlingrad.math.types.Var

@Suppress("NonAsciiCharacters", "LocalVariableName")
fun main(args: Array<String>) {
  val x = Var("x", Double(0), DoublePrototype)
  val y = Var("y", Double(1), DoublePrototype)

  val z = x * (-sin(x * y) + y)      // Operator overloads
  val `∂z_∂x`    = d(   z   ) / d(x) // Leibniz notation
  val `∂z_∂y`    = d(   z   ) / d(y) // Multiple variables
  val `∂²z_∂x²`  = d(`∂z_∂x`) / d(x) // Higher order and
  val `∂²z_∂x∂y` = d(`∂z_∂x`) / d(y) // partial derivatives

  val p = "${x.value}, ${y.value}"
  print("z(x, y) \t\t\t= $z\n" +
    "∂z($p)_∂x \t= " + `∂z_∂x` + "\n\t\t\t\t\t= " + `∂z_∂x`.value + "\n" +
    "∂z($p)_∂y \t= " + `∂z_∂y` + "\n\t\t\t\t\t= " + `∂z_∂y`.value + "\n" +
    "∂²z($p)_∂x² \t= " + `∂z_∂y` + "\n\t\t\t\t\t= " + `∂²z_∂x²`.value + "\n" +
    "∂²z($p)_∂x∂y \t= " + `∂²z_∂x∂y` + "\n\t\t\t\t\t= " + `∂²z_∂x∂y`.value)
}