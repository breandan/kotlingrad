package ai.hypergraph.shipshape

import org.gradle.api.*
import java.io.File

open class Shipshape: Plugin<Project> {
  override fun apply(project: Project) {
    project.run {
      tasks.register("genShapes") {
        // TODO: Parameterize this path
        val outputDir = "$projectDir/src/jvmMain/kotlin/ai/hypergraph/kotlingrad"
        // TODO: Switch to commonMain
//        val outputDir = "$projectDir/src/commonMain/kotlin/ai/hypergraph/kotlingrad"
        try {
          File("$outputDir/shapes").mkdirs()
          File("$outputDir/shapes/Shapes.kt")
            .also { it.createNewFile() }
            .writeText(generateShapes())

          File("$outputDir/typelevel").mkdirs()
          File("$outputDir/typelevel/Variables.kt")
            .also { it.createNewFile() }
            .writeText(genTypeLevelVariables())
        } catch (e: Exception) {
          logger.error(outputDir)
          throw e
        }
      }
    }
  }
}
