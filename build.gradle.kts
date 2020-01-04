import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `maven-publish`
  kotlin("jvm") version "1.3.61"
  id("org.openjfx.javafxplugin") version "0.0.8"
}

val kotlinVersion = "1.3.61"
group = "edu.umontreal"
version = "0.2.4"

repositories {
  jcenter()
  mavenCentral()
  maven("https://jitpack.io")
  maven("http://maven.jzy3d.org/releases")
  maven("https://dl.bintray.com/mipt-npm/scientifik")
  maven("https://jetbrains.bintray.com/lets-plot-maven")
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation(kotlin("stdlib-jdk8"))
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
  testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
  implementation("org.jzy3d:jzy3d-api:1.0.2")
  implementation("org.knowm.xchart:xchart:3.6.0")
  implementation("ch.obermuhlner:big-math:2.3.0")
//  implementation("scientifik:kmath-core:0.1.3")
  implementation("org.openjfx:javafx-swing:13")
  implementation("org.openjfx:javafx:13")
  implementation("guru.nidi:graphviz-kotlin:0.12.1")
  implementation("org.jetbrains.lets-plot:lets-plot-jfx:1.0.1-SNAPSHOT")
  implementation("org.jetbrains.lets-plot:lets-plot-kotlin-api:0.0.8-SNAPSHOT")
//  implementation("com.ionspin.kotlin:bignum:0.1.0")

  testCompile("org.jetbrains.kotlin:kotlin-script-runtime:$kotlinVersion")
  testCompile("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
  testCompile("org.jetbrains.kotlin:kotlin-script-util:$kotlinVersion")
  testCompile("net.java.dev.jna:jna:4.2.2")
  testRuntime("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:$kotlinVersion")
}

javafx {
  modules("javafx.controls")
}

tasks {
  listOf("Plot2D", "Plot3D", "HelloKotlinGrad", "physics.DoublePendulum", "physics.SinglePendulum", "VariableCapture",
          "ToyExample", "ToyVectorExample", "ToyMatrixExample")
    .forEach { fileName ->
      register(fileName, JavaExec::class) {
        main = "edu.umontreal.kotlingrad.samples.${fileName}Kt"
        classpath = sourceSets["main"].runtimeClasspath
      }
    }

  withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-XXLanguage:+NewInference"
  }

  test {
    useJUnitPlatform()
  }
}

val fatJar by tasks.creating(Jar::class) {
  archiveBaseName.set("${project.name}-fat")
  manifest {
    attributes["Implementation-Title"] = "kotlingrad"
    attributes["Implementation-Version"] = archiveVersion
    attributes["Main-Class"] = "edu.umontreal.kotlingrad.samples.Plot2DKt"
  }
  from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
  with(tasks.jar.get() as CopySpec)
  exclude("**.png")
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
    maven {
      name = "GitHubPackages"
      setUrl("https://maven.pkg.github.com/breandan/kotlingrad")
      credentials {
        username = project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
        password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_API_KEY")
      }
    }
  }
  publications {
    register("gpr", MavenPublication::class) {
      from(components["java"])
    }
  }
}