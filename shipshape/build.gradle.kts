plugins {
  `java-gradle-plugin`
  kotlin("jvm") version "2.0.0"
  id("com.gradle.plugin-publish") version "1.2.1"
}

gradlePlugin {
  website = "https://github.com/breandan/kotlingrad"
  vcsUrl = "https://github.com/breandan/kotlingrad"
  description = "A shape-safe code generator for Kotlin."
//  tags = listOf("uri", "types", "codegen", "kotlin")

//  mavenCoordinates {
//    groupId = "ai.hypergraph"
//    artifactId = "shipshape"
//  }
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