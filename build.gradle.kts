import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  application
  kotlin("jvm") version "1.3.21"
}

group = "edu.umontreal"
version = "0.1"
repositories.jcenter()

val kotlinVersion = "1.3.21"

tasks {
  register("plot", JavaExec::class) {
    main = "edu.umontreal.kotlingrad.samples.TestPlotKt"
    classpath = sourceSets["main"].runtimeClasspath
    description = "Generates 2D plots"
  }

  register("plot3D", JavaExec::class) {
    main = "edu.umontreal.kotlingrad.samples.Jzy3DemoKt"
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
  testCompile("io.kotlintest:kotlintest-runner-junit5:3.2.1")
  compile("com.github.holgerbrandl:kravis:-SNAPSHOT")
  compile("org.jzy3d:jzy3d-api:1.0.2")
  compile("ch.obermuhlner:kotlin-big-math:0.0.1")
}