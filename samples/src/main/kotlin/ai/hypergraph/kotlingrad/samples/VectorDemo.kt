package ai.hypergraph.kotlingrad.samples

import ai.hypergraph.kotlingrad.api.*
import ai.hypergraph.kotlingrad.shapes.*

@Suppress("DuplicatedCode")
fun main() {
  val f = x pow 2
  println(f(x to 3.0))
  println("f(x) = $f")
  val df_dx = f.d(x)
  println("f'(x) = $df_dx")

  val g = x pow x
  println("g(x) = $g")
  val dg_dx = g.d(x)
  println("g'(x) = $dg_dx")

  val h = x + y
  println("h(x) = $h")
  val dh_dx = h.d(x)
  println("h'(x) = $dh_dx")

  val vf0 = DReal.Vec(0.0, 0.0)
  println(vf0(1, 2))
  val vf1 = Vec(y + x, y * 2)
  println(vf1)
  val bh = x * vf1 + DReal.Vec(1.0, 3.0)
  println(bh.invoke(y to 2.0, x to 4.0))
  val vf2 = Vec(x, y)
  val q = vf1 + vf2 + DReal.Vec(0.0, 0.0)
  val z = q(x to 1.0).magnitude()(y to 2.0)
  println(z)

  val vf3 = vf2 ʘ Vec(x, x)
//  val mf1 = vf3.d(x, y)
  println(vf3.d(x)(x to 1.0, y to 2.0))

  oldVecDemo()
}

fun oldVecDemo() {
  val vectorOfOne = DReal.Vec(1.0).also { println(it) }
  val vectorOfTwo = DReal.Vec(1.0, 2.0).also { println(it) }
  val vectorOfThree = DReal.Vec(1.0, 2.0, 3.0).also { println(it) }

  // Inferred type: Vec<Double, D3>List<Double>
  val add0Result = ((DReal.Vec(1.0, 2.0, 3.0) + DReal.Vec(3.0, 2.0, 1.0)) + DReal.Vec(0.0, 0.0, 0.0)).also { println("Addition result: $it\n") }
//  val add1Result = (Vec(1.0, 2.0, 3.0, 4.0) + Vec(3.0, 2.0, 1.0)) // Does not compile

  fun willOnlyAcceptVectorsOfLength2(l: Vec<DReal, D2>) {}
  willOnlyAcceptVectorsOfLength2(vectorOfTwo)
//  willOnlyAcceptVectorsOfLength2(vectorOfThree) // Does not compile
//  willOnlyAcceptVectorsOfLength2(vectorOfOne) // Does not compile

  // Unsafe construction of vectors is allowed, but may fail at runtime
//  val v = Vec(D100, IntArray(100) { 0 }.toList())
//  v + v
//  v - Vec(1, 2, 3) // Does not compile

//  val q = Vec(1, 2, 3, 4)
//  val q_2 = q[D2]
//  //q[D5]
}