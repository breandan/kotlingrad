import io.github.gradlenexus.publishplugin.NexusPublishExtension

plugins {
  idea
  kotlin("multiplatform") version "2.1.0" apply false
  id("com.github.ben-manes.versions") version "0.51.0"
  id("org.jetbrains.dokka") version "2.0.0"
  id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

val sonatypeApiUser = providers.gradleProperty("sonatypeApiUser")
val sonatypeApiKey = providers.gradleProperty("sonatypeApiKey")
if (sonatypeApiUser.isPresent && sonatypeApiKey.isPresent) {
  nexusPublishing {
    repositories {
      sonatype {
        nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
        snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        username.set(sonatypeApiUser)
        password.set(sonatypeApiKey)
        useStaging.set(true)
      }
    }
  }
} else {
  logger.info("Sonatype API key not defined, skipping configuration of Maven Central publishing repository")
}

idea.module {
  excludeDirs.add(file("latex"))
  isDownloadJavadoc = true
  isDownloadSources = true
}

allprojects {
  repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.jzy3d.org/releases")
  }

  group = "ai.hypergraph"
  version = "0.4.7"
}

val documentedSubprojects = setOf(
  "kotlingrad" // :core
)

subprojects {
  if (name in documentedSubprojects) {
    apply(plugin = "org.jetbrains.dokka")
  }
}