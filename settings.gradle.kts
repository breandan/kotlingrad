//import de.fayard.dependencies.bootstrapRefreshVersionsAndDependencies

include("core", "samples")
includeBuild("kaliningraph") {
  dependencySubstitution {
    substitute(module("com.github.breandan:kaliningraph")).with(project(":"))
  }
}

pluginManagement.repositories {
  mavenCentral()
  gradlePluginPortal()
}

//buildscript {
//    repositories { gradlePluginPortal() }
//    dependencies.classpath("de.fayard:dependencies:0.5.8")
//}
//
//bootstrapRefreshVersionsAndDependencies()
