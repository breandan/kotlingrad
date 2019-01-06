package edu.umontreal.kotlingrad.math.functions

import edu.umontreal.kotlingrad.math.algebra.Field
import edu.umontreal.kotlingrad.math.algebra.Ring
import edu.umontreal.kotlingrad.math.calculus.Differentiable

interface MatrixFunction<X: Field<X>>: Ring<MatrixFunction<X>>, Differentiable<X, MatrixFunction<X>> {

}