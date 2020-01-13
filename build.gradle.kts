plugins {
  idea
  id("de.fayard.refreshVersions") version "0.8.6"
}

allprojects {
  group = "edu.umontreal"
  version = "0.2.4"

  repositories {
    mavenCentral()
  }
}

idea {
  module {
    excludeDirs.add(file("latex"))
  }
}