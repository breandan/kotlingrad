package edu.umontreal.kotlingrad.samples

import java.io.File

const val resourcesPath = "samples/src/main/resources"

fun File.viewInBrowser() = ProcessBuilder("x-www-browser", path).start()