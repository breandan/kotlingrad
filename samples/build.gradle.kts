plugins {
  application
  id("com.palantir.graal") version "0.9.0"
}

val entrypoint = "edu.umontreal.kotlingrad.samples.HelloKotlingradKt"

application.mainClass.set(entrypoint)

graal {
  mainClass(entrypoint)
  outputName("hello-kotlingrad")
}

dependencies {
  implementation(project(":kotlingrad"))

  // Graphical libraries
  implementation("org.jzy3d:jzy3d-api:1.0.3")
  implementation("com.github.breandan.T-SNE-Java:tsne:master-SNAPSHOT")

  implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:3.0.1")

  implementation("io.github.vovak:astminer:0.6.4")
  implementation("org.nield:kotlin-statistics:1.2.1")
}

tasks {
  listOf(
    "HelloKotlingrad", "Plot2D", "Plot3D", "VisualizeDFG", "VariableCapture",
    "LetsPlot", "ScalarDemo", "VectorDemo", "MatrixDemo",
    "MLP", "LinearRegression", "PolynomialRegression",
    "PolynomialAttack", "ReadSeff", "Code2Vec"
  ).forEach { fileName ->
    register(fileName, JavaExec::class) {
      mainClass.set("edu.umontreal.kotlingrad.samples.${fileName}Kt")
      classpath = sourceSets["main"].runtimeClasspath
    }
  }

/*
If overwriting an older version, it is necessary to first run:

rm -rf ~/.m2/repository/com/github/breandan/kaliningraph \
       ~/.ivy2/cache/com.github.breandan/kaliningraph

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
      "LinearRegression", "VariableCapture"
    )
  }
}
