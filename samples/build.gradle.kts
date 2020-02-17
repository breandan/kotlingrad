plugins {
  idea
  application
  kotlin("jvm")
  id("org.openjfx.javafxplugin") version "0.0.8"
  id("com.palantir.graal") version "0.6.0-74-g5306dc5"
}

val entrypoint = "edu.umontreal.kotlingrad.samples.HelloKotlingradKt"

application.mainClassName = entrypoint
graal {
  mainClass(entrypoint)
  outputName("hello-kotlingrad")
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

  // Lets-Plot dependencies: https://github.com/JetBrains/lets-plot-kotlin/issues/5
  implementation("org.jetbrains.lets-plot:lets-plot-jfx:_")
  implementation("org.jetbrains.lets-plot:lets-plot-common:_")
  implementation("org.jetbrains.lets-plot:lets-plot-kotlin-api:_")
  implementation("org.jetbrains.lets-plot:kotlin-frontend-api:_")

  implementation("org.nield:kotlin-statistics:_")
}

javafx.modules("javafx.controls", "javafx.swing")

tasks {
  compileKotlin {
    kotlinOptions {
      jvmTarget = JavaVersion.VERSION_1_8.toString()
      languageVersion = "1.3"
    }
  }

  listOf(
    "Plot2D", "Plot3D", "HelloKotlingrad", "physics.DoublePendulum",
    "physics.SinglePendulum", "VariableCapture", "LetsPlot", "ScalarDemo",
    "VectorDemo", "MatrixDemo", "MLP", "LinearRegression", "PolynomialRegression", "ReadSeff"
  ).forEach { fileName ->
    register(fileName, JavaExec::class) {
      main = "edu.umontreal.kotlingrad.samples.${fileName}Kt"
      classpath = sourceSets["main"].runtimeClasspath
    }
  }
}