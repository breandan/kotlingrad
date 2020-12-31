package edu.mcgill.shipshape

// TODO: make this a plugin-configurable setting
const val maxDim = 10
const val maxNat = 100

fun genDim() = """
  interface Nat<T: D0> { val i: Int }
  sealed class INat<T: D0>(open val i: Int): SConst<INat<T>>(i) { override fun toString() = "${"\$"}i" }
  
  sealed class D0(override val i: Int = 0): INat<D0>(i) { companion object: D0(), Nat<D0> }
  sealed class DN(): D1() { companion object: DN(), Nat<DN> }
""".trimIndent() + (1..maxNat).joinToString("\n", "\n", "\n") {
  "sealed class D$it(override val i: Int = $it): D${it - 1}(i) { companion object: D$it(), Nat<D$it> }"
}