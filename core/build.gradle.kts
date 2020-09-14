plugins {
  `maven-publish`
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation(kotlin("stdlib-jdk8"))
//  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")
  api("com.github.breandan:kaliningraph")

  // Mathematical libraries
  implementation("ch.obermuhlner:big-math:2.3.0")
  implementation("org.jetbrains.bio:viktor:1.0.1")

  val kmathVersion by extra { "0.1.4" }
  testImplementation("kscience.kmath:kmath-core:$kmathVersion")
  testImplementation("kscience.kmath:kmath-ast:$kmathVersion")
  testImplementation("kscience.kmath:kmath-prob:$kmathVersion")
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
    dependsOn("publishToMavenLocal")
    val installPath = findProperty("ath") ?: installPathLocal
    doFirst { mkdir(installPath) }
    from(file("kotlingrad.json"))
    into(installPath)
    doLast { logger.info("Kotlin∇ notebook was installed in: $installPath") }
  }
}

val fatJar by tasks.creating(Jar::class) {
  archiveBaseName.set("${project.name}-fat")
  manifest {
    attributes["Implementation-Title"] = "kotlingrad"
    attributes["Implementation-Version"] = archiveVersion
  }
  setExcludes(listOf("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA"))
  from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
  with(tasks.jar.get() as CopySpec)
}

publishing {
  publications.create<MavenPublication>("default") {
    artifact(fatJar)
    pom {
      description.set("Kotlin∇: Differentiable Functional Programming with Algebraic Data Types")
      name.set("Kotlin∇")
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