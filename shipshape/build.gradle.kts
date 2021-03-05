plugins {
  `java-gradle-plugin`
  kotlin("jvm") version "1.5.0-M1"
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
}

dependencies {
  implementation(kotlin("compiler-embeddable"))
  implementation("com.squareup:kotlinpoet:1.7.2")
}

gradlePlugin.plugins.register("shipshape") {
  id = "shipshape"
  implementationClass = "edu.mcgill.shipshape.Shipshape"
}