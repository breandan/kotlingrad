package edu.umontreal.kotlingrad.samples

import astminer.common.model.Parser
import astminer.parse.antlr.SimpleNode
import astminer.parse.antlr.python.PythonParser
import com.jujutsu.tsne.TSne
import com.jujutsu.tsne.barneshut.ParallelBHTsne
import com.jujutsu.utils.TSneUtils
import edu.mcgill.kaliningraph.*
import edu.mcgill.kaliningraph.circuits.ComputationGraph
import edu.umontreal.kotlingrad.experimental.*
import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.plot.PlotSvgExport
import jetbrains.letsPlot.*
import jetbrains.letsPlot.geom.*
import jetbrains.letsPlot.intern.toSpec
import jetbrains.letsPlot.label.ggtitle
import java.io.File
import kotlin.random.Random

fun main() {
//  val (labels, graphs) = generateASTs()
//  val (labels, graphs) = mineASTs()
  val (labels, graphs) = generateDigraphs()

  val rounds = listOf(1)//, 2, 5, 10)
  for (round in rounds) {
    val X = graphs.map { it.gnn(t = round * 10).nz_values }.padded()
    val embeddings = embedGraph(X)
    val clusters = plot(round, graphs, embeddings, labels)

    File.createTempFile("clusters", ".html")
      .apply { writeText("<html>$clusters</html>") }.show()
  }
}

// Pad length to a common vector length for TNSE
private fun List<DoubleArray>.padded(): Array<DoubleArray> =
  map { it.size }.maxOrNull()!!.let { maxDim ->
    map { cur ->
      DoubleArray(maxDim) { if (it < cur.size) cur[it] else 0.0 }
    }.toTypedArray()
  }

private fun plot(
  messagePassingRounds: Int,
  graphs: List<LabeledGraph>,
  embeddings: Array<out DoubleArray>,
  labels: List<String>
): String {
  val data = mapOf(
    "labels" to labels,
    "x" to embeddings.map { it[0] },
    "y" to embeddings.map { it[1] }
  )
  var plot = lets_plot(data) { x = "x"; y = "y"; color = "labels" } +
    ggsize(300, 250) + geom_point(size = 6) +
    ggtitle("Graph Types by Structural Similarity (t = $messagePassingRounds)") +
    theme().axisLine_blank().axisTitle_blank().axisTicks_blank().axisText_blank()
//  plot = names.foldIndexed(plot) { i, plt, f -> plt +
//    geom_text(x = embeddings[i][0] + 5, y = embeddings[i][1] + 5, label = f, color= BLACK)
//  }
  return PlotSvgExport.buildSvgImageFromRawSpecs(
    plotSpec = plot.toSpec(), plotSize = DoubleVector(1000.0, 500.0)
  )
}

private fun embedGraph(
  X: Array<DoubleArray>,
  outputDims: Int = 2,
  perplexity: Double = 10.0,
  tSne: TSne = ParallelBHTsne()
): Array<out DoubleArray> =
  tSne.tsne(TSneUtils.buildConfig(X, outputDims, X.size - 1, perplexity, 5000))

fun mineASTs(
  dataDir: String = {}.javaClass.getResource("/datasets/python").path,
  parser: Parser<SimpleNode> = PythonParser()
): Pair<List<String>, List<LabeledGraph>> =
  File(dataDir).walk().filter { it.extension == "py" }
    .map { parser.parse(it.inputStream())!!.apply { setToken(it.nameWithoutExtension) } }
    .map { it.toKGraph().let { (it.size / 10).toString() to it } }
    .toList().unzip()

fun generateASTs(
  heights: IntRange = 2..5, numExps: Int = 50,
//  generator: ExpressionGenerator<DReal> = ExpressionGenerator(DoublePrecision)
): Pair<List<String>, List<ComputationGraph>> =
  heights.flatMap { height ->
    (0..numExps).map {
      height.toString() to ExpressionGenerator(DoublePrecision, rand = Random(it)).randomBiTree(height)
    }.map { it.first to it.second.toGate().graph }.take(numExps).toList()
  }.unzip()


fun generateDigraphs(
  heights: IntRange = 4..7,
  degrees: IntRange = 2..5,
  numExps: Int = 50,
): Pair<List<String>, List<LabeledGraph>> =
  heights.flatMap { height ->
    degrees.flatMap { degree ->
      (0..numExps).map { i ->
        val graph: LabeledGraph = (1..height)
          .fold(LabeledGraph(LGVertex("0"))) { it, _ -> it.attachRandomT(degree) }
        "$degree / $height" to graph
      }
    }
  }.unzip()