package edu.mcgill.shipshape

fun genMat(): String {
  var s = ""

  for (i in 1 until maxDim)
    for (j in 1 until maxDim)
      s += "\nfun <X: RealNumber<X, Y>, Y: Number> RealNumber<X, Y>.Mat${i}x${j}(${
        (0 until (i * j)).joinToString(", ") { "y$it: Y" }
      }): MConst<X, D$i, D$j> = MConst(Vec(${
        (0 until (i * j)).joinToString("") {
          "y$it${
            if ((it + 1) == (i * j)) ")" else if ((it + 1) % j == 0) "), Vec(" else ", "
          }"
        }
      })"

  return s
}