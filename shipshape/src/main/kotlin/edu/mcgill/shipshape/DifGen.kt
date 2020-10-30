package edu.mcgill.shipshape

fun main() {
  println(genDif())
}

fun genDif() =
  (2..maxDim).joinToString("\n", "\n", "\n") {
    "fun <X: SFun<X>> SFun<X>.d(${
      (1..it).joinToString { "v$it: SVar<X>" }
    }): Vec<X, D$it> = Vec(${
      (1..it).joinToString { "d(v$it)" }
    })"
  } + """
    fun <X: SFun<X>> SFun<X>.d(vararg vars: SVar<X>): Map<SVar<X>, SFun<X>> =
      vars.map { it to d(it) }.toMap() 
  """.trimIndent()