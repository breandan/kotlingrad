package edu.umontreal.kotlingrad.samples.physics

import edu.umontreal.kotlingrad.samples.*
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.paint.Color.GREEN
import javafx.scene.paint.Color.RED
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.stage.Stage
import javafx.util.Duration
import kotlin.math.*


class DoublePendulum(private val len: Double = 900.0) : Application(), EventHandler<ActionEvent> {
  val rod1 = Line(len, 0.0, len, 0.0).apply { strokeWidth = 3.0 }
  val rod2 = Line(len, 0.0, len, 0.0).apply { strokeWidth = 3.0 }
  lateinit var rod3: Line
  lateinit var rod4: Line
  val bob1 = Circle(20.0, GREEN)
  val bob2 = Circle(20.0, GREEN)
  val bob3 = Circle(20.0, RED)
  val bob4 = Circle(20.0, RED)
  lateinit var twin: DoublePendulum


  override fun start(stage: Stage) {
    twin = DoublePendulum()
    rod3 = twin.rod1
    rod4 = twin.rod2
    twin.G = 9.9

    val canvas = Pane().apply { children.addAll(rod1, rod2, rod3, rod4, bob3, bob4, bob1, bob2) }

    stage.apply {
      title = "Pendulum II"
      scene = Scene(canvas, len + 40, len + 40)
      show()
    }

    Timeline(KeyFrame(Duration.millis(20.0), this)).apply { cycleCount = Timeline.INDEFINITE }.play()
  }

  var a1 = 0.0
  var a2 = 0.0
  var ω1 = 0.0
  var ω2 = 0.0
  var α1 = 0.0
  var α2 = 0.0
  var m1 = 2.0
  var m2 = 2.0

  var G = 9.8
  var µ = 0.1

  var r1 = DoublePrecision.Vec(1.0, 0.0)
  var r2 = DoublePrecision.Vec(1.0, 0.0)

  val VConst<DoubleReal, `2`>.x: Double
    get() = DoublePrecision.run { this@x[0].asDouble() }
  val VConst<DoubleReal, `2`>.y: Double
    get() = DoublePrecision.run { this@y[1].asDouble() }

  val VConst<DoubleReal, `2`>.magn: Double
    get() = DoublePrecision.run { magnitude().asDouble() }

  val VConst<DoubleReal, `2`>.angle: Double
    get() = DoublePrecision.run { atan2(this@angle.y, this@angle.x) }

  fun makeVec(magn: Double, angle: Double) =
    DoublePrecision.run { Vec(magn * cos(angle), magn * sin(angle)) }

  fun VConst<DoubleReal, `2`>.render(rod: Line, maxLength: Double, maxMagnitude: Double, xAdjust: Double = 0.0, yAdjust: Double = 0.0) {
    with(DoublePrecision) {
      rod.startX = this@render.x + xAdjust
      rod.startY = this@render.y + yAdjust
      rod.endX = this@render.x + cos(angle) * (maxLength * (magn / maxMagnitude).coerceAtMost(1.0)) + xAdjust
      rod.endY = this@render.y - sin(angle) * (maxLength * (magn / maxMagnitude).coerceAtMost(1.0)) + yAdjust
    }
  }

  fun renderBobs() {
    bob1.layoutX = rod1.endX
    bob1.layoutY = rod1.endY
    bob2.layoutX = rod2.endX
    bob2.layoutY = rod2.endY
    bob3.layoutX = rod3.endX
    bob3.layoutY = rod3.endY
    bob4.layoutX = rod4.endX
    bob4.layoutY = rod4.endY
  }

  override fun handle(t: ActionEvent) {
    update(0.02)
    twin.update(0.02)

    r1.render(rod1, 200.0, 1.0, 450.0, 300.0)
    r2.render(rod2, 200.0, 1.0, rod1.endX, rod1.endY)
    twin.r1.render(rod3, 200.0, 1.0, 450.0, 300.0)
    twin.r2.render(rod4, 200.0, 1.0, rod3.endX, rod3.endY)

    renderBobs()
  }


  fun update(dt: Double) {
    a1 = (r1.angle % (2.0 * PI)).let {
      it + if (it < 0) 2 * PI else 0.0
    } - 3 * PI / 2
    a2 = (r2.angle % (2.0 * PI)).let {
      it + if (it < 0) 2 * PI else 0.0
    } - 3 * PI / 2

    var k1 = -G * (2.0 * m1 + m2) * sin(a1)
    var k2 = -m2 * G * sin(a1 - 2 * a2)
    var k3 = -2.0 * sin(a1 - a2) * m2
    var k4 = ω2 * ω2 * r2.magn + ω1 * ω1 * r1.magn * cos(a1 - a2)
    var ds = r1.magn * (2 * m1 + m2 - m2 * cos(2 * a1 - 2 * a2))
    α1 = (k1 + k2 + k3 * k4) / ds - µ * ω1

    k1 = 2.0 * sin(a1 - a2)
    k2 = ω1 * ω1 * r1.magn * (m1 + m2)
    k3 = G * (m1 + m2) * cos(a1)
    k4 = ω2 * ω2 * r2.magn * m2 * cos(a1 - a2)
    ds = r2.magn * (2 * m1 + m2 - m2 * cos(2 * a1 - 2 * a2))
    α2 = k1 * (k2 + k3 + k4) / ds + µ * ω2

    ω1 += α1 * dt
    ω2 -= α2 * dt

    r1 = makeVec(r1.magn, r1.angle + (ω1 * dt + .5 * α1 * dt * dt))
    r2 = makeVec(r2.magn, r2.angle - (ω2 * dt + .5 * α2 * dt * dt))
  }
}

fun main() = Application.launch(DoublePendulum::class.java)