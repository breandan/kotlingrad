plugins {
  application
  kotlin("jvm") version "1.3.11"
}

group = "edu.umontreal"
version = "0.1"
repositories.jcenter()

val kotlinVersion = "1.3.11"

tasks {
  register("plot", JavaExec::class) {
    main = "edu.umontreal.kotlingrad.samples.TestPlotKt"
    classpath = sourceSets["main"].runtimeClasspath
    description = "Generates plots"
  }

  register("demo", JavaExec::class) {
    main = "edu.umontreal.kotlingrad.samples.HelloKotlinGradKt"
    classpath = sourceSets["main"].runtimeClasspath
    description = "Runs demo script"
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
  testCompile("io.kotlintest:kotlintest-runner-junit5:3.1.11")
  compile("com.github.holgerbrandl:kravis:-SNAPSHOT")
  compile("org.jzy3d:jzy3d-api:1.0.2")
  compile("ch.obermuhlner:kotlin-big-math:0.0.1")
}