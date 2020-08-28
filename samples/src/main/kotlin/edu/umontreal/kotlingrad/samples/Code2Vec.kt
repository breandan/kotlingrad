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
import jetbrains.datalore.base.values.Color.Companion.BLACK
import jetbrains.datalore.plot.PlotSvgExport
import jetbrains.letsPlot.*
import jetbrains.letsPlot.geom.*
import jetbrains.letsPlot.intern.toSpec
import jetbrains.letsPlot.label.ggtitle
import java.io.File
import kotlin.random.Random

fun main() {
  println("Generating ASTS")
//  val (files, graphs) = generateASTs()
  val (files, graphs) = mineASTs()

  for (i in 0..2)
    File.createTempFile("randomGraph$i", ".svg")
      .apply { writeText(graphs.random().html()); println(this.path) }

  val X = graphs.map { it.gnn().nz_values }.toList().toTypedArray()

  val maxDim = X.map { it.size }.maxOrNull()!!

  println("Max graph size is $maxDim")

  val padded = X.map { cur ->
    DoubleArray(maxDim) { if (it < cur.size) cur[it] else 0.0 }
  }.toTypedArray()

  val embeddings = embedGraph(padded)
  val clusters = plot(graphs, embeddings, files)

  File.createTempFile("clusters", ".svg")
    .apply { writeText(clusters) }
    .show()
}

private fun plot(graphs: List<LabeledGraph>,
                 embeddings: Array<out DoubleArray>,
                 names: List<String>): String {
  val data = mapOf(
    "names" to names,
    "sizes" to graphs.map { (it.size / 10).toString() },
    "x" to embeddings.map { it[0] },
    "y" to embeddings.map { it[1] }
  )
  var plot = lets_plot(data) { x = "x"; y = "y"; color="sizes" } +
    ggsize(300, 250) + geom_point(shape = "names", size = 6) +
    ggtitle("Graph Types by Structural Similarity")
  plot = names.foldIndexed(plot) { i, plt, f -> plt +
    geom_text(x = embeddings[i][0] + 5, y = embeddings[i][1] + 5, label = f, color= BLACK)
  }
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
  tSne.tsne(TSneUtils.buildConfig(X, outputDims, X.size - 1, perplexity, 1000))

fun mineASTs(
  dataDir: String = {}.javaClass.getResource("/datasets/python").path,
  parser: Parser<SimpleNode> = PythonParser()
): Pair<List<String>, List<LabeledGraph>> =
  File(dataDir).walk().filter { it.extension == "py" }
    .map { parser.parse(it.inputStream())!!.apply { setToken(it.nameWithoutExtension) } }
    .map { it.getToken() to it.toKGraph() }
    .toList().unzip()

fun generateASTs(
  heights: IntRange = 2..5, numExps: Int = 50,
//  generator: ExpressionGenerator<DReal> = ExpressionGenerator(DoublePrecision)
): Pair<List<String>, List<ComputationGraph>> =
  heights.flatMap { height ->
    (0..numExps).map {
      height.toString() to ExpressionGenerator(DoublePrecision, rand = Random(it)).randomBiTree(height).also { println(it.toString()) }
    }.map { it.first to it.second.toGate().graph }.take(numExps).toList()
  }.unzip()