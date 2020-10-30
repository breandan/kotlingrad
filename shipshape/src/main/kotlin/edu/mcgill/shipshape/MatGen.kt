package edu.mcgill.shipshape

fun genMat() =
  genMatOfScalars() +
    genMatOfSFun() +
    genMatOfVec()

private fun genMatOfScalars() =
  (1..maxDim).map { i -> (1..maxDim).map { j -> i to j } }.flatten()
    .joinToString("\n", "\n", "\n") { (i, j) ->
      "fun <X: RealNumber<X, Y>, Y: Number> RealNumber<X, Y>.Mat${i}x${j}(${
        (1..(i * j)).joinToString(", ") { "y$it: Y" }
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
  (1..maxDim).map { i -> (1..maxDim).map { j -> i to j } }.flatten()
    .joinToString("\n", "\n", "\n") { (i, j) ->
      "fun <X: SFun<X>> Mat${i}x${j}(${
        (1..(i * j)).joinToString(", ") { "y$it: SFun<X>" }
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
  (1..maxDim).map { i -> (1..maxDim).map { j -> i to j } }.flatten()
    .joinToString("\n", "\n", "\n") { (i, j) ->
      "fun <X: SFun<X>> Mat${i}x${j}(${
        (1..i).joinToString(", ") { "y$it: Vec<X, D$j>" }
      }): Mat<X, D$i, D$j> = Mat(${
        (1..i).joinToString(", ") { "y$it" }
      })"
    }