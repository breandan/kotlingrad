plugins {
  application
  id("com.palantir.graal") version "0.12.0"
  kotlin("jvm")
}

val entrypoint = "ai.hypergraph.kotlingrad.samples.HelloKotlingradKt"

application.mainClass.set(entrypoint)

graal {
  mainClass(entrypoint)
  outputName("hello-kotlingrad")
}

dependencies {
  implementation(kotlin("stdlib"))
  compileOnly("org.jetbrains:annotations:26.0.1")
  implementation(project(":kotlingrad"))

  implementation("org.graalvm.js:js:24.1.1")
  implementation("guru.nidi:graphviz-kotlin:0.18.1")
  // Graphical libraries
  implementation("org.jzy3d:jzy3d-api:1.0.3")

  implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.9.3")
  implementation("org.jetbrains.lets-plot:platf-awt-jvm:4.5.2")

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
