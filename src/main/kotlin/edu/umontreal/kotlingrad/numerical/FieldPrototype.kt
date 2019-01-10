package edu.umontreal.kotlingrad.numerical

import edu.umontreal.kotlingrad.algebra.Field

interface FieldPrototype<X: Field<X>> {
  val zero: X
  val one: X
}