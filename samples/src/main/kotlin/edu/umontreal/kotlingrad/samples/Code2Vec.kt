package edu.umontreal.kotlingrad.samples

import astminer.common.model.Node
import astminer.common.model.Parser
import astminer.parse.antlr.*
import astminer.parse.antlr.python.PythonParser
import com.jujutsu.tsne.TSne
import com.jujutsu.tsne.barneshut.ParallelBHTsne
import com.jujutsu.utils.TSneUtils
import edu.mcgill.kaliningraph.*
import edu.mcgill.kaliningraph.circuits.ComputationGraph
import edu.umontreal.kotlingrad.api.*
import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.plot.PlotSvgExport
import jetbrains.letsPlot.*
import jetbrains.letsPlot.geom.*
import jetbrains.letsPlot.intern.toSpec
import jetbrains.letsPlot.label.ggtitle
import org.ejml.kotlin.*
import java.io.File
import kotlin.random.Random

fun main() {
//  val (labels, graphs) = generateASTs()
  val (labels, graphs) = mineASTs()
//  val (labels, graphs) = generateDigraphs()

  val rounds = listOf(100)//, 2, 5, 10)
  for (round in rounds) {
    val X = graphs.map { it.gnn(t = round) }.permute()
    val embeddings = embedGraph(X)
    val clusters = plot(round, embeddings, labels)

    File.createTempFile("clusters", ".html")
      .apply { writeText("<html>$clusters</html>") }.show()
  }
}

fun Node.toKGraph() =
  LabeledGraph {
    closure(
      toVisit = setOf(this@toKGraph),
      successors = { flatMap { setOfNotNull(it.parent) + it.children }.toSet() }
    ).forEach { parent ->
      children.forEach { child ->
        LGVertex(parent.token) - LGVertex(child.token)
        LGVertex(child.token) - LGVertex(parent.token)
      }
    }
  }

// Pad length to a common vector length for TNSE
private fun List<SpsMat>.permute(): Array<DoubleArray> =
  maxOf { it.nz_length }.let { maxDim ->
    map { cur ->
      val data = cur.toFDRM().getData()
      val padded = DoubleArray(maxDim) { if (it < data.size) data[it] else 0.0 }
      padded.apply { shuffle(Random.Default) }.take(20).toDoubleArray()//.also { println(it.map { 0.0 < it }.joinToString()) }
    }.toTypedArray()
  }

private fun plot(
  messagePassingRounds: Int,
  embeddings: Array<out DoubleArray>,
  labels: List<String>
): String {
  val data = mapOf(
    "labels" to labels,
    "x" to embeddings.map { it[0] },
    "y" to embeddings.map { it[1] }
  )
  val plot = letsPlot(data) { x = "x"; y = "y"; color = "labels" } +
    ggsize(300, 250) + geomPoint(size = 6) +
    ggtitle("Graph Types by Structural Similarity (t = $messagePassingRounds)") +
    theme().axisLineBlank().axisTitleBlank().axisTicksBlank().axisTextBlank()
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
  parser: Parser<AntlrNode> = PythonParser()
): Pair<List<String>, List<LabeledGraph>> =
  File(dataDir).walk().filter { it.extension == "py" }
    .map { parser.parseFile(it).root.apply { token = it.nameWithoutExtension } }
    .map { it.toKGraph().let { kgraph -> (kgraph.size / 10).toString() to kgraph } }
    .toList().unzip()

fun generateASTs(
  heights: IntRange = 2..5, numExps: Int = 50,
//  generator: ExpressionGenerator<DReal> = ExpressionGenerator(DoublePrecision)
): Pair<List<String>, List<ComputationGraph>> =
  heights.flatMap { height ->
    (0..numExps).map {
      height.toString() to ExpressionGenerator<DReal>().randomBiTree(height)
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