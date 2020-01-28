package edu.umontreal.kotlingrad.samples.physics

import edu.umontreal.kotlingrad.experimental.*
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.paint.Color.*
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.stage.Stage
import javafx.util.Duration
import kotlin.math.*

@Suppress("NonAsciiCharacters", "LocalVariableName")
class DoublePendulum(private val len: Double = 900.0) : Application(), EventHandler<ActionEvent> {
  var ω1: SFun<DReal> = DoublePrecision.wrap(0.0) // Angular velocities
  var ω2: SFun<DReal> = DoublePrecision.wrap(0.0)
  val m1 = 2.0 // Masses
  val m2 = 2.0
  var G: SFun<DReal> = DoublePrecision.wrap(9.81) // Gravity
  var µ = 0.01 // Friction
  val Gp = 0.01  // Simulate measurement error
  val µp = -0.01
  var r1 = DoublePrecision.Vec(1.0, 0.0) // Polar vector
  var r2 = DoublePrecision.Vec(1.0, 0.0)
  val observationSteps = 30
  var priorVal = 5.0
  fun step(obs: SFun<DReal>? = null, groundTruth: Pair<Vec<DReal, D2>, Vec<DReal, D2>>? = null) = with(DoublePrecision) {
    val isObserving = false
//    val priorVal = if(G is Var) G.asDouble()
    if (isObserving) {
      G = Var("G")
//      µ = Var("µ")
    }

    if (iter == observationSteps) println("\nGOING BLIND\n")

    val dt = 0.01
    val a1 = (r1.angle % (2.0 * PI)).let { it + if (it < 0) 2 * PI else 0.0 } - 3 * PI / 2
    val a2 = (r2.angle % (2.0 * PI)).let { it + if (it < 0) 2 * PI else 0.0 } - 3 * PI / 2

    val j1 = -G * (2.0 * m1 + m2) * sin(a1)
    val j2 = -m2 * G * sin(a1 - 2 * a2)
    val j3 = -2.0 * sin(a1 - a2) * m2
    val j4 = ω2 * ω2 * r2.magn + ω1 * ω1 * r1.magn * cos(a1 - a2)
    val cs = r1.magn * (2 * m1 + m2 - m2 * cos(2 * a1 - 2 * a2))
    val α1 = (j1 + j2 + j3 * j4) * (1 / cs) - µ * ω1
    ω1 += α1 * dt

    val k1 = 2.0 * sin(a1 - a2)
    val k2 = ω1 * ω1 * r1.magn * (m1 + m2)
    val k3 = G * (m1 + m2) * cos(a1)
    val k4 = ω2 * ω2 * r2.magn * m2 * cos(a1 - a2)
    val ds = r2.magn * (2 * m1 + m2 - m2 * cos(2 * a1 - 2 * a2))
    val α2 = k1 * (k2 + k3 + k4) * (1 / ds) + µ * ω2
    ω2 -= α2 * dt

    if(isObserving && obs != null) {
      val loss = (ω2 - obs) pow 2
      G = loss.descend(100, 0.0, 0.9, 0.1, G as Var<DReal> to wrap(priorVal))
      priorVal = G.toDouble()
      println(G)
    }

    val r1a = (r1.angle + (ω1 * dt + .5 * α1 * dt * dt)).run {
      if(G is Var) this(G as Var<DReal> to priorVal).toDouble() else try {
        toDouble()
      } catch(e: Exception) {println(this); this(bindings.sVars.first() to priorVal).toDouble() }
    }
    val r2a = (r2.angle + - (ω2 * dt + .5 * α2 * dt * dt)).run {
      if(G is Var) this(G as Var<DReal> to priorVal).toDouble() else try {
        toDouble()
      } catch(e: Exception) {println(this); this(bindings.sVars.first() to priorVal).toDouble() }
    }

    if(G is Var) {
      ω1 = ω1(G as Var<DReal> to priorVal)
      ω2 = ω2(G as Var<DReal> to priorVal)
    }

    r1 = Vec(r1.magn * cos(r1a), r1.magn * sin(r1a))
    r2 = Vec(r2.magn * cos(r2a), r2.magn * sin(r2a))

    // Resync state while observing
    if(isObserving) {
      r1 = Vec(r1.magn * cos(r1a), r1.magn * sin(r1a))
      r2 = Vec(r2.magn * cos(r2a), r2.magn * sin(r2a))
    }
  }

  fun SFun<DReal>.descend(steps: Int, vinit: Double, gamma: Double, α: Double = 0.1, map: Pair<Var<DReal>, SFun<DReal>>): SFun<DReal> {
    with(DoublePrecision) {
      val d_dg = this@descend.d(map.first)
      var G1P: SFun<DReal> = map.second
      var velocity: SFun<DReal> = wrap(vinit)
      var i = 0
      do {
        velocity = gamma * velocity + d_dg(map.first to G1P) * α
        G1P -= velocity
        i++
      } while (abs(velocity.toDouble()) > 0.00001 && i < steps)

      return G1P
    }
  }

  val rod1 = Line(len, 0.0, len, 0.0).apply { strokeWidth = 3.0 }
  val rod2 = Line(len, 0.0, len, 0.0).apply { strokeWidth = 3.0 }

  val rodLen = len / 4

  val anchor = Circle(10.0, BLACK).apply { layoutX = 470.0; layoutY = 200.0 }

  val bob1 = Circle(m1 * 10.0, GREEN)
  val bob2 = Circle(m2 * 10.0, GREEN) // Ground truth

  lateinit var twin: DoublePendulum
  lateinit var rod3: Line
  lateinit var rod4: Line
  lateinit var bob3: Circle
  lateinit var bob4: Circle           // Prediction

  lateinit var canvas: Pane

  override fun start(stage: Stage) {
    twin = DoublePendulum().apply {
      with(DoublePrecision) {
        G += Gp // Perturb gravity and friction
        µ += µp

        // TODO: Erase these parameters -- should be fully learned
//    µ = 0.0
//    G = 0.0
      }
    }

    rod3 = twin.rod1
    rod4 = twin.rod2
    bob3 = Circle(twin.m1 * 10, RED)
    bob4 = Circle(twin.m2 * 10, RED)

    canvas = Pane().apply { children.addAll(anchor, rod3, rod4, bob3, bob4, rod1, rod2, bob1, bob2) }

    stage.apply {
      title = "Pendulum II"
      scene = Scene(canvas, len + 40, len + 40)
      show()
    }

    Timeline(KeyFrame(Duration.millis(20.0), this)).apply { cycleCount = Timeline.INDEFINITE }.play()
  }

  val Vec<DReal, D2>.r: Double
    get() = DoublePrecision.run { this@r[0].toDouble() }
  val Vec<DReal, D2>.theta: Double
    get() = DoublePrecision.run { this@theta[1].toDouble() }

  val Vec<DReal, D2>.end: Vec<DReal, D2>
    get() = DoublePrecision.run { Vec(this@end.r + rodLen * magn * cos(angle), this@end.theta - rodLen * magn * sin(angle)) }

  val Vec<DReal, D2>.magn: Double
    get() = DoublePrecision.run { magnitude().toDouble() }

  val Vec<DReal, D2>.angle: Double
    get() = DoublePrecision.run { atan2(this@angle.theta, this@angle.r) }

  fun Vec<DReal, D2>.render(rod: Line, xAdjust: Double = 0.0, yAdjust: Double = 0.0) {
    rod.startX = this@render.r + xAdjust
    rod.startY = this@render.theta + yAdjust
    val end = this@render.end
    rod.endX = end.r + xAdjust
    rod.endY = end.theta + yAdjust
  }

  private fun renderBobs() {
    bob1.layoutX = rod1.endX
    bob1.layoutY = rod1.endY
    bob2.layoutX = rod2.endX
    bob2.layoutY = rod2.endY
    bob3.layoutX = rod3.endX
    bob3.layoutY = rod3.endY
    bob4.layoutX = rod4.endX
    bob4.layoutY = rod4.endY
  }

  var iter = 0

  override fun handle(t: ActionEvent) {
    this.step()
    twin.step(ω2, Pair(r1, r2))
    if (iter > 0) canvas.children.removeAt(canvas.children.size - 1) // Clears previous text

    renderArms()
    tracePaths()
    renderBobs()
    renderInfo()
    iter++
  }

  private fun renderInfo() {
    val bob1Err = DoublePrecision.run { ((r1.end - twin.r1.end).magnitude() * (1 / 200.0)) }.toString().subSequence(0, 5)
    val bob2Err = DoublePrecision.run { ((r2.end - twin.r2.end).magnitude() * (1 / 200.0)) }.toString().subSequence(0, 5)
    canvas.children.add(Text("Iteration: $iter\nbob_1 error: $bob1Err\nbob_2 error: $bob2Err\nG = $G, Ĝ = ${twin.G}\nµ = $µ, µ\u0302 = ${twin.µ}").apply { layoutX = 300.0; layoutY = 50.0; font = Font("Monospaced", 20.0) })
  }

  private fun renderArms() {
    r1.render(rod1, anchor.layoutX, anchor.layoutY)
    r2.render(rod2, rod1.endX, rod1.endY)
//    if (iter > 300) {
    twin.r1.render(rod3, anchor.layoutX, anchor.layoutY)
    twin.r2.render(rod4, rod3.endX, rod3.endY)
//    } else {
//      r1.render(rod3, 200.0, 1.0, anchor.layoutX, anchor.layoutY)
//      r2.render(rod4, 200.0, 1.0, rod3.endX, rod3.endY)
//    }
  }

  private fun tracePaths() {
    canvas.children.addAll(
      Circle(1.0, GREEN).apply { layoutX = rod2.endX; layoutY = rod2.endY },
      Circle(1.0, RED).apply { layoutX = rod4.endX; layoutY = rod4.endY }
    )

    while (canvas.children.size > 1000) canvas.children.remove(10, 12)
  }
}

fun main() = Application.launch(DoublePendulum::class.java)