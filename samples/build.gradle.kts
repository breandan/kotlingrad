plugins {
  application
  id("com.palantir.graal") version "0.7.1-20-g113a84d"
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
  implementation("com.github.lejon.T-SNE-Java:tsne:master-SNAPSHOT")
  implementation("org.jetbrains.lets-plot-kotlin:lets-plot-kotlin-api:1.2.0")
  implementation("io.github.vovak.astminer:astminer:0.6")
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
      main = "edu.umontreal.kotlingrad.samples.${fileName}Kt"
      classpath = sourceSets["main"].runtimeClasspath
    }
  }

  val jupyterRun by creating(Exec::class) {
    dependsOn(":kotlingrad:jupyterInstall")
    dependsOn(gradle.includedBuild("kaliningraph").task(":jupyterInstall"))
    commandLine("jupyter", "notebook", "--notebook-dir=notebooks")
  }

  test {
    dependsOn(
      "ScalarDemo", "MatrixDemo", "VectorDemo",
      "LinearRegression", "VariableCapture"
    )
  }
}
