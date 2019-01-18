package edu.umontreal.kotlingrad.numerical

import edu.umontreal.kotlingrad.algebra.Field

abstract class Real<X: Real<X, Y>, Y>: Number(), Field<X>, Comparable<Y> where Y: Number, Y: Comparable<Y> {
  abstract val value: Y

  override fun toByte() = value.toByte()

  override fun toChar() = value.toChar()

  override fun toDouble() = value.toDouble()

  override fun toFloat() = value.toFloat()

  override fun toInt() = value.toInt()

  override fun toLong() = value.toLong()

  override fun toShort() = value.toShort()

  override fun compareTo(other: Y) = value.compareTo(value)
}