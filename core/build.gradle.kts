import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
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
  val kmathVersion by extra { "1.4.0" }
  implementation("scientifik:kmath-core:$kmathVersion")
  implementation("scientifik:kmath-ast:$kmathVersion")
  implementation("scientifik:kmath-prob:$kmathVersion")
//  implementation("com.ionspin.kotlin:bignum:0.1.0")
//  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:_")
  implementation("org.jetbrains.bio:viktor:_")
  implementation("com.github.breandan:kaliningraph:_")

  // Property-based testing
  testImplementation("io.kotlintest:kotlintest-runner-junit5:_")
  testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")

  // Symbolic fuzzing interpreter
  testImplementation("org.jetbrains.kotlin:kotlin-scripting-jsr223-embeddable:_")
  // Graphical libraries
  implementation("guru.nidi:graphviz-kotlin:_")
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