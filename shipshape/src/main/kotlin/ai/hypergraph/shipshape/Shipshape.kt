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

open class ShipshapeExt(var outputDir: String = "")

open class GenSources: DefaultTask() {
  @TaskAction
  fun genShapes() {
    try {
      val outputDir = project.extensions.getByType(ShipshapeExt::class.java).outputDir
      File("$outputDir/shapes").mkdirs()
      File("$outputDir/shapes/Shapes.kt")
        .also { it.createNewFile() }
        .writeText(generateShapes())

      File("$outputDir/typelevel").mkdirs()
      File("$outputDir/typelevel/Variables.kt")
        .also { it.createNewFile() }
        .writeText(genTypeLevelVariables())
    } catch (e: Exception) {
      logger.error(e.toString())
      throw e
    }
  }
}