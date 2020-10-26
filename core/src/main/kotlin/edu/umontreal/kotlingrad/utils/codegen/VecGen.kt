package edu.umontreal.kotlingrad.utils.codegen

fun main() {
  for (i in 2..maxInt)
    println("fun <Z: Number> Vec(${
      (0 until i).joinToString(", ") { "z$it: Z" }
    }) = VConst<X, D$i>(${
      (0 until i).joinToString(
        ", "
      ) { "wrap(z$it)" }
    })")
}

//fun <Z: Number> Vec(y0: Z, y1: Z) = VConst<X, D2>(wrap(y0), wrap(y1))
//fun <Z: Number> Vec(y0: Z, y1: Z, y2: Z) = VConst<X, D3>(wrap(y0), wrap(y1), wrap(y2))
//fun <Z: Number> Vec(y0: Z, y1: Z, y2: Z, y3: Z) = VConst<X, D4>(wrap(y0), wrap(y1), wrap(y2), wrap(y3))
//fun <Z: Number> Vec(y0: Z, y1: Z, y2: Z, y3: Z, y4: Z) = VConst<X, D5>(wrap(y0), wrap(y1), wrap(y2), wrap(y3), wrap(y4))
//fun <Z: Number> Vec(y0: Z, y1: Z, y2: Z, y3: Z, y4: Z, y5: Z) = VConst<X, D6>(wrap(y0), wrap(y1), wrap(y2), wrap(y3), wrap(y4), wrap(y5))
//fun <Z: Number> Vec(y0: Z, y1: Z, y2: Z, y3: Z, y4: Z, y5: Z, y6: Z) = VConst<X, D7>(wrap(y0), wrap(y1), wrap(y2), wrap(y3), wrap(y4), wrap(y5), wrap(y6))
//fun <Z: Number> Vec(y0: Z, y1: Z, y2: Z, y3: Z, y4: Z, y5: Z, y6: Z, y7: Z) = VConst<X, D8>(wrap(y0), wrap(y1), wrap(y2), wrap(y3), wrap(y4), wrap(y5), wrap(y6), wrap(y7))
//fun <Z: Number> Vec(y0: Z, y1: Z, y2: Z, y3: Z, y4: Z, y5: Z, y6: Z, y7: Z, y8: Z) = VConst<X, D9>(wrap(y0), wrap(y1), wrap(y2), wrap(y3), wrap(y4), wrap(y5), wrap(y6), wrap(y7), wrap(y8))