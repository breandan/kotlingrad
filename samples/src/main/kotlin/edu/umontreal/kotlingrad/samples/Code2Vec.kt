package edu.umontreal.kotlingrad.samples

import com.jujutsu.tsne.barneshut.ParallelBHTsne
import com.jujutsu.utils.*
import edu.umontreal.kotlingrad.experimental.show
import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.plot.PlotSvgExport
import jetbrains.letsPlot.geom.geom_point
import jetbrains.letsPlot.ggsize
import jetbrains.letsPlot.intern.toSpec
import jetbrains.letsPlot.lets_plot
import java.io.File

fun main() {
//  val X = MatrixUtils.simpleRead2DMatrix(File("samples/src/main/resources/datasets/mnist250_X.txt"), "   ")
  val graphs = generateSequence { PolyGenerator.randomBiTree(4) }.take(100)
  val X = graphs.map { it.toGate().graph.A.nz_values }.toList().toTypedArray()

  val initialDims = X.first().size
  val perplexity = 20.0
  println(MatrixOps.doubleArrayToPrintString(X, ", ", 50, 10))
  val config = TSneUtils.buildConfig(X, 2, initialDims, perplexity, 1000)
  val Y = ParallelBHTsne().tsne(config)
  println("${Y.size}x${Y[0].size}")

  val data = mapOf("x" to Y.map { it[0] }, "y" to Y.map { it[1] })
  val plot = lets_plot(data) { x = "x"; y = "y" } + ggsize(300, 250) + geom_point(shape = 1)
  val plotSpec = plot.toSpec()
  val plotSize = DoubleVector(1000.0, 500.0)
  val svg = PlotSvgExport.buildSvgImageFromRawSpecs(plotSpec, plotSize)

  File.createTempFile("test", ".svg").apply { writeText(svg); println(this.path) }
}