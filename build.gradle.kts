import org.gradle.api.JavaVersion.VERSION_11
import org.jetbrains.dokka.gradle.*

plugins {
  idea
  id("com.github.ben-manes.versions") version "0.38.0"
  id("org.jetbrains.dokka") version "1.4.32"
  kotlin("jvm") version "1.5.0"
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
    maven(url = uri("https://packages.jetbrains.team/maven/p/astminer/astminer"))
  }

  group = "com.github.breandan"
  version = "0.4.5"

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

    compileTestKotlin {
      kotlinOptions {
        jvmTarget = VERSION_11.toString()
      }
    }
  }
}

subprojects {
  apply<DokkaPlugin>()

  tasks.withType<DokkaTaskPartial> {
    dokkaSourceSets.configureEach {
     externalDocumentationLink("https://ejml.org/javadoc/")
    }
  }
}
