plugins {
  application
  id("com.palantir.graal") version "0.10.0"
  kotlin("jvm") version "1.6.20-RC2"
}

val entrypoint = "ai.hypergraph.kotlingrad.samples.HelloKotlingradKt"

application.mainClass.set(entrypoint)

graal {
  mainClass(entrypoint)
  outputName("hello-kotlingrad")
}

dependencies {
  implementation(kotlin("stdlib"))
  compileOnly("org.jetbrains:annotations:23.0.0")
  implementation(project(":kotlingrad"))

  implementation("org.graalvm.js:js:21.3.0")
  implementation("guru.nidi:graphviz-kotlin:0.18.1")
  // Graphical libraries
  implementation("org.jzy3d:jzy3d-api:1.0.3")

  implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:3.1.1")

  implementation("org.nield:kotlin-statistics:1.2.1")
}

tasks {
  listOf(
    "HelloKotlingrad", "Plot2D", "Plot3D", "VisualizeDFG", "VariableCapture",
    "LetsPlot", "ScalarDemo", "VectorDemo", "MatrixDemo",
    "MLP", "LinearRegression", "PolynomialRegression",
    "PolynomialAttack", "ReadSeff", "Arithmetic"
  ).forEach { fileName ->
    register(fileName, JavaExec::class) {
      mainClass.set("ai.hypergraph.kotlingrad.samples.${fileName}Kt")
      classpath = sourceSets["main"].runtimeClasspath
    }
  }

/*
If overwriting an older version, it is necessary to first run:

rm -rf ~/.m2/repository/ai/hypergraph/kaliningraph \
       ~/.ivy2/cache/ai.hypergraph/kaliningraph

https://github.com/Kotlin/kotlin-jupyter/issues/121

To deploy to Maven Local and start the notebook, run:

./gradlew [build publishToMavenLocal] jupyterRun -x test
*/

  val jupyterRun by creating(Exec::class) {
    commandLine("jupyter", "notebook", "--notebook-dir=notebooks")
  }

  test {
    dependsOn(
      "HelloKotlingrad", "ScalarDemo", "MatrixDemo", "VectorDemo",
      "LinearRegression", "MLP", "VariableCapture"
    )
  }
}
