package co.ndan.kotlingrad.math.types.dependent

fun main(args: kotlin.Array<String>) {
  val arrayOfNone = Array<Int, `0`>().also { println("$it\n") }
//  val firstValueOfNone = arrayOfNone[`0`] // Does not compile

  val arrayOfOne = Array(1).also { println(it) }
  val firstValueOfOne = arrayOfOne[`0`].also { println("1st value: $it\n") }
//  val secondValueOfOne =  arrayOfOne[`1`] // Does not compile

  val arrayOfTwo = Array(1, 2).also { println(it) }
  val firstValueOfTwo = arrayOfTwo[`0`].also { println("1st value: $it") }
  val secondValueOfTwo = arrayOfTwo[`1`].also { println("2nd value: $it\n") }
//  val thirdValueOfTwo = arrayOfTwo[`2`] // Does not compile

  val arrayOfThree = Array(1, 2, 3).also { println(it) }
  val firstValueOfThree = arrayOfThree[`0`].also { println("1st value: $it") }
  val secondValueOfThree = arrayOfThree[`1`].also { println("2nd value: $it") }
  val thirdValueOfThree = arrayOfThree[`2`].also { println("3nd value: $it\n") }
//  val fourthValueOfThree = arrayOfThree[`3`] // Does not compile

  val arrayOfFour = (arrayOfTwo + arrayOfTwo).also { println(it) }
  val fourthValueOfFour = arrayOfFour[`3`].also { println("4th value: $it\n") }
//  val fifthValueOfFour = arrayOfFour[`4`] // Does not compile

  val arrayOfFive = (arrayOfTwo + arrayOfThree).also { println(it) }
  val fifthValueOfFive = arrayOfFive[`4`].also { println("5th value: $it\n") }
//  val sixthValueOfFive = arrayOfFive[`5`] // Does not compile

  fun willOnlyAcceptArraysOfLength2(l: Array<Int, `2`>) = l.contents
  willOnlyAcceptArraysOfLength2(arrayOfTwo)
//  willOnlyAcceptArraysOfLength2(arrayOfThree) // Does not compile
//  willOnlyAcceptArraysOfLength2(arrayOfOne) // Does not compile
}