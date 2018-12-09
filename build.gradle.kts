plugins {
  application
  kotlin("jvm") version "1.3.0"
}

application.mainClassName = "co.ndan.kotlingrad.math.samples.HelloKotlinGradKt"

group = "co.ndan"
version = "0.1"
repositories.jcenter()

val kotlinVersion = "1.3.11"

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
  testCompile("io.kotlintest:kotlintest-runner-junit5:3.1.10")
}