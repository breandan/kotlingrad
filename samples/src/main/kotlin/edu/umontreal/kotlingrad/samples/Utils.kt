package edu.umontreal.kotlingrad.samples

import edu.mcgill.kaliningraph.browserCmd
import java.io.File

val resourcesPath =
  File(File("").absolutePath)
    .walk(FileWalkDirection.TOP_DOWN)
    .first { it.name == "samples" }.absolutePath + "/src/main/resources"

fun File.viewInBrowser() = ProcessBuilder(browserCmd, path).start()