import io.github.gradlenexus.publishplugin.NexusPublishExtension
import org.gradle.api.JavaVersion.VERSION_15
import org.jetbrains.dokka.gradle.*
import java.net.URL

plugins {
  idea
  id("com.github.ben-manes.versions") version "0.41.0"
  // https://github.com/Kotlin/dokka/issues/2024
  id("org.jetbrains.dokka") version "1.6.0"
//  id("org.jetbrains.dokka") version "1.4.32"
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

subprojects {
  apply<DokkaPlugin>()

  tasks.withType<DokkaTaskPartial> {
    dokkaSourceSets.configureEach {
      jdkVersion.set(15)

      sourceLink {
        localDirectory.set(file("core/src/jvmMain/kotlin"))
        remoteUrl.set(URL("https://github.com/breandan/kotlingrad/blob/master/core/src/jvmMain/kotlin"))
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