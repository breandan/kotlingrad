package edu.umontreal.kotlingrad.dependent

fun main() {
  val vectorOfNone = Vec<Int>().also { println("$it\n") }
//  val firstValueOfNone = vectorOfNone[`0`] // Does not compile

  val vectorOfOne = Vec(1).also { println(it) }
  val firstValueOfOne = vectorOfOne[`0`].also { println("1st value: $it\n") }
//  val secondValueOfOne =  vectorOfOne[`1`] // Does not compile

  val vectorOfTwo = Vec(1, 2).also { println(it) }
  val firstValueOfTwo = vectorOfTwo[`0`].also { println("1st value: $it") }
  val secondValueOfTwo = vectorOfTwo[`1`].also { println("2nd value: $it\n") }
//  val thirdValueOfTwo = vectorOfTwo[`2`] // Does not compile

  val vectorOfThree = Vec(1, 2, 3).also { println(it) }
  val firstValueOfThree = vectorOfThree[`0`].also { println("1st value: $it") }
  val secondValueOfThree = vectorOfThree[`1`].also { println("2nd value: $it") }
  val thirdValueOfThree = vectorOfThree[`2`].also { println("3nd value: $it\n") }
//  val fourthValueOfThree = vectorOfThree[`3`] // Does not compile

  // Inferred type: Vec<Double, `3`>List<Double>
  val add0Result = ((Vec(1.0, 2.0, 3.0) + Vec(3.0, 2.0, 1.0)) + Vec(0.0, 0.0, 0.0)).also { println("Addition result: $it\n") }
//  val add1Result = (Vec(1.0, 2.0, 3.0, 4.0) + Vec(3.0, 2.0, 1.0)) // Does not compile

  val vectorOfFour = (vectorOfTwo cat vectorOfTwo).also { println(it) }
  val fourthValueOfFour = vectorOfFour[`3`].also { println("4th value: $it\n") }
//  val fifthValueOfFour = vectorOfFour[`4`] // Does not compile

//  val vectorOfFive = (vectorOfTwo + vectorOfThree).also { println(it) }
//  val fifthValueOfFive = vectorOfFive[`4`].also { println("5th value: $it\n") }
//  val sixthValueOfFive = vectorOfFive[`5`] // Does not compile

  fun willOnlyAcceptVectorsOfLength2(l: Vec<Int, `2`>) {}
  willOnlyAcceptVectorsOfLength2(vectorOfTwo)
//  willOnlyAcceptVectorsOfLength2(vectorOfThree) // Does not compile
//  willOnlyAcceptVectorsOfLength2(vectorOfOne) // Does not compile
}