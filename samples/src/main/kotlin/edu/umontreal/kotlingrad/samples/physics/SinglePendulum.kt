package edu.umontreal.kotlingrad.samples.physics

import edu.umontreal.kotlingrad.functions.Fun
import edu.umontreal.kotlingrad.functions.ScalarVar
import edu.umontreal.kotlingrad.numerical.DoublePrecision
import edu.umontreal.kotlingrad.numerical.DoublePrecision.eval
import edu.umontreal.kotlingrad.numerical.DoublePrecision.invoke
import edu.umontreal.kotlingrad.numerical.DoubleReal
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.animation.Timeline.INDEFINITE
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.paint.Color.*
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.stage.Stage
import javafx.util.Duration
import java.awt.Point
import kotlin.math.*

@Suppress("NonAsciiCharacters", "PropertyName", "LocalVariableName")
class SinglePendulum(private val len: Double = 300.0): Application(), EventHandler<ActionEvent> {
  val pivot = Point(len.toInt() + 20, 0)
  val anchor = Circle(10.0, BLACK).apply { layoutX = len; layoutY = 0.0 }

  val rod1 = Line(len, 0.0, len * 2, 0.0).apply { strokeWidth = 3.0 }
  val rod2 = Line(len, 0.0, len * 2, 0.0).apply { strokeWidth = 3.0 }

  val bob1 = Circle(20.0, GREEN) // Ground truth
  val bob2 = Circle(20.0, RED)   // Prediction

  override fun start(stage: Stage) {
    val canvas = Pane().apply { children.addAll(anchor, rod1, rod2, bob2, bob1) }

    stage.apply {
      title = "Pendulum I"
      scene = Scene(canvas, 2 * len + 40, len + 40)
      show()
    }

    Timeline(KeyFrame(Duration.millis(ms), this)).apply { cycleCount = INDEFINITE }.play()
  }

  val ms = 20.0
  val G = 9.81
  val dt = 0.1
  var ω = 0.0
  var θ = PI / 3
  var ωP = ω
  var θP = θ
  var G1 = DoublePrecision.Var("G1", 1)
  var q = 0
  val sync = 180

  override fun handle(t: ActionEvent) {
    if (q < sync) {
      ωP = ω
      θP = θ
    } else if (q == sync) {
      println("\nGOING BLIND (OPEN LOOP)\n")
    }

    val dω = -G / len * sin(θ) * dt
    ω += dω
    θ += ω * dt

    bob1.layoutX = pivot.x + sin(θ) * len
    bob1.layoutY = pivot.y + cos(θ) * len
    rod1.endX = bob1.layoutX
    rod1.endY = bob1.layoutY

    with(DoublePrecision) {
      val dωp = -G1 / len * sin(θP) * dt
      val ωpf = ωP + dωp
      ωP += dωp.eval()
      val θPf = θP + ωpf * dt
      θP += (ωpf * dt).eval()
      val XPred = pivot.x + sin(θPf) * len
      val YPred = pivot.y + cos(θPf) * len
      bob2.layoutX = XPred.eval()
      bob2.layoutY = YPred.eval()
      rod2.endX = bob2.layoutX
      rod2.endY = bob2.layoutY
      val delXPred = (bob1.layoutX - XPred) pow 2
      val delYPred = (bob1.layoutY - YPred) pow 2
      val l2 = sqrt(delXPred + delYPred)
      if (q < sync) {
        G1 = l2.descend(steps = 100, vinit = 0.0, gamma = 0.9, α = 0.01, variable = G1)
        if (q % 10 == 0) println("G = ${G1.eval()}")
      } else if (q % 100 == 0) println("Loss: ${l2.eval()}")
      q++
    }
  }

  fun Fun<DoubleReal>.descend(steps: Int, vinit: Double, gamma: Double, α: Double = 0.01, variable: ScalarVar<DoubleReal>): ScalarVar<DoubleReal> {
    val d_dg = with(DoublePrecision) { d(this@descend) / d(variable) }
    var G1P = variable.eval()
    var velocity = vinit
    var i = 0
    do {
      velocity = gamma * velocity + α * d_dg(variable to G1P)
      G1P -= velocity
      i++
    } while (abs(velocity) > 0.00001 && i < steps)
    return DoublePrecision.Var("G1", G1P)
  }
}

fun main() = Application.launch(SinglePendulum::class.java)