package edu.umontreal.kotlingrad.math.algebra

interface RealPrototype<X: Real<X>>: FieldPrototype<X> {
  fun cos(x: X): X

  fun sin(x: X): X

  fun tan(x: X): X

  fun exp(x: X): X

  fun log(x: X): X

  fun pow(x: X, y: X): X

  fun sqrt(x: X): X

  fun square(x: X): X
}