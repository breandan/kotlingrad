include("core", "samples")
includeBuild("kaliningraph") {
  dependencySubstitution {
    substitute(module("com.github.breandan:kaliningraph")).with(project(":"))
  }
}

project(":core").name = "kotlingrad"

pluginManagement.repositories {
  mavenCentral()
  gradlePluginPortal()
  maven ("https://dl.bintray.com/kotlin/kotlin-eap")
}