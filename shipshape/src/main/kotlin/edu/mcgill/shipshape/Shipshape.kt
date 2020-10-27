package edu.mcgill.shipshape

import org.gradle.api.*
import java.io.File

open class Shipshape: Plugin<Project> {
  fun generateProjectSources(path: String) {
    val header = """// This file was generated by Shipshape
    
    package edu.umontreal.kotlingrad.shapes
    
    import edu.umontreal.kotlingrad.experimental.SConst
    import edu.umontreal.kotlingrad.experimental.MConst
    import edu.umontreal.kotlingrad.experimental.VConst
    import edu.umontreal.kotlingrad.experimental.RealNumber
    """.trimIndent()

    val shapes = """
      $header
      
      ${genDim()}
      ${genVec()}
      ${genMat()}
    """.trimIndent()

    val outputDir = "$path/src/main/kotlin/gen"
    File(outputDir).mkdirs()
    return File("$outputDir/Shapes.kt")
      .also { it.createNewFile() }
      .writeText(shapes)
  }

  override fun apply(project: Project) {
    project.run {
      tasks.register("genShapes") {
        // TODO: Parameterize this path
        val projectPath = project(path).projectDir.invariantSeparatorsPath
        generateProjectSources(path)
      }
    }
  }
}