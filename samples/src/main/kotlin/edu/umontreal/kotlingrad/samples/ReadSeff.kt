package edu.umontreal.kotlingrad.samples

import org.nield.kotlinstatistics.standardDeviation
import java.io.*
import kotlin.math.sqrt

fun List<Double>.standardError() = standardDeviation() / sqrt(size.toDouble())

fun main() {
  File("seff.log").readLines().filter { it.contains(",") && it.contains("0") }
    .map { it.split(", ").let { Triple(it[0].toDouble(), it[1].toDouble(), it[2].toDouble()) } }
    .groupBy { it.first }.mapValues {
      listOf(it.key,
        it.value.map { it.second }.average(),
        it.value.map { it.second }.standardError(),
        it.value.map { it.third }.average(),
        it.value.map { it.third }.standardError())
    }.forEach { println(it.value.joinToString(", ")) }

  ObjectInputStream(FileInputStream("losses.hist")).use {
    val t = it.readObject()
    (t as List<List<Triple<Int, Double, Double>>>).flatten().groupBy { it.first }.mapValues {
      listOf(it.key,
        it.value.map { it.second }.average(),
        it.value.map { it.second }.standardError(),
        it.value.map { it.third }.average(),
        it.value.map { it.third }.standardError()
      )
    }.forEach { println(it.value.joinToString(", ")) }
  }
}