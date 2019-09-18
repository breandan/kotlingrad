import com.github.csabasulyok.gradlelatex.LatexExtension

buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath("com.github.csabasulyok:gradle-latex:1.0")
  }
}

apply {
  plugin("latex")
}

LatexExtension(project).apply {
  tex(mapOf("tex" to "ptml_abstract.tex",
    "bib" to "ptml_abstract.bib",
    "pdf" to "ptml_abstract.pdf"))
}