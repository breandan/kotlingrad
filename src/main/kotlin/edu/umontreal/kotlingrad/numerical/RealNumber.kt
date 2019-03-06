package edu.umontreal.kotlingrad.numerical

import edu.umontreal.kotlingrad.algebra.Field

// TODO: Try to make this a subtype of Function.ScalarConst
abstract class RealNumber<X: RealNumber<X, Y>, Y>(val value: Y):
  Field<X>, Comparable<Y> where Y: Number, Y: Comparable<Y> {
  override fun compareTo(other: Y) = value.compareTo(value)

  override fun equals(other: Any?) = if (other is RealNumber<*, *>) value == other.value else super.equals(other)
}