package edu.mcgill.shipshape

fun genVec() =
  genVecOfPrimitives() +
    genVecOfSConst() +
    genVecOfSFun()

private fun genVecOfSFun() =
  (1..maxDim).joinToString("\n", "\n", "\n") {
    "fun <X: SFun<X>> Vec(${
      (1..it).joinToString { "y$it: SFun<X>" }
    }) = Vec<X, D$it>(arrayListOf(${
      (1..it).joinToString { "y$it" }
    }))"
  }

private fun genVecOfSConst() =
  (1..maxDim).joinToString("\n", "\n", "\n") {
    "fun <X: SFun<X>> Vec(${
      (1..it).joinToString { "y$it: SConst<X>" }
    }) = VConst<X, D$it>(${
      (1..it).joinToString { "y$it" }
    })"
  }

private fun genVecOfPrimitives() =
  (1..maxDim).joinToString("\n", "\n", "\n") {
    "fun <X: RealNumber<X, Y>, Y: Number> RealNumber<X, Y>.Vec(${
      (1..it).joinToString { "y$it: Y" }
    }) = VConst<X, D$it>(${
      (1..it).joinToString { "wrap(y$it)" }
    })"
  }