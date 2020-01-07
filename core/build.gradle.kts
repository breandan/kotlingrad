plugins {
  kotlin("jvm")
}

val kotlinVersion = "1.3.61"
dependencies {
  implementation(kotlin("stdlib"))
  implementation(kotlin("stdlib-jdk8"))
//  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")

  // Mathematical libraries
  implementation("ch.obermuhlner:big-math:2.3.0")
//  implementation("scientifik:kmath-core:0.1.3")
//  implementation("com.ionspin.kotlin:bignum:0.1.0")

  // Property-based testing
  testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")

  // Symbolic fuzzing interpreter
  testImplementation("org.jetbrains.kotlin:kotlin-scripting-jsr223-embeddable:$kotlinVersion")

  // Graphical libraries
  implementation("guru.nidi:graphviz-kotlin:0.12.1")
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += "-XXLanguage:+NewInference"
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  test {
    useJUnitPlatform()
  }
}