package edu.umontreal.kotlingrad.samples.physics

import edu.umontreal.kotlingrad.samples.resourcesPath
import javafx.animation.*
import javafx.application.Application
import javafx.event.*
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.paint.Color.RED
import javafx.scene.shape.Circle
import javafx.stage.Stage
import javafx.util.Duration
import java.io.File
import kotlin.math.sqrt
import kotlin.math.pow

class Vector3D(val x: Double, val y: Double, val z: Double) {
  operator fun plus(v: Vector3D) = Vector3D(x + v.x, y + v.y, z + v.z)
  operator fun minus(v: Vector3D) = Vector3D(x - v.x, y - v.y, z - v.z)
  operator fun times(s: Double) = Vector3D(s * x, s * y, s * z)

  val mod = sqrt(x * x + y * y + z * z)
}

val origin = Vector3D(0.0, 0.0, 0.0)

class NBodySimulator(fileName: String = "$resourcesPath/nbody.txt"): Application(), EventHandler<ActionEvent> {
  val gc: Double
  val bodies: Int
  val timeSteps: Int
  val masses: DoubleArray
  val positions: Array<Vector3D>
  val velocities: Array<Vector3D>
  val accelerations: Array<Vector3D>

  init {
    val f = File(fileName)
    val lines = f.readLines()
    val (g, b, t) = lines[0].split(' ')
    gc = g.toDouble()
    bodies = b.toInt()
    timeSteps = t.toInt()
    masses = DoubleArray(bodies)
    positions = Array(bodies) { origin }
    velocities = Array(bodies) { origin }
    accelerations = Array(bodies) { origin }
    for (i in 0 until bodies) {
      masses[i] = lines[i * 3 + 1].toDouble()
      positions[i] = decompose(lines[i * 3 + 2])
//      velocities[i] = decompose(lines[i * 3 + 3])
    }
    println("Contents of $fileName")
    println(f.readText())
    print("Body   :      x          y          z    |")
    println("     vx         vy         vz")
  }

  private fun decompose(line: String): Vector3D {
    val (x, y, z) = line.split(' ').map { it.toDouble() }
    return Vector3D(x, y, z)
  }

  private fun resolveCollisions() {
    for (i in 0 until bodies) {
      for (j in i + 1 until bodies) {
        if (positions[i].x == positions[j].x &&
            positions[i].y == positions[j].y &&
            positions[i].z == positions[j].z) {
          val temp = velocities[i]
          velocities[i] = velocities[j]
          velocities[j] = temp
        }
      }
    }
  }

  private fun computeAccelerations() {
    for (i in 0 until bodies) {
      accelerations[i] = origin
      for (j in 0 until bodies) {
        if (i != j) {
          val temp = gc * masses[j] / (positions[i] - positions[j]).mod.pow(3)
          accelerations[i] += (positions[j] - positions[i]) * temp
        }
      }
    }
  }

  private fun computeVelocities() {
    for (i in 0 until bodies) velocities[i] += accelerations[i]
  }

  private fun computePositions() {
    for (i in 0 until bodies) positions[i] += velocities[i] + accelerations[i] * 0.5
  }

  fun printResults() {
    val fmt = "Body %d : % 8.6f  % 8.6f  % 8.6f | % 8.6f  % 8.6f  % 8.6f"
    for (i in 0 until bodies) {
      println(fmt.format(
          i + 1,
          positions[i].x,
          positions[i].y,
          positions[i].z,
          velocities[i].x,
          velocities[i].y,
          velocities[i].z
      ))
    }
  }

  val ms = 1.0

  val bodiesRendered = positions.map { Circle(it.x * 0.01 + 500, it.y* 0.01 + 500, 10.0, RED) }

  override fun start(stage: Stage) {
    val canvas = Pane().apply { children.addAll(bodiesRendered) }

    stage.apply {
      title = "n-body simulator"
      scene = Scene(canvas, 1000.0, 1000.0)
      show()
    }

    Timeline(KeyFrame(Duration.millis(ms), this)).apply { cycleCount = Timeline.INDEFINITE }.play()
  }

  override fun handle(p0: ActionEvent?) {
    computeAccelerations()
    computePositions()
    bodiesRendered.forEachIndexed { i, it -> it.centerX = positions[i].x * 0.01 + 500; it.centerY = positions[i].y * 0.01 + 500}
    computeVelocities()
//    resolveCollisions()
    printResults()
  }
}

fun main() {
//  val nb1 = NBody()
//  val nb2 = NBody()
  Application.launch(NBodySimulator::class.java)
}
