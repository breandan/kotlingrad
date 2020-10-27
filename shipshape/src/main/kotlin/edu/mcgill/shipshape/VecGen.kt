package edu.mcgill.shipshape

fun genVec() =
  (1 until maxDim).fold("") { r, i ->
    "$r\nfun <X: RealNumber<X, Y>, Y: Number> RealNumber<X, Y>.Vec(${
      (0 until i).joinToString(", ") { "y$it: Y" }
    }) = VConst<X, D$i>(${
      (0 until i).joinToString(
        ", "
      ) { "wrap(y$it)" }
    })"
  }