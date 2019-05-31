package edu.umontreal.kotlingrad.numerical

import edu.umontreal.kotlingrad.functions.ScalarConst
import edu.umontreal.kotlingrad.functions.ScalarFun

abstract class RealNumber<X: ScalarFun<X>, Y: Number>(open val value: Y): ScalarConst<X>(), Comparable<Y> {
  override fun equals(other: Any?) =
    if (other is RealNumber<*, *>) value == other.value else super.equals(other)
}