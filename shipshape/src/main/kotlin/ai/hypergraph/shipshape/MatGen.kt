package ai.hypergraph.shipshape

fun genMat() =
  genMatOfPrimitives() +
    genMatOfSFun() +
    genMatOfVec()

private fun genMatOfPrimitives() =
  cartProd(1..maxDim) { i, j ->
    "fun <X: RealNumber<X, Y>, Y: Number> RealNumber<X, Y>.Mat${i}x${j}(${
      (1..(i * j)).joinToString { "y$it: Y" }
    }): MConst<X, D$i, D$j> = MConst(Vec(${
      (1..(i * j)).joinToString("") {
        "y$it${
          when {
            it == (i * j) -> ")"
            it % j == 0 -> "), Vec("
            else -> ", "
          }
        }"
      }
    })"
  }

private fun genMatOfSFun() =
  cartProd(1..maxDim) { i, j ->
    "fun <X: SFun<X>> Mat${i}x${j}(${
      (1..(i * j)).joinToString { "y$it: SFun<X>" }
    }): Mat<X, D$i, D$j> = Mat(Vec(${
      (1..(i * j)).joinToString("") {
        "y$it${
          when {
            it == (i * j) -> ")"
            it % j == 0 -> "), Vec("
            else -> ", "
          }
        }"
      }
    })"
  }

private fun genMatOfVec() =
  cartProd(1..maxDim) { i, j ->
    "fun <X: SFun<X>> Mat${i}x${j}(${
      (1..i).joinToString { "y$it: Vec<X, D$j>" }
    }): Mat<X, D$i, D$j> = Mat(${
      (1..i).joinToString { "y$it" }
    })"
  }

fun <T> cartProd(a: Iterable<T>, b: Iterable<T> = a, map: (T, T) -> String) =
   a.map { i -> b.map { j -> i to j } }.flatten()
    .joinToString("\n", "\n", "\n") { (i, j) -> map(i, j) }