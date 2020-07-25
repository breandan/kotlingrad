package edu.umontreal.kotlingrad.ice9.ctxs

import edu.umontreal.kotlingrad.ice9.*

fun <Q: Typ<Q>, T: Fx<Q>> sin(t: T): T = t.sin()

