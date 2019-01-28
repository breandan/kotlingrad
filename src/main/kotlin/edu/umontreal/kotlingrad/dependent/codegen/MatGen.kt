package edu.umontreal.kotlingrad.dependent.codegen

const val maxDim = 10

fun main() {
  var s =
    """
    package edu.umontreal.kotlingrad.dependent

    open class Mat<T, Rows: `$maxDim`, Cols: `$maxDim`> internal constructor(
      val rowT: Nat<Rows>,
      val colT: Nat<Cols>,
      /**TODO: Make contents a Vec<Vec<T, MaxCols>, MaxRows>**/
      val contents: List<Vec<T, Cols>> = arrayListOf()
    ) /**TODO: Maybe extend Vec? **/ {
      val numRows: Int = rowT.i
      val numCols: Int = colT.i
      operator fun get(i: Int): Vec<T, Cols> = contents[i]

      companion object {
  """.trimIndent()

  for (i in 1..maxDim)
    for (j in 1..maxDim)
      s += "    @JvmName(\"m${i}x${j}\") private operator fun <T, V: Vec<T, `$j`>> invoke(${(0 until i).map { "t$it: V" }.joinToString(", ")}): Mat<T, `$i`, `$j`> = Mat(`$i`, `$j`, arrayListOf(${(0 until i).map { "t$it" }.joinToString(", ")}))\n"

  s += "\n"

  for (i in 1..maxDim)
    for (j in 1..maxDim)
      s += "    @JvmName(\"m${i}x${j}f\") operator fun <T> invoke(m: Nat<`$i`>, n: Nat<`$j`>, ${(0 until (i * j)).map { "t$it: T" }.joinToString(", ")}) = Mat(Vec(${(0 until (i * j)).map { "t$it${if ((it + 1) == (i * j)) ")" else if ((it + 1) % j == 0) "), Vec(" else ", "}" }.joinToString("")})\n"

  s += """
      }

  override fun toString() = "(${'$'}numRows x ${'$'}numCols) ${'$'}contents"
}
  """.trimIndent()

  println(s)
}
