include("core", "samples")

includeBuild("shipshape")

project(":core").name = "kotlingrad"

pluginManagement.repositories {
  mavenCentral()
  gradlePluginPortal()
}

includeBuild("kaliningraph")