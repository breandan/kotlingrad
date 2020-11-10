package edu.mcgill.shipshape

fun genTuple() =
  """
    data class T2<A1, A2>(val t1: A1, val t2: A2)

    data class T3<A1, A2, A3>(val t1: A1, val t2: A2, val t3: A3)

    data class T4<A1, A2, A3, A4>(val t1: A1, val t2: A2, val t3: A3, val t4: A4)

    data class T5<A1, A2, A3, A4, A5>(val t1: A1, val t2: A2, val t3: A3, val t4: A4, val t5: A5)

    data class T6<A1, A2, A3, A4, A5, A6>(val t1: A1, val t2: A2, val t3: A3, val t4: A4, val t5: A5, val t6: A6)

    infix fun <A1, A2> A1.cc(a2: A2): T2<A1, A2> {
        return T2(this, a2)
    }

    infix fun <A1, A2, A3> T2<A1, A2>.cc(a3: A3): T3<A1, A2, A3> {
        return T3(this.t1, this.t2, a3)
    }

    infix fun <A1, A2, A3, A4> T3<A1, A2, A3>.cc(a4: A4): T4<A1, A2, A3, A4> {
        return T4(this.t1, this.t2, this.t3, a4)
    }

    infix fun <A1, A2, A3, A4, A5> T4<A1, A2, A3, A4>.cc(a5: A5): T5<A1, A2, A3, A4, A5> {
        return T5(this.t1, this.t2, this.t3, this.t4, a5)
    }

    infix fun <A1, A2, A3, A4, A5, A6> T5<A1, A2, A3, A4, A5>.cc(a6: A6): T6<A1, A2, A3, A4, A5, A6> {
        return T6(this.t1, this.t2, this.t3, this.t4, this.t5, a6)
    }
  """.trimIndent()

data class T2<A1, A2>(val t1: A1, val t2: A2)

data class T3<A1, A2, A3>(val t1: A1, val t2: A2, val t3: A3)

data class T4<A1, A2, A3, A4>(val t1: A1, val t2: A2, val t3: A3, val t4: A4)

data class T5<A1, A2, A3, A4, A5>(val t1: A1, val t2: A2, val t3: A3, val t4: A4, val t5: A5)

data class T6<A1, A2, A3, A4, A5, A6>(val t1: A1, val t2: A2, val t3: A3, val t4: A4, val t5: A5, val t6: A6)

infix fun <A1, A2> A1.cc(a2: A2): T2<A1, A2> {
  return T2(this, a2)
}

infix fun <A1, A2, A3> T2<A1, A2>.cc(a3: A3): T3<A1, A2, A3> {
  return T3(this.t1, this.t2, a3)
}

infix fun <A1, A2, A3, A4> T3<A1, A2, A3>.cc(a4: A4): T4<A1, A2, A3, A4> {
  return T4(this.t1, this.t2, this.t3, a4)
}

infix fun <A1, A2, A3, A4, A5> T4<A1, A2, A3, A4>.cc(a5: A5): T5<A1, A2, A3, A4, A5> {
  return T5(this.t1, this.t2, this.t3, this.t4, a5)
}

infix fun <A1, A2, A3, A4, A5, A6> T5<A1, A2, A3, A4, A5>.cc(a6: A6): T6<A1, A2, A3, A4, A5, A6> {
  return T6(this.t1, this.t2, this.t3, this.t4, this.t5, a6)
}