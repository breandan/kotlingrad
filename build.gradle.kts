plugins {
  idea
  kotlin("jvm") version "1.3.72"
  id("de.fayard.refreshVersions") version "0.8.7"
}

allprojects {
  group = "edu.umontreal"
  version = "0.2.5"

  repositories {
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/kotlin-dev")
    jcenter()
  }
}

idea.module {
  excludeDirs.add(file("latex"))
  isDownloadJavadoc = true
  isDownloadSources = true
}