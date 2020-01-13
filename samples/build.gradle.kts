plugins {
  kotlin("jvm") version "1.3.61"
  id("org.openjfx.javafxplugin") version "0.0.8"
}

repositories {
  maven("https://jitpack.io")
  maven("https://maven.jzy3d.org/releases")
  maven("https://jetbrains.bintray.com/lets-plot-maven")
}

dependencies {
  implementation(project(":core"))
  implementation(kotlin("stdlib-jdk8"))

  // Graphical libraries
  implementation("org.openjfx:javafx-swing:_")
  implementation("org.openjfx:javafx:_")
  implementation("guru.nidi:graphviz-kotlin:_")
  implementation("org.jzy3d:jzy3d-api:_")
  implementation("org.knowm.xchart:xchart:_")

  // Lets-Plot dependencies: https://github.com/JetBrains/lets-plot-kotlin/issues/5
  implementation("org.jetbrains.lets-plot:lets-plot-jfx:_")
  implementation("org.jetbrains.lets-plot:lets-plot-common:_")
  implementation("org.jetbrains.lets-plot:lets-plot-kotlin-api:_")
  implementation("org.jetbrains.lets-plot:kotlin-frontend-api:_")
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += "-XXLanguage:+NewInference"
  }

  compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }

  listOf("Plot2D", "Plot3D", "HelloKotlinGrad", "physics.DoublePendulum", "physics.SinglePendulum", "VariableCapture",
    "ToyExample", "ToyVectorExample", "ToyMatrixExample", "LetsPlot")
    .forEach { fileName ->
      register(fileName, JavaExec::class) {
        main = "edu.umontreal.kotlingrad.samples.${fileName}Kt"
        classpath = sourceSets["main"].runtimeClasspath
      }
    }
}

javafx {
  modules("javafx.controls", "javafx.swing")
}