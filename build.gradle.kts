import org.gradle.api.JavaVersion.VERSION_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  idea
  id("com.github.ben-manes.versions") version "0.38.0"
  kotlin("jvm") version "1.5.0-M2"
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
    withType<KotlinCompile> {
      kotlinOptions {
        jvmTarget = VERSION_1_8.toString()
        // Remove pending: https://youtrack.jetbrains.com/issue/KT-36853
//        freeCompilerArgs += "-Xdisable-phases=Tailrec"
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