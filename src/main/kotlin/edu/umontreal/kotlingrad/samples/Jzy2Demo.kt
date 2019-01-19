package edu.umontreal.kotlingrad.samples


import net.miginfocom.swing.MigLayout
import org.jzy3d.chart.Chart
import org.jzy3d.chart2d.Chart2d
import org.jzy3d.colors.Color
import org.jzy3d.plot2d.primitives.Serie2d
import org.jzy3d.plot3d.primitives.ConcurrentLineStrip
import org.jzy3d.plot3d.primitives.axes.layout.providers.PitchTickProvider
import org.jzy3d.plot3d.primitives.axes.layout.renderers.PitchTickRenderer
import org.jzy3d.ui.LookAndFeel
import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.IOException
import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JPanel

/**
 * Showing a pair of 2d charts to represent pitch and amplitude variation of an
 * audio signal.
 *
 * Noticed problems on chart resize. Suspect "wrong stuffs" around miglayout or jogl.
 *
 * FIXME : use ChartGroup to build interface. Miglayout/JOGL interaction causes problem when downsizing windows
 *
 * @author Martin Pernollet
 */

object Jzy2Demo {
  var duration = 60f
  /** milisecond distance between two generated samples */
  var interval = 50
  var maxfreq = 880
  var nOctave = 5

  /** Simple timer  */
  internal var start: Long = 0

  @JvmStatic
  fun main(args: Array<String>) {
    val log = PitchAmpliControlCharts(duration, maxfreq, nOctave)
    TimeChartWindow(log.charts)

    generateSamplesInTime(log)
    // generateSamples(log, 500000);
  }

  @Throws(InterruptedException::class)
  fun generateSamples(log: PitchAmpliControlCharts, n: Int) {
    println("will generate $n samples")

    for (i in 0 until n) {
      // Random audio info
      val pitch = Math.random() * maxfreq
      val ampli = Math.random()

      // Add to time series
      log.seriePitch.add(time(n, i), pitch)
      log.serieAmpli.add(time(n, i), ampli)
    }
  }

  fun time(n: Int, i: Int) = i.toDouble() / n * duration

  @Throws(InterruptedException::class)
  fun generateSamplesInTime(log: PitchAmpliControlCharts) {
    println("will generate approx. " + duration * 1000 / interval + " samples")

    start = System.nanoTime()

    while (elapsed() < duration) {
      // Random audio info
      val pitch = Math.random() * maxfreq
      val ampli = Math.random()

      // Add to time series
      log.seriePitch.add(elapsed(), pitch)
      log.serieAmpli.add(elapsed(), ampli)

      // Wait a bit
      Thread.sleep(interval.toLong())
    }
  }

  /** Hold 2 charts, 2 time series, and 2 drawable lines  */
  class PitchAmpliControlCharts(timeMax: Float, freqMax: Int, nOctave: Int) {
    var pitchChart: Chart2d = Chart2d()
    var ampliChart: Chart2d
    var seriePitch: Serie2d
    var serieAmpli: Serie2d
    var pitchLineStrip: ConcurrentLineStrip
    var amplitudeLineStrip: ConcurrentLineStrip

    val charts: List<Chart>
      get() = arrayListOf(pitchChart, ampliChart)

    init {
      pitchChart.asTimeChart(timeMax, 0f, freqMax.toFloat(), "Time", "Frequency")

      val axe = pitchChart.axeLayout
      axe.yTickProvider = PitchTickProvider(nOctave)
      axe.yTickRenderer = PitchTickRenderer()

      seriePitch = pitchChart.getSerie("frequency", Serie2d.Type.LINE)
      seriePitch.color = Color.BLUE
      pitchLineStrip = seriePitch.drawable as ConcurrentLineStrip

      ampliChart = Chart2d()
      ampliChart.asTimeChart(timeMax, 0f, 1.1f, "Time", "Amplitude")
      serieAmpli = ampliChart.getSerie("amplitude", Serie2d.Type.LINE)
      serieAmpli.color = Color.RED
      amplitudeLineStrip = serieAmpli.drawable as ConcurrentLineStrip
    }
  }

  class TimeChartWindow @Throws(IOException::class)

  constructor(charts: List<Chart>): JFrame() {

    init {
      LookAndFeel.apply()
      val lines = "[300px]"
      val columns = "[500px,grow]"
      layout = MigLayout("", columns, lines)
      for ((k, c) in charts.withIndex()) addChart(c, k)
      windowExitListener()
      pack()
      show()
      isVisible = true
    }

    fun addChart(chart: Chart, id: Int) {
      val canvas = chart.canvas as java.awt.Component

      val chartPanel = JPanel(BorderLayout())
      /*chartPanel.setMaximumSize(null);
            chartPanel.setMinimumSize(null);
            canvas.setMinimumSize(null);
            canvas.setMaximumSize(null);*/

      val b = BorderFactory.createLineBorder(java.awt.Color.black)
      chartPanel.border = b
      chartPanel.add(canvas, BorderLayout.CENTER)
      add(chartPanel, "cell 0 $id, grow")
    }

    fun windowExitListener() {
      addWindowListener(object: WindowAdapter() {
        override fun windowClosing(e: WindowEvent?) {
          this@TimeChartWindow.dispose()
          System.exit(0)
        }
      })
    }
  }

  fun elapsed() = (System.nanoTime() - start) / 1000000000.0
}