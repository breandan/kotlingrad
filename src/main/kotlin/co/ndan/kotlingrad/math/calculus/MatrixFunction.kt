package co.ndan.kotlingrad.math.calculus

import co.ndan.kotlingrad.math.algebra.Field
import co.ndan.kotlingrad.math.algebra.Ring

interface MatrixFunction<X: Field<X>>: Ring<MatrixFunction<X>>, Differentiable<X, MatrixFunction<X>>