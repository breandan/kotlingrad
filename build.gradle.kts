import io.github.gradlenexus.publishplugin.NexusPublishExtension
import org.gradle.api.JavaVersion.VERSION_15
import org.jetbrains.dokka.gradle.*
import java.net.URL

plugins {
  idea
  signing
  `maven-publish`
  id("com.github.ben-manes.versions") version "0.39.0"
  // https://github.com/Kotlin/dokka/issues/2024
  // id("org.jetbrains.dokka") version "1.5.0"
  id("org.jetbrains.dokka") version "1.4.32"
  kotlin("jvm") version "1.6.20-dev-1357"
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

java.toolchain {
  languageVersion.set(JavaLanguageVersion.of(15))
  vendor.set(JvmVendorSpec.ADOPTOPENJDK)
  implementation.set(JvmImplementation.J9)
}

allprojects {
  repositories {
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://maven.jzy3d.org/releases")
    maven("https://packages.jetbrains.team/maven/p/astminer/astminer")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
  }

  group = "ai.hypergraph"
  version = "0.4.6"

  apply(plugin = "org.jetbrains.kotlin.jvm")

  dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib"))
    compileOnly("org.jetbrains:annotations:22.0.0")
  }

  tasks {
    compileKotlin {
      kotlinOptions {
        jvmTarget = VERSION_15.toString()
      }
    }

    compileTestKotlin {
      kotlinOptions {
        jvmTarget = VERSION_15.toString()
      }
    }
  }
}

subprojects {
  apply<DokkaPlugin>()

  tasks.withType<DokkaTaskPartial> {
    dokkaSourceSets.configureEach {
      jdkVersion.set(15)

      sourceLink {
        localDirectory.set(file("core/src/main/kotlin"))
        remoteUrl.set(URL("https://github.com/breandan/kotlingrad/blob/master/core/src/main/kotlin"))
        remoteLineSuffix.set("#L")
      }

      sourceLink {
        localDirectory.set(file("samples/src/main/kotlin"))
        remoteUrl.set(URL("https://github.com/breandan/kotlingrad/blob/master/samples/src/main/kotlin"))
        remoteLineSuffix.set("#L")
      }

      perPackageOption {
        matchingRegex.set("(.*?)")
        suppress.set(true)
      }

      perPackageOption {
        matchingRegex.set("ai.hypergraph.kotlingrad.*")
        suppress.set(false)
      }

      externalDocumentationLink("https://github.com/breandan/kotlingrad")
    }
  }
}