import org.gradle.api.JavaVersion.VERSION_11
import org.jetbrains.dokka.gradle.*
import java.net.URL

plugins {
  idea
  id("com.github.ben-manes.versions") version "0.38.0"
  id("org.jetbrains.dokka") version "1.4.32"
  kotlin("jvm") version "1.5.20-M1
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
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib"))
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
      jdkVersion.set(11)

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
        matchingRegex.set("edu.umontreal.kotlingrad.*")
        suppress.set(false)
      }

      externalDocumentationLink("https://ejml.org/javadoc/")
    }
  }
}
