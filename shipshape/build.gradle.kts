plugins {
  `java-gradle-plugin`
  kotlin("jvm") version "1.5.30-dev-2019"
  id("com.gradle.plugin-publish") version "0.12.0"
}

pluginBundle {
  website = "https://github.com/breandan/kotlingrad"
  vcsUrl = "https://github.com/breandan/kotlingrad"
  description = "A shape-safe code generator for Kotlin."
  tags = listOf("uri", "types", "codegen", "kotlin")

  mavenCoordinates {
    groupId = "edu.mcgill"
    artifactId = "shipshape"
  }
}

repositories {
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
}

dependencies {
  implementation(kotlin("compiler-embeddable"))
}

gradlePlugin.plugins.register("shipshape") {
  id = "shipshape"
  implementationClass = "edu.mcgill.shipshape.Shipshape"
}