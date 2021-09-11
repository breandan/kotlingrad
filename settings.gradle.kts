include("core", "samples")

includeBuild("shipshape")

project(":core").name = "kotlingrad"

pluginManagement.repositories {
  mavenCentral()
  gradlePluginPortal()
  maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
}