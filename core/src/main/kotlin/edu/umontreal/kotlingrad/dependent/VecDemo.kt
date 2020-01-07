package edu.umontreal.kotlingrad.dependent

fun main() {
  val vectorOfNone = Vec<Int>().also { println("$it\n") }
//  val firstValueOfNone = vectorOfNone[D0] // Does not compile

  val vectorOfOne = Vec(1).also { println(it) }
  val vectorOfTwo = Vec(1, 2).also { println(it) }
  val vectorOfThree = Vec(1, 2, 3).also { println(it) }

  // Inferred type: Vec<Double, D3>List<Double>
  val add0Result = ((Vec(1.0, 2.0, 3.0) + Vec(3.0, 2.0, 1.0)) + Vec(0.0, 0.0, 0.0)).also { println("Addition result: $it\n") }
//  val add1Result = (Vec(1.0, 2.0, 3.0, 4.0) + Vec(3.0, 2.0, 1.0)) // Does not compile

  fun willOnlyAcceptVectorsOfLength2(l: Vec<Int, D2>) {}
  willOnlyAcceptVectorsOfLength2(vectorOfTwo)
//  willOnlyAcceptVectorsOfLength2(vectorOfThree) // Does not compile
//  willOnlyAcceptVectorsOfLength2(vectorOfOne) // Does not compile

  // Unsafe construction of vectors is allowed, but may fail at runtime
  val v = Vec(D100, IntArray(100) { 0 }.toList())
  v + v
//  v - Vec(1, 2, 3) // Does not compile
}