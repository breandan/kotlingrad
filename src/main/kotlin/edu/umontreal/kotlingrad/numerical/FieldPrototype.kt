package edu.umontreal.kotlingrad.numerical

import edu.umontreal.kotlingrad.algebra.Field

interface FieldPrototype<X: Field<X>> {
  val zero: X
  val one: X
  val e: X

  fun cos(x: X): X

  fun sin(x: X): X

  fun tan(x: X): X

  fun exp(x: X): X

  fun log(x: X): X

  fun pow(x: X, y: X): X

  fun sqrt(x: X): X
}