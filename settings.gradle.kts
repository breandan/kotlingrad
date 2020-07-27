//import de.fayard.dependencies.bootstrapRefreshVersionsAndDependencies

include("core", "samples")

pluginManagement.repositories {
  mavenCentral()
  gradlePluginPortal()
  maven ("https://dl.bintray.com/kotlin/kotlin-eap")
}

//buildscript {
//    repositories { gradlePluginPortal() }
//    dependencies.classpath("de.fayard:dependencies:0.5.8")
//}
//
//bootstrapRefreshVersionsAndDependencies()
