plugins {
  application
  kotlin("jvm") version "1.3.0"
}

application {
  mainClassName = "co.ndan.kotlingrad.math.samples.HelloKotlinGradKt"
}

group = "co.ndan"
version = "0.1"

repositories {
  jcenter()
  maven("https://dl.bintray.com/spekframework/spek-dev")
}

val kotlinVersion = "1.3.11"
val junitPlatformVersion = "1.1.0"
val spekVersion = "2.0.0-rc.1"

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")

  testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.0-alpha.1") {
    exclude(group = "org.jetbrains.kotlin")
  }
  testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.0-alpha.1") {
    exclude(group = "org.junit.platform")
    exclude(group = "org.jetbrains.kotlin")
  }

  testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
}

tasks.withType<Test> {
  useJUnitPlatform {
    includeEngines("spek2")
  }
}