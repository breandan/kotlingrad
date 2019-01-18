package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.Function


interface Differentiable<X: Field<X>, D> {
  fun diff(ind: Function.Var<X>): D

  fun grad(): Map<Function.Var<X>, D>
}
