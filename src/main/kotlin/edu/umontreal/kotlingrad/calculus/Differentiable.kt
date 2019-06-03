package edu.umontreal.kotlingrad.calculus

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.Function


interface Differentiable<X: Field<X>> {
  infix fun diff(ind: X): X

  fun grad(): Map<X, X>
}
