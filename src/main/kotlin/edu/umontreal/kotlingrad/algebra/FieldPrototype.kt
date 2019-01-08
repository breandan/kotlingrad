package edu.umontreal.kotlingrad.algebra

interface FieldPrototype<X: Field<X>> {
  val zero: X
  val one: X
}