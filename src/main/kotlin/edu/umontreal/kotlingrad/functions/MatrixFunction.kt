package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.algebra.Ring
import edu.umontreal.kotlingrad.calculus.Differentiable

interface MatrixFunction<X: Field<X>>: Ring<MatrixFunction<X>>, Differentiable<X, MatrixFunction<X>>