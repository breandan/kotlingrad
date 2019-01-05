package edu.umontreal.kotlingrad.math.calculus

import edu.umontreal.kotlingrad.math.algebra.Field
import edu.umontreal.kotlingrad.math.types.Var

interface Differentiable<X: Field<X>, D> {
  fun diff(ind: Var<X>): D

  fun grad(): Map<Var<X>, D>
}