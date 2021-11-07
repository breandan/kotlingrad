package ai.hypergraph.kotlingrad.utils

import org.jetbrains.bio.viktor.F64Array

fun F64Array.toKotlinArray() =
  toGenericArray().map { it as DoubleArray }.toTypedArray()

infix fun F64Array.matmul(f: F64Array) =
  F64Array(shape[0], f.shape[1]) { i, j -> view(i) dot f.view(j, 1) }
