import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  idea
  kotlin("jvm")
  `maven-publish`
}

//repositories {
//  maven("https://dl.bintray.com/mipt-npm/scientifik")
//}

dependencies {
  implementation(kotlin("stdlib"))
  implementation(kotlin("stdlib-jdk8"))
//  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")

  // Mathematical libraries
  implementation("ch.obermuhlner:big-math:_")
//  implementation("scientifik:kmath-core:0.1.3")
//  implementation("com.ionspin.kotlin:bignum:0.1.0")

  // Property-based testing
  testImplementation("io.kotlintest:kotlintest-runner-junit5:_")

  // Symbolic fuzzing interpreter
  testImplementation("org.jetbrains.kotlin:kotlin-scripting-jsr223-embeddable:_")

  // Graphical libraries
  implementation("guru.nidi:graphviz-kotlin:_")
}

tasks {
  val jvmTarget = JavaVersion.VERSION_1_8.toString()
  compileKotlin {
    kotlinOptions.jvmTarget = jvmTarget
    kotlinOptions.freeCompilerArgs += "-XXLanguage:+NewInference"
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = jvmTarget
  }
  test {
    minHeapSize = "1024m"
    maxHeapSize = "4096m"

    useJUnitPlatform()
    testLogging {
      events = setOf(FAILED, PASSED, SKIPPED, STANDARD_OUT, STANDARD_ERROR)
      exceptionFormat = FULL
      showExceptions = true
      showCauses = true
      showStackTraces = true
    }
  }
}

val fatJar by tasks.creating(Jar::class) {
  archiveBaseName.set("${project.name}-fat")
  manifest {
    attributes["Implementation-Title"] = "kotlingrad"
    attributes["Implementation-Version"] = archiveVersion
  }
  from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
  with(tasks.jar.get() as CopySpec)
}

publishing {
  publications.create<MavenPublication>("default") {
    artifact(fatJar)
    pom {
      description.set("Kotlin∇: Differentiable Functional Programming with Algebraic Data Types")
      name.set("Kotlin∇")
      url.set("https://github.com/breandan/kotlingrad")
      licenses {
        license {
          name.set("The Apache Software License, Version 2.0")
          url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
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
  repositories {
    maven {
      name = "GitHubPackages"
      setUrl("https://maven.pkg.github.com/breandan/kotlingrad")
      credentials {
        username = project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
        password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_API_KEY")
      }
    }
  }
  publications {
    register("gpr", MavenPublication::class) {
      from(components["java"])
    }
  }
}