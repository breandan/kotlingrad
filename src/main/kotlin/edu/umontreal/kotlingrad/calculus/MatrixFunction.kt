package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.algebra.Ring
import edu.umontreal.kotlingrad.functions.Differentiable

interface MatrixFunction<X: Field<X>>: Ring<MatrixFunction<X>>, Differentiable<X, MatrixFunction<X>>