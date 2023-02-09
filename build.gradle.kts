import io.github.gradlenexus.publishplugin.NexusPublishExtension

plugins {
  idea
  kotlin("multiplatform") version "1.8.20-Beta" apply false
  id("com.github.ben-manes.versions") version "0.45.0"
  id("org.jetbrains.dokka") version "1.7.20"
  id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

val sonatypeApiUser = providers.gradleProperty("sonatypeApiUser")
val sonatypeApiKey = providers.gradleProperty("sonatypeApiKey")
if (sonatypeApiUser.isPresent && sonatypeApiKey.isPresent) {
  configure<NexusPublishExtension> {
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