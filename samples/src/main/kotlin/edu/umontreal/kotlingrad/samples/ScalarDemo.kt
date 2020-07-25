package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*

@Suppress("DuplicatedCode")
fun main() {
  val f = x pow 3
  println(f(x to 3.0))
  println("f(x) = $f")
  val df_dx = f.d(x)
  println("f'(x) = $df_dx")
  println("f'(3) = ${df_dx(x to 3.0)}") // Should be 27
  println("f''(2) = ${df_dx.d(x)(x to 2.0)}")  // Should be 12

  val g = x pow x
  println("g(x) = $g")
  val dg_dx = g.d(x)
  println("g'(x) = $dg_dx")

  val q = y + z
  val h = x + q / x
  println("h(x) = $h")
  val j = h(q to (x pow 2))
  println("h(q = x^2) = j = $j")
  val k = j(x to 3)
  println("j(x = 2) = $k")
  val dh_dx = h.d(x)
  println("h'(x) = $dh_dx")
  println("h'(1, 2, 3) = ${dh_dx(x to 1, y to 2, z to 3)}")

  val t = g.d(x, y, z)

  val r = x + 1
  val m = x + 2
  val s = x + 3
  val ro_mos = r(x to m(x to s))
  val rom_os = r(x to m)(x to s)

  val i = 0
  println("r ∘ (m ∘ s) ∘ $i = $ro_mos ∘ $i = ${ro_mos(x to 0)}")
  println("(r ∘ m) ∘ s ∘ $i = $rom_os ∘ $i = ${rom_os(x to 0)}")
}