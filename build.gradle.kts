import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  application
  kotlin("jvm") version "1.3.41"
}

group = "edu.umontreal"
version = "0.1"
repositories.jcenter()

val kotlinVersion = "1.3.41"

tasks {
  register("plot", JavaExec::class) {
    main = "edu.umontreal.kotlingrad.samples.Plot2DKt"
    classpath = sourceSets["main"].runtimeClasspath
    description = "Generates 2D plots"
  }

  register("plot3D", JavaExec::class) {
    main = "edu.umontreal.kotlingrad.samples.Plot3DKt"
    classpath = sourceSets["main"].runtimeClasspath
    description = "Generates 3D plots"
  }

  register("demo", JavaExec::class) {
    main = "edu.umontreal.kotlingrad.samples.HelloKotlinGradKt"
    classpath = sourceSets["main"].runtimeClasspath
    description = "Runs demo script"
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

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-M1")
  testCompile("io.kotlintest:kotlintest-runner-junit5:3.3.3")
  compile("org.jzy3d:jzy3d-api:1.0.2")
  compile("org.knowm.xchart:xchart:3.5.4")
  compile("ch.obermuhlner:big-math:2.1.0")
//  implementation("com.ionspin.kotlin:bignum:0.1.0")
}