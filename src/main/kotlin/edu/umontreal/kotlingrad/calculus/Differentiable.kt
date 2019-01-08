package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.types.Var

interface Differentiable<X: Field<X>, D> {
  fun diff(ind: Var<X>): D

  fun grad(): Map<Var<X>, D>
}