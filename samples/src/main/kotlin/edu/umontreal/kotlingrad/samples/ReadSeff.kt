package edu.umontreal.kotlingrad.samples

import org.nield.kotlinstatistics.standardDeviation
import java.io.*
import kotlin.math.sqrt

fun main() {
  File("$resourcesPath/seff.log").readLines().filter { it.contains(",") && it.contains("0") }
    .map { it.split(", ").let { Triple(it[0].toDouble(), it[1].toDouble(), it[2].toDouble()) } }
    .groupBy { it.first }.mapValues {
      listOf(it.key,
        it.value.map { it.second }.average(),
        it.value.map { it.second }.let { it.standardDeviation() / sqrt(it.size.toDouble()) },
        it.value.map { it.third }.average(),
        it.value.map { it.third }.let { it.standardDeviation() / sqrt(it.size.toDouble()) })
    }.forEach { println(it.value.joinToString(", ")) }

  ObjectInputStream(FileInputStream("checkpoint.hist")).use {
    val t = it.readObject()
    (t as List<List<Pair<Int, Double>>>)
      .flatten().groupBy { it.first }
      .mapValues {
        Triple(
          it.key,
          it.value.map { it.second }.average(),
          it.value.map { it.second }.let { it.standardDeviation() / sqrt(it.size.toDouble()) }
        )
      }.forEach { println("${it.value.first}, ${it.value.second}, ${it.value.third}") }
  }
}