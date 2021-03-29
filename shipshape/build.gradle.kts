plugins {
  `java-gradle-plugin`
  kotlin("jvm") version "1.5.0-M2"
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

repositories.mavenCentral()

dependencies {
  implementation(kotlin("compiler-embeddable"))
}

gradlePlugin.plugins.register("shipshape") {
  id = "shipshape"
  implementationClass = "edu.mcgill.shipshape.Shipshape"
}