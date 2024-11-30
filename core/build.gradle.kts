import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.net.URL

plugins {
  signing
  `maven-publish`
  id("shipshape")
  idea
  kotlin("multiplatform")
//  kotlin("jupyter.api") version "0.11.0-225"
  id("com.xcporter.metaview") version "0.0.6"
  id("org.jetbrains.dokka")
}

// Stub secrets to let the project sync and build without the publication values set up
ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null

val keyId = providers.gradleProperty("signing.gnupg.keyId")
val password = providers.gradleProperty("signing.gnupg.password")
val secretKey = providers.gradleProperty("signing.gnupg.key")
val sonatypeApiUser = providers.gradleProperty("sonatypeApiUser")
val sonatypeApiKey = providers.gradleProperty("sonatypeApiKey")
if (keyId.isPresent && password.isPresent && secretKey.isPresent) {
  ext["signing.keyId"] = keyId
  ext["signing.password"] = password
  ext["signing.key"] = secretKey
  ext["ossrhUsername"] = sonatypeApiUser
  ext["ossrhPassword"] = sonatypeApiKey
}

val generatedSourcesPath = file("src/commonMain/gen")

shipshape {
  maxVal = 12
  generatePseudoConstructors = true
  outputDir = "${generatedSourcesPath.path}/ai/hypergraph/kotlingrad"
}

idea.module { generatedSourceDirs.add(generatedSourcesPath) }

generateUml {
  projectDir.resolve("src/commonMain/kotlin/ai/hypergraph/kotlingrad/api/")
    .listFiles()!!.forEach {
      classTree {
        target = it
        outputFile = it.nameWithoutExtension + ".md"
      }
    }
}

tasks.withType<KotlinCompile> { dependsOn("genShapes") }

kotlin {
  jvm {
    tasks {
//      processJupyterApiResources {
//        libraryProducers = listOf("ai.hypergraph.kotlingrad.notebook.Integration")
//      }

      named<Test>("jvmTest") {
        minHeapSize = "1024m"
        maxHeapSize = "4096m"
        useJUnitPlatform()
        testLogging {
          events = setOf(
            FAILED,
            PASSED,
            SKIPPED,
            STANDARD_OUT
          )
          exceptionFormat = FULL
          showExceptions = true
          showCauses = true
          showStackTraces = true
          showStandardStreams = true
        }
      }
    }

    val keyId = providers.gradleProperty("signing.gnupg.keyId")
    val password = providers.gradleProperty("signing.gnupg.password")
    val secretKey = providers.gradleProperty("signing.gnupg.key")

//    fun getExtraString(name: String) = ext(name).toString()

    signing {
      useGpgCmd()
      if (keyId.isPresent && password.isPresent) {
        useInMemoryPgpKeys(keyId.get(), secretKey.get(), password.get())
        sign(publishing.publications)
      } else {
        logger.info("PGP signing key not defined, skipping signing configuration")
      }
    }

    val javadocJar by tasks.registering(Jar::class) { archiveClassifier.set("javadoc") }

    /*
     * Publishing instructions:
     *
     *  (1) ./gradlew publishAllPublicationsToSonatypeRepository
     *  (2) Visit https://s01.oss.sonatype.org/index.html#stagingRepositories
     *  (3) Close and check content tab.
     *  (4) Release.
     *
     * Adapted from: https://dev.to/kotlin/how-to-build-and-publish-a-kotlin-multiplatform-library-going-public-4a8k
     */

    publishing.publications.withType<MavenPublication> {
      artifact(javadocJar.get())

      pom {
        name.set("Kotlin∇")
        description.set("Differentiable Functional Programming with Algebraic Data Types")
        url.set("https://github.com/breandan/kotlingrad")
        licenses {
          license {
            name.set("The Apache Software License, Version 1.0")
            url.set("http://www.apache.org/licenses/LICENSE-3.0.txt")
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
  }

  sourceSets {
    val commonMain by getting {
      // TODO: Maybe move this into the plugin somehow?
      kotlin.srcDir(generatedSourcesPath)
      dependencies {
        implementation(kotlin("stdlib-common"))
        implementation(kotlin("reflect:2.1.0"))
        api("ai.hypergraph:kaliningraph:0.2.1")
      }
    }

    val jvmMain by getting {
      dependencies {
        implementation(kotlin("bom"))
        implementation(kotlin("stdlib"))

        implementation("org.graalvm.js:js:24.1.1")
        implementation("guru.nidi:graphviz-kotlin:0.18.1")

        implementation(kotlin("reflect:2.1.0"))
      }
    }

    val jvmTest by getting {
      dependencies {
        implementation("org.jetbrains.bio:viktor:1.2.0")

        // Property-based testing

        val ejmlVersion = "0.43.1"
        implementation("org.ejml:ejml-kotlin:$ejmlVersion")
        implementation("org.ejml:ejml-all:$ejmlVersion")

        val kotestVersion = "5.9.1"
        implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
        implementation("io.kotest:kotest-assertions-core:$kotestVersion")
        implementation("io.kotest:kotest-property:$kotestVersion")
        implementation("org.junit.jupiter:junit-jupiter:5.9.1")

        // Symbolic fuzzing interpreter
        implementation(kotlin("scripting-jsr223"))
//        implementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.9")
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
      }
    }
  }
}

tasks.withType<DokkaTaskPartial> {
  dokkaSourceSets.configureEach {
    jdkVersion = 17

    sourceLink {
      localDirectory = projectDir.resolve("src")
      remoteUrl = URL("https://github.com/breandan/kotlingrad/tree/master/core/src")
      remoteLineSuffix = "#L"
    }
  }
}
