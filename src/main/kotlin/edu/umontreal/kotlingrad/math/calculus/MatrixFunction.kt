package edu.umontreal.kotlingrad.math.calculus

import edu.umontreal.kotlingrad.math.algebra.Field
import edu.umontreal.kotlingrad.math.algebra.Ring

interface MatrixFunction<X: Field<X>>: Ring<MatrixFunction<X>>, Differentiable<X, MatrixFunction<X>>