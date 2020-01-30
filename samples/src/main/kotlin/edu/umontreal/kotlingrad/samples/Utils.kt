package edu.umontreal.kotlingrad.samples

import java.io.File

val resourcesPath =
  File(File("").absolutePath)
    .walk(FileWalkDirection.TOP_DOWN)
    .first { it.name == "samples" }.absolutePath + "/src/main/resources"

fun File.viewInBrowser() = ProcessBuilder("x-www-browser", path).start()