package ai.hypergraph.shipshape

import org.gradle.api.*
import org.gradle.api.tasks.TaskAction
import java.io.File

open class Shipshape : Plugin<Project> {
  override fun apply(project: Project) {
    project.run {
      extensions.create("shipshape", ShipshapeExt::class.java)
      tasks.register("genShapes", GenSources::class.java)
      tasks.getByName("build") { it.dependsOn("genShapes") }
    }
  }
}

open class ShipshapeExt(
  var outputDir: String = "",
  var maxVal: Int = 10,
  var generatePseudoConstructors: Boolean = false
)

open class GenSources: DefaultTask() {
  @TaskAction
  fun genShapes() {
    try {
      val ext = project.extensions.getByType(ShipshapeExt::class.java)
      val outputDir = ext.outputDir
      maxDim = ext.maxVal
      generatePseudoConstructors = ext.generatePseudoConstructors

      File("$outputDir/shapes").mkdirs()
      File("$outputDir/shapes/Shapes.kt")
        .also { it.createNewFile() }
        .writeText(generateShapes())

      File("$outputDir/typelevel/arity").mkdirs()
      File("$outputDir/typelevel/arity/Variables.kt")
        .also { it.createNewFile() }
        .writeText(genTypeLevelVariables())

      File("$outputDir/typelevel/arithmetic").mkdirs()
      File("$outputDir/typelevel/arithmetic/Church.kt")
        .also { it.createNewFile() }
        .writeText(genChurchArithmetic())

      File("$outputDir/typelevel/array").mkdirs()
      File("$outputDir/typelevel/array/Arrays.kt")
        .also { it.createNewFile() }
        .writeText(genArrays())
    } catch (e: Exception) {
      logger.error(e.toString())
      throw e
    }
  }
}