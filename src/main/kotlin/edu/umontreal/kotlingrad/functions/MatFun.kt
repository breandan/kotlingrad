package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.algebra.Ring
import edu.umontreal.kotlingrad.calculus.Differentiable

interface MatFun<X: Field<X>>: Ring<MatFun<X>>, Differentiable<X>