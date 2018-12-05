package co.ndan.kotlingrad.math.samples

import co.ndan.kotlingrad.math.algebra.DoublePrototype
import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import co.ndan.kotlingrad.math.calculus.RealFunctor
import co.ndan.kotlingrad.math.types.Double

@Suppress("NonAsciiCharacters", "LocalVariableName")
fun main(args: Array<String>) {
  val reft = RealFunctor(DoublePrototype)
  val x = reft.variable("x", Double(1))
  val y = reft.variable("y", Double(2))

  val z = x * (-reft.sin(x * y) + y) // Operator overloads
  val `∂z_∂x`    = d(   z   ) / d(x) // Leibniz notation
  val `∂z_∂y`    = d(   z   ) / d(y) // Multiple variables
  val `∂²z_∂x²`  = d(`∂z_∂x`) / d(x) // Higher order and
  val `∂²z_∂x∂y` = d(`∂z_∂x`) / d(y) // partial derivatives

  println(`∂z_∂x`.value.dbl)
  println(`∂z_∂y`.value.dbl)
  println(`∂²z_∂x²`.value.dbl)
  println(`∂²z_∂x∂y`.value.dbl)
}