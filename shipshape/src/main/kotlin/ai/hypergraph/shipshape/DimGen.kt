package ai.hypergraph.shipshape

import org.intellij.lang.annotations.Language

var maxDim = 10
var maxNat = 100

@Language("kt")
fun genDim() = """
  interface Nat<T: D0> { val i: Int }
  sealed class INat<T: D0>(open val i: Int) { override fun toString() = "${'$'}i" }
  
  sealed class D0(override val i: Int = 0): INat<D0>(i) { companion object: D0(), Nat<D0> }
  sealed class DN: D1() { companion object: DN(), Nat<DN> }
""".trimIndent() + (1..maxNat).joinToString("\n", "\n", "\n") { nat ->
  "sealed class D$nat(override val i: Int = $nat): D${nat - 1}(i) { companion object: D$nat(), Nat<D$nat> }"
}