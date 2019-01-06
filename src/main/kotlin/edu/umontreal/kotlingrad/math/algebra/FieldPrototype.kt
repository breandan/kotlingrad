package edu.umontreal.kotlingrad.math.algebra

interface FieldPrototype<X: Field<X>> {
  val zero: X
  val one: X
}