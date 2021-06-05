include("core", "samples")

includeBuild("shipshape")

includeBuild("kaliningraph") {
  dependencySubstitution {
    substitute(module("com.github.breandan:kaliningraph")).with(project(":"))
  }
}

project(":core").name = "kotlingrad"

pluginManagement.repositories {
  mavenCentral()
  gradlePluginPortal()
  maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
}