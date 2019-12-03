package edu.umontreal.kotlingrad.samples

import kotlin.contracts.*

//@ExperimentalContracts
//fun String.isNullOrEmpty(): String {
//  contract {
//    returns(true) implies (toCharArray()?.all { it in 'a'..'z' })
//  }
//  return "test"
//}