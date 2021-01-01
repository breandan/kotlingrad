package edu.umontreal.kotlingrad.utils.codegen

import edu.umontreal.kotlingrad.api.*
import java.io.File
import kotlin.reflect.KClass

fun main(args: Array<String>) =
  """{
  "link": "https://github.com/breandan/kotlingrad",
  "description": "A shape-safe symbolic differentiation framework for Kotlin",
  "dependencies": [
    "com.github.breandan:kotlingrad:${args[1]}"
  ],
  "imports": [
    "edu.umontreal.kotlingrad.experimental.*",
    "edu.mcgill.kaliningraph.*"
  ],
  "renderers": {
${allRecursiveSubclasses(SFun::class, VFun::class, MFun::class)
    .joinToString(",\n") { "    \"${it.qualifiedName}\" : \"HTML(\$it.html())\"" }}
  }
}
""".let { File("${args[0]}/kotlingrad.json").writeText(it) }

fun allRecursiveSubclasses(vararg classes: KClass<*>): List<KClass<*>> =
  classes.map { allRecursiveSubclasses(*it.sealedSubclasses.toTypedArray()) + it }.flatten()
