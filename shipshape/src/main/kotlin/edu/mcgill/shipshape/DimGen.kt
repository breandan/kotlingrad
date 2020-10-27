package edu.mcgill.shipshape

// TODO: make this a plugin-configurable setting
const val maxDim = 10
const val maxNat = 101

fun genDim(): String {
  var s = "interface Nat<T: D0> { val i: Int }\n"
  s += "sealed class INat<T: D0>(open val i: Int): SConst<INat<T>>(i) { override fun toString() = \"\$i\" }\n"
  s += "sealed class D0(override val i: Int = 0): INat<D0>(i) { companion object: D0(), Nat<D0> }\n\n"
  for(i in 1 until maxNat)
    s += "sealed class D$i(override val i: Int = $i): D${i - 1}(i) { companion object: D$i(), Nat<D$i> }\n"

  return s
}