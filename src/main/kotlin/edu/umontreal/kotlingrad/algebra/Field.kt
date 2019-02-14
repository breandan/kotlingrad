package edu.umontreal.kotlingrad.algebra

import edu.umontreal.kotlingrad.calculus.Differentiable

interface Field<X: Field<X>>: CommutativeRing<X>, Differentiable<X> {
  val e: X

  fun inverse(): X

  infix operator fun div(divisor: X): X = this * divisor.inverse()

  infix fun pow(exp: X): X

  fun sin(): X

  fun cos(): X

  fun tan(): X

  fun exp(): X

  fun log(): X

  fun sqrt(): X
}