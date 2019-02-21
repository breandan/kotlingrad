package edu.umontreal.kotlingrad.dependent.codegen

const val maxInt = 100

fun main() {
  println("package edu.umontreal.kotlingrad.dependent\n\n")

  for(i in 0 until maxInt)
    println("open class `$i`(override val i: Int = $i): `${i + 1}`(i) { companion object: `$i`(), Nat<`$i`> }")

  println("\nsealed class `$maxInt`(open val i: Int = $maxInt) {\n  companion object: `$maxInt`(), Nat<`$maxInt`>\n  override fun toString() = \"\$i\"\n}\n")

  println("interface Nat<T: `$maxInt`> { val i: Int }")
}