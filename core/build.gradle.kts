plugins {
  `maven-publish`
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation(kotlin("stdlib-jdk8"))
//  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")
  api("com.github.breandan:kaliningraph:0.0.7")
  // Graphical libraries
  implementation("guru.nidi:graphviz-kotlin:0.17.0")

  // Mathematical libraries
  implementation("ch.obermuhlner:big-math:2.3.0")
  implementation("org.jetbrains.bio:viktor:1.0.1")

  val kmathVersion by extra { "0.1.4-dev-8" }
  testImplementation("scientifik:kmath-core:$kmathVersion")
  testImplementation("scientifik:kmath-ast:$kmathVersion")
  testImplementation("scientifik:kmath-prob:$kmathVersion")
//  implementation("com.ionspin.kotlin:bignum:0.1.0")
//  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:_")

  testImplementation("org.nd4j:nd4j-native-platform:1.0.0-beta7")

//  val tfVersion by extra { "-SNAPSHOT" }
//  testImplementation("com.github.tensorflow:java:$tfVersion")
//  testImplementation("com.github.tensorflow:tensorflow-core-platform:$tfVersion")
  val ejmlVersion = "0.39"
  testImplementation("org.ejml:ejml-kotlin:$ejmlVersion")
  testImplementation("org.ejml:ejml-all:$ejmlVersion")
  testImplementation("com.github.breandan:tensor:master-SNAPSHOT")

  // Property-based testing
  testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
  testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")

  // Symbolic fuzzing interpreter
  testImplementation("org.jetbrains.kotlin:kotlin-scripting-jsr223:1.4.0")
}

tasks {
  val genNotebookJSON by creating(JavaExec::class) {
    main = "edu.umontreal.kotlingrad.utils.codegen.NotebookGenKt"
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf(projectDir.path, project.version.toString())
  }

  val installPathLocal = "${System.getProperty("user.home")}/.jupyter_kotlin/libraries"

  val jupyterInstall by registering(Copy::class) {
    dependsOn(genNotebookJSON)
    val installPath = findProperty("ath") ?: installPathLocal
    doFirst { mkdir(installPath) }
    from(file("kotlingrad.json"))
    into(installPath)
    doLast { logger.info("Kotlin∇ notebook was installed in: $installPath") }
  }
}