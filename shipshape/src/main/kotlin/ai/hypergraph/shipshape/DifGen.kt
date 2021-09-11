package ai.hypergraph.shipshape

import org.intellij.lang.annotations.Language

@Language("kt")
fun genDif() =
  (2..maxDim).joinToString("\n", "\n", "\n") { dim ->
    "fun <X: SFun<X>> SFun<X>.d(${
      (1..dim).joinToString { "v$it: SVar<X>" }
    }): Vec<X, D$dim> = Vec(${
      (1..dim).joinToString { "d(v$it)" }
    })"
  } + """
    fun <X: SFun<X>> SFun<X>.d(vararg vars: SVar<X>): Map<SVar<X>, SFun<X>> =
      vars.associateWith { d(it) }
  """.trimIndent()