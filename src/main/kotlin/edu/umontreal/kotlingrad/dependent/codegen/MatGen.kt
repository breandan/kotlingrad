package edu.umontreal.kotlingrad.dependent.codegen

const val maxDim = 10

fun main() {
  var s = ""

  for (i in 1..maxDim)
    for (j in 1..maxDim)
      s += "    @JvmName(\"m${i}x${j}\") private operator fun <T, V: Vec<T, `$j`>> invoke(${(0 until i).joinToString(", ") { "t$it: V" }}): Mat<T, `$i`, `$j`> = Mat(`$i`, `$j`, Vec(${(0 until i).joinToString(", ") { "t$it" }}))\n"

  s += "\n"

  for (i in 1..maxDim)
    for (j in 1..maxDim)
      if (i == 1 && j == 1) continue
      else s += "    @JvmName(\"m${i}x${j}f\") operator fun <T> invoke(m: Nat<`$i`>, n: Nat<`$j`>, ${(0 until (i * j)).joinToString(", ") { "t$it: T" }}) = Mat(Vec(${(0 until (i * j)).joinToString("") { "t$it${if ((it + 1) == (i * j)) ")" else if ((it + 1) % j == 0) "), Vec(" else ", "}" }})\n"

  println(s)
}
