package edu.umontreal.kotlingrad.utils.codegen

import edu.umontreal.kotlingrad.experimental.*
import java.io.File
import kotlin.reflect.KClass

fun main(args: Array<String>) =
  """{
  "link": "https://github.com/breandan/kotlingrad",
  "repositories": [
    "*mavenLocal",
    "https://jitpack.io"
  ],
  "dependencies": [
    "com.github.breandan:kotlingrad:${args[1]}"
  ],
  "imports": [
    "edu.umontreal.kotlingrad.experimental.*",
    "edu.mcgill.kaliningraph.*"
  ],
  "renderers": {
${allRecursiveSubclasses(SFun::class, VFun::class, MFun::class)
    .map { "    \"${it.qualifiedName}\" : \"HTML(\$it.html())\"" }.joinToString(",\n")}
  }
}
""".let { File("${args[0]}/kotlingrad.json").writeText(it) }

fun allRecursiveSubclasses(vararg classes: KClass<*>): List<KClass<*>> =
  classes.map { allRecursiveSubclasses(*it.sealedSubclasses.toTypedArray()) + it }.flatten()
