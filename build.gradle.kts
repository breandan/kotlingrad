import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  application
  kotlin("jvm") version "1.3.50"
  `maven-publish`
  id("io.freefair.github.package-registry-maven-publish") version "4.1.1"
  id("org.openjfx.javafxplugin") version "0.0.8"
}

group = "edu.umontreal"
version = "0.2.2"

repositories {
  jcenter()
  maven("https://dl.bintray.com/mipt-npm/scientifik")
}

val kotlinVersion = "1.3.50"

tasks {
  listOf("Plot2D", "Plot3D", "HelloKotlinGrad", "physics.DoublePendulum", "physics.SinglePendulum")
    .forEach {
      register(it, JavaExec::class) {
        main = "edu.umontreal.kotlingrad.samples.${it}Kt"
        classpath = sourceSets["main"].runtimeClasspath
      }
    }

  withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-XXLanguage:+NewInference"
  }

  val test by getting(Test::class) {
    useJUnitPlatform()
  }
}

repositories {
  mavenCentral()
  maven("https://jitpack.io")
  maven("http://maven.jzy3d.org/releases")
}

val arrow_version = "0.9.1-SNAPSHOT"
dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-M1")
  testCompile("io.kotlintest:kotlintest-runner-junit5:3.4.0")
  api("org.jzy3d:jzy3d-api:1.0.2")
  api("org.knowm.xchart:xchart:3.5.4")
  api("ch.obermuhlner:big-math:2.1.0")
  api("scientifik:kmath-core:0.1.3")
  implementation("org.openjfx:javafx-swing:11")
  implementation("org.openjfx:javafx:11")
//  implementation("com.ionspin.kotlin:bignum:0.1.0")
}

val sourcesJar by tasks.creating(Jar::class) {
  dependsOn(tasks.classes)
  from(sourceSets.main.get().allSource)
  with(tasks["jar"] as CopySpec)
  exclude("**.png", "LICENSE.txt")
}

github {
  slug
  username.set(project.properties["githubUsername"]?.toString())
  token.set(project.properties["githubToken"]?.toString())
  tag.set(project.version.toString())
}

publishing {
  publications.create<MavenPublication>("default") {
    artifact(sourcesJar)
    pom {
      description.set("Kotlin∇: Differentiable Functional Programming with Algebraic Data Types")
      name.set("Kotlin∇")
      url.set("https://github.com/breandan/kotlingrad")
      licenses {
        license {
          name.set("The Apache Software License, Version 2.0")
          url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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
  repositories {
    maven("${project.rootDir}/releases")
  }
}

javafx {
  modules("javafx.controls")
}