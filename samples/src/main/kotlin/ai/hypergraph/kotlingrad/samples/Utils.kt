package ai.hypergraph.kotlingrad.samples

import java.io.File

val resourcesPath =
  File(File("").absolutePath)
    .walk(FileWalkDirection.TOP_DOWN)
    .first { it.name == "samples" }.absolutePath + "/src/main/resources"