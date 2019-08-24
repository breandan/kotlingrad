import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  application
  kotlin("jvm") version "1.3.41"
  `maven-publish`
}

group = "edu.umontreal"
version = "0.1"

repositories {
  jcenter()
  maven("https://dl.bintray.com/mipt-npm/scientifik")
//  maven("https://dl.bintray.com/arrow-kt/arrow-kt/")
//  mavenCentral()
//  maven("https://oss.jfrog.org/artifactory/oss-snapshot-local/")
}

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

val arrow_version = "0.9.1-SNAPSHOT"
dependencies {
  //  compile("io.arrow-kt:arrow-core-data:$arrow_version")
//  compile("io.arrow-kt:arrow-core-extensions:$arrow_version")
//  compile("io.arrow-kt:arrow-syntax:$arrow_version")
//  compile("io.arrow-kt:arrow-typeclasses:$arrow_version")
//  compile("io.arrow-kt:arrow-extras-data:$arrow_version")
//  compile("io.arrow-kt:arrow-extras-extensions:$arrow_version")

  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-M1")
  testCompile("io.kotlintest:kotlintest-runner-junit5:3.4.0")
  compile("org.jzy3d:jzy3d-api:1.0.2")
  compile("org.knowm.xchart:xchart:3.5.4")
  compile("ch.obermuhlner:big-math:2.1.0")
  api("scientifik:kmath-core:0.1.3")
//  implementation("com.ionspin.kotlin:bignum:0.1.0")
}

val sourcesJar by tasks.registering(Jar::class) {
  archiveClassifier.set("sources")
  from(sourceSets["main"].allSource)
}

publishing {
  publications.create<MavenPublication>("default") {
    from(components["java"])
    artifact(sourcesJar.get())
  }
  repositories.maven("${project.rootDir}/releases")
}