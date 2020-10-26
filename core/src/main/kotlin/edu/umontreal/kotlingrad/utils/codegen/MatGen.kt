package edu.umontreal.kotlingrad.utils.codegen

const val maxDim = 10

// TODO: Update codegen
fun main() {
  var s = ""

  for (i in 1..maxDim)
    for (j in 1..maxDim)
      s += "@JvmName(\"m${i}x$j\") private operator fun <T, V: Vec<T, `$j`>> invoke(${
        (0 until i).joinToString(", ") { "t$it: V" }
      }): Mat<T, `$i`, `$j`> = Mat(`$i`, `$j`, Vec(${
        (0 until i).joinToString(", ") { "t$it" }
      }))\n"

  s += "\n"

  for (i in 1..maxDim)
    for (j in 1..maxDim)
      if (i == 1 && j == 1) continue
      else s += "@JvmName(\"m${i}x${j}f\") operator fun <T> invoke(m: Nat<`$i`>, n: Nat<`$j`>, ${
        (0 until (i * j)).joinToString(", ") { "t$it: T" }
      }) = Mat(Vec(${
        (0 until (i * j)).joinToString("") {
          "t$it${
            if ((it + 1) == (i * j)) ")" else if ((it + 1) % j == 0) "), Vec(" else ", "
          }"
        }
      })\n"

  println(s)
}

//fun <Z: Number> Mat1x1(y0: Z) = MConst<X, D1, D1>(Vec(y0))
//fun <Z: Number> Mat1x2(y0: Z, y1: Z) = MConst<X, D1, D2>(Vec(y0, y1))
//fun <Z: Number> Mat1x3(y0: Z, y1: Z, y2: Z) = MConst<X, D1, D3>(Vec(y0, y1, y2))
//fun <Z: Number> Mat2x1(y0: Z, y1: Z) = MConst<X, D2, D1>(Vec(y0), Vec(y1))
//fun <Z: Number> Mat2x2(y0: Z, y1: Z, y2: Z, y3: Z) = MConst<X, D2, D2>(Vec(y0, y1), Vec(y2, y3))
//fun <Z: Number> Mat2x3(y0: Z, y1: Z, y2: Z, y3: Z, y4: Z, y5: Z) = MConst<X, D2, D3>(Vec(y0, y1, y2), Vec(y3, y4, y5))
//fun <Z: Number> Mat3x1(y0: Z, y1: Z, y2: Z) = MConst<X, D3, D1>(Vec(y0), Vec(y1), Vec(y2))
//fun <Z: Number> Mat3x2(y0: Z, y1: Z, y2: Z, y3: Z, y4: Z, y5: Z) = MConst<X, D3, D2>(Vec(y0, y1), Vec(y2, y3), Vec(y4, y5))
//fun <Z: Number> Mat3x3(y0: Z, y1: Z, y2: Z, y3: Z, y4: Z, y5: Z, y6: Z, y7: Z, y8: Z) = MConst<X, D3, D3>(Vec(y0, y1, y2), Vec(y3, y4, y5), Vec(y6, y7, y8))