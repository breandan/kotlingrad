plugins {
  `java-gradle-plugin`
  kotlin("jvm") version "1.8.20-Beta"
  id("com.gradle.plugin-publish") version "0.12.0"
}

pluginBundle {
  website = "https://github.com/breandan/kotlingrad"
  vcsUrl = "https://github.com/breandan/kotlingrad"
  description = "A shape-safe code generator for Kotlin."
  tags = listOf("uri", "types", "codegen", "kotlin")

  mavenCoordinates {
    groupId = "ai.hypergraph"
    artifactId = "shipshape"
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(kotlin("compiler-embeddable"))
}

gradlePlugin.plugins.register("shipshape") {
  id = "shipshape"
  implementationClass = "ai.hypergraph.shipshape.Shipshape"
}