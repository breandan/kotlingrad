import org.gradle.api.JavaVersion.VERSION_11

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
  }

  group = "com.github.breandan"
  version = "0.4.2"

  apply(plugin = "org.jetbrains.kotlin.jvm")

  dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib-jdk8"))
  }

  tasks {
    compileTestKotlin {
      kotlinOptions {
        jvmTarget = VERSION_11.toString()
      }
    }
  }
}