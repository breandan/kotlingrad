package edu.umontreal.kotlingrad.math.samples

sealed class Collideable {
  abstract operator fun times(other: Collideable)
}

object Asteroid: Collideable() {
  override fun times(other: Collideable) = when (other) {
    is Asteroid -> println("asteroid : asteroid")
    is Spaceship -> println("asteroid : spaceship")
  }
}

object Spaceship: Collideable() {
  override fun times(other: Collideable) = when (other) {
    is Asteroid -> println("spaceship : asteroid")
    is Spaceship -> println("spaceship : spaceship")
  }
}

fun main(args: Array<String>) {
  Asteroid * Asteroid
  Asteroid * Spaceship
  Spaceship * Asteroid
  Spaceship * Spaceship
}