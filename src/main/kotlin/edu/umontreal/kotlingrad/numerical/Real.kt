package edu.umontreal.kotlingrad.numerical

import edu.umontreal.kotlingrad.algebra.Field

interface Real<X: Real<X, Y>, Y: Number>: Field<X> {
  val value: Y
}