import org.gradle.api.JavaVersion.VERSION_11
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  idea
  id("com.github.ben-manes.versions") version "0.38.0"
  kotlin("jvm") version "1.5.0-RC"
}

idea.module {
  excludeDirs.add(file("latex"))
  isDownloadJavadoc = true
  isDownloadSources = true
}

allprojects {
  repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.jzy3d.org/releases")
    // TODO: Remove pending https://github.com/JetBrains-Research/astminer/issues/124
    maven("https://dl.bintray.com/egor-bogomolov/astminer")
    // TODO: https://github.com/JetBrains/lets-plot-kotlin/issues/55
    maven("https://jetbrains.bintray.com/lets-plot-maven")
  }

  group = "com.github.breandan"
  version = "0.4.2"

  apply(plugin = "org.jetbrains.kotlin.jvm")

  dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib-jdk8"))
  }

  tasks {
    compileKotlin {
      kotlinOptions {
        jvmTarget = VERSION_11.toString()
      }
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
      }
    }
  }
}