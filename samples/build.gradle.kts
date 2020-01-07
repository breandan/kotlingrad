plugins {
  kotlin("jvm")
  id("org.openjfx.javafxplugin") version "0.0.8"
}

dependencies {
  implementation(project(":core"))
  implementation(kotlin("stdlib-jdk8"))

  // Graphical libraries
  implementation("org.openjfx:javafx-swing:13")
  implementation("org.openjfx:javafx:13")
  implementation("guru.nidi:graphviz-kotlin:0.12.1")
  implementation("org.jzy3d:jzy3d-api:1.0.2")
  implementation("org.knowm.xchart:xchart:3.6.0")

// Lets-Plot dependencies: https://github.com/JetBrains/lets-plot-kotlin/issues/5
  implementation("org.jetbrains.lets-plot:lets-plot-jfx:1.1.1-SNAPSHOT")
  implementation("org.jetbrains.lets-plot:lets-plot-common:1.1.1-SNAPSHOT")
  implementation("org.jetbrains.lets-plot:lets-plot-kotlin-api:0.0.8-SNAPSHOT")
  implementation("org.jetbrains.lets-plot:kotlin-frontend-api:0.0.8-SNAPSHOT")
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