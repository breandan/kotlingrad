import org.gradle.api.JavaVersion.VERSION_11
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  `maven-publish`
  id("shipshape")
  idea
  kotlin("jupyter.api") version "0.10.0-53"
}

// TODO: Maybe move this into the plugin somehow?
val generatedSourcesPath = file("src/main/kotlin/gen")
kotlin.sourceSets["main"].kotlin.srcDir(generatedSourcesPath)

idea.module {
  generatedSourceDirs.add(generatedSourcesPath)
}

dependencies {
//  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")
  api("com.github.breandan:kaliningraph:0.1.7")

  // Mathematical libraries
  implementation("com.github.JetBrains-Research:viktor:1.1.0")

  implementation(kotlin("reflect"))
  // Notebook integration test
  testImplementation(kotlin("reflect"))
  testImplementation("junit", "junit", "4.13.2")
  testImplementation(kotlin("scripting-jvm"))
  testImplementation("org.jetbrains.kotlinx:kotlin-jupyter-kernel:0.10.0-53")

  testImplementation("org.nd4j:nd4j-native-platform:1.0.0-beta7")

//  val tfVersion by extra { "-SNAPSHOT" }
//  testImplementation("com.github.tensorflow:java:$tfVersion")
//  testImplementation("com.github.tensorflow:tensorflow-core-platform:$tfVersion")
  testImplementation("com.github.breandan:tensor:master-SNAPSHOT")

  // Property-based testing

  val kotestVersion = "4.6.0"
  testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
  testImplementation("io.kotest:kotest-property:$kotestVersion")
  testImplementation("org.junit.jupiter:junit-jupiter:5.8.0-M1")

  // Symbolic fuzzing interpreter
  testImplementation(kotlin("scripting-jsr223"))
}

configure<JavaPluginConvention> {
  sourceCompatibility = VERSION_11
}

tasks {
  compileKotlin { dependsOn("genShapes") }

  val sourcesJar by registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
  }

  test {
    minHeapSize = "1024m"
    maxHeapSize = "4096m"
    useJUnitPlatform()
    testLogging {
      events = setOf(FAILED, PASSED, SKIPPED, STANDARD_OUT)
      exceptionFormat = FULL
      showExceptions = true
      showCauses = true
      showStackTraces = true
      showStandardStreams = true
    }
  }
}

publishing {
  publications.create<MavenPublication>("default") {
    from(components["java"])
    artifact(tasks["sourcesJar"])

    pom {
      description.set("Kotlin∇: Differentiable Functional Programming with Algebraic Data Types")
      name.set("Kotlin∇")
      url.set("https://github.com/breandan/kotlingrad")
      licenses {
        license {
          name.set("The Apache Software License, Version 1.0")
          url.set("http://www.apache.org/licenses/LICENSE-3.0.txt")
          distribution.set("repo")
        }
      }
      developers {
        developer {
          id.set("Breandan Considine")
          name.set("Breandan Considine")
          email.set("bre@ndan.co")
          organization.set("Université de Montréal")
        }
      }
      scm {
        url.set("https://github.com/breandan/kotlingrad")
      }
    }
  }
}