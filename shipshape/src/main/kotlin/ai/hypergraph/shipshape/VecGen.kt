package ai.hypergraph.shipshape

fun genVec() =
  genVecOfPrimitives() +
    genVecOfSConst() +
    genVecOfSFun()

private fun genVecOfSFun() =
  (1..maxDim).joinToString("\n", "\n", "\n") { dim ->
    "fun <X: SFun<X>> Vec(${
      (1..dim).joinToString { "y$it: SFun<X>" }
    }) = Vec<X, D$dim>(arrayListOf(${
      (1..dim).joinToString { "y$it" }
    }))"
  }

private fun genVecOfSConst() =
  (1..maxDim).joinToString("\n", "\n", "\n") { dim ->
    "fun <X: SFun<X>> Vec(${
      (1..dim).joinToString { "y$it: SConst<X>" }
    }) = VConst<X, D$dim>(${
      (1..dim).joinToString { "y$it" }
    })"
  }

private fun genVecOfPrimitives() =
  (1..maxDim).joinToString("\n", "\n", "\n") { dim ->
    "fun <X: RealNumber<X, Y>, Y: Number> RealNumber<X, Y>.Vec(${
      (1..dim).joinToString { "y$it: Y" }
    }) = VConst<X, D$dim>(${
      (1..dim).joinToString { "wrap(y$it)" }
    })"
  }